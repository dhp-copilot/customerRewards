package my.customer.rewards.controller;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.greaterThanOrEqualTo;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class ApiImprovementsIntegrationTest {
    @Autowired
    private MockMvc mvc;

    @Test
    void transactionsArePagedWithStableOrderingAndValidatedSize() throws Exception {
        String token = token("alice");

        mvc.perform(get("/api/customers/alice/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()", is(2)))
                .andExpect(jsonPath("$.totalElements", greaterThanOrEqualTo(3)))
                .andExpect(jsonPath("$.content[0].transactionDate", is("2024-01-15")));

        mvc.perform(get("/api/customers/alice/transactions")
                        .header("Authorization", "Bearer " + token)
                        .param("size", "101"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void transactionCreationIsIdempotentAndIncludesAuditFields() throws Exception {
        String token = token("alice");
        String key = "focused-test-" + System.nanoTime();
        String body = "{\"amount\":17.50,\"transactionDate\":\"2026-09-18\"}";

        String first = mvc.perform(post("/api/customers/alice/transactions")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .with(request -> {
                            request.setRemoteAddr("203.0.113.8");
                            return request;
                        })
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.createdBy", is("alice")))
                .andExpect(jsonPath("$.clientIp", is("203.0.113.8")))
                .andExpect(jsonPath("$.idempotencyKey", is(key)))
                .andReturn().getResponse().getContentAsString();
        String id = first.replaceFirst(".*\"id\":([0-9]+).*", "$1");

        mvc.perform(post("/api/customers/alice/transactions")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(Integer.valueOf(id))));

        mvc.perform(post("/api/customers/alice/transactions")
                        .header("Authorization", "Bearer " + token)
                        .header("Idempotency-Key", key)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"amount\":18.50,\"transactionDate\":\"2026-09-18\"}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status", is(409)));
    }

    @Test
    void openApiIncludesOperationSecurityAndExamples() throws Exception {
        mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paths['/api/auth/login'].post.summary",
                        is("Authenticate a user")))
                .andExpect(jsonPath("$.paths['/api/customers/{username}/transactions'].get.security").exists())
                .andExpect(jsonPath("$.components.schemas.TransactionRequest.properties.amount.example",
                        is(120.0)));
    }

    private String token(String username) throws Exception {
        return mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\":\"" + username + "\",\"password\":\"password\"}"))
                .andExpect(status().isOk())
                .andReturn().getResponse().getContentAsString()
                .replaceFirst(".*\"token\":\"([^\"]+)\".*", "$1");
    }
}
