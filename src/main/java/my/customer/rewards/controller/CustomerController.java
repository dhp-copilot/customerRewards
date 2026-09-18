package my.customer.rewards.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import my.customer.rewards.dto.*;
import my.customer.rewards.exception.ApiError;
import my.customer.rewards.service.CustomerService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;

@RestController
@RequestMapping("/api/customers")
@Validated
@Tag(name = "Customers", description = "Customer transactions and purchase history.")
public class CustomerController {
    private final CustomerService customerService;

    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }

    @PostMapping("/{username}/transactions")
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "Create a purchase transaction", description = "Creates a transaction for the customer. "
            + "The Idempotency-Key is scoped to the customer and safely supports retries.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Transaction created or idempotent retry",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request or missing Idempotency-Key",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "The authenticated user cannot access this customer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Idempotency-Key conflicts with another payload",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public TransactionResponse addTransaction(
                                              @Parameter(description = "Customer username", example = "alice")
                                              @PathVariable String username,
                                              @Valid @RequestBody TransactionRequest request,
                                              @Parameter(name = "Idempotency-Key", in = ParameterIn.HEADER,
                                                      required = true, example = "purchase-2026-09-18-001")
                                              @RequestHeader("Idempotency-Key") String idempotencyKey,
                                              Authentication authentication,
                                              HttpServletRequest servletRequest) {
        return customerService.addTransaction(username, request, authentication.getName(),
                servletRequest.getRemoteAddr(), idempotencyKey);
    }

    @GetMapping("/{username}/transactions")
    @PreAuthorize("#username == authentication.name or hasRole('ADMIN')")
    @Operation(summary = "List customer transactions", description = "Returns transactions ordered by "
            + "transaction date and ID, with stable page metadata.",
            security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paged transactions"),
            @ApiResponse(responseCode = "400", description = "Invalid page or size",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "The authenticated user cannot access this customer",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    public Page<TransactionResponse> transactions(
            @Parameter(description = "Customer username", example = "alice")
            @PathVariable String username,
            @Parameter(description = "Zero-based page number", example = "0")
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @Parameter(description = "Page size (maximum 100)", example = "20")
            @RequestParam(defaultValue = "20") @Min(1) @Max(100) int size) {
        return customerService.transactions(username, PageRequest.of(page, size,
                Sort.by(Sort.Direction.ASC, "transactionDate")
                        .and(Sort.by(Sort.Direction.ASC, "id"))));
    }
}
