package my.customer.rewards.security;

import org.junit.jupiter.api.Test;
import org.springframework.mock.web.*;

import javax.servlet.FilterChain;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RateLimitFilterTest {
    @Test
    void returnsStructured429AfterOneHundredRequestsPerClient() throws Exception {
        RateLimitFilter filter = new RateLimitFilter();
        FilterChain chain = (request, response) -> { };
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/rewards/alice/total");
        request.setRemoteAddr("10.10.10.10");
        for (int i = 0; i < 100; i++) {
            filter.doFilter(request, new MockHttpServletResponse(), chain);
        }
        MockHttpServletResponse limited = new MockHttpServletResponse();
        filter.doFilter(request, limited, chain);
        assertEquals(429, limited.getStatus());
        org.junit.jupiter.api.Assertions.assertTrue(limited.getContentAsString().contains("\"status\":429"));
        org.junit.jupiter.api.Assertions.assertNotNull(limited.getHeader("Retry-After"));
    }
}
