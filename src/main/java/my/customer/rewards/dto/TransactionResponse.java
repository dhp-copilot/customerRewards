package my.customer.rewards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import my.customer.rewards.domain.PurchaseTransaction;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Schema(description = "A customer purchase and its calculated reward points.")
public class TransactionResponse {
    @Schema(example = "42")
    private final Long id;
    @Schema(example = "alice")
    private final String username;
    @Schema(example = "120.00")
    private final BigDecimal amount;
    @Schema(example = "2024-01-15")
    private final LocalDate transactionDate;
    @Schema(example = "90")
    private final int rewardPoints;
    @Schema(example = "2026-09-18T20:21:55Z")
    private final Instant createdAt;
    @Schema(example = "alice")
    private final String createdBy;
    @Schema(example = "198.51.100.10")
    private final String clientIp;
    @Schema(example = "purchase-2026-09-18-001")
    private final String idempotencyKey;

    public TransactionResponse(PurchaseTransaction transaction, int rewardPoints) {
        this.id = transaction.getId();
        this.username = transaction.getCustomer().getUsername();
        this.amount = transaction.getAmount();
        this.transactionDate = transaction.getTransactionDate();
        this.rewardPoints = rewardPoints;
        this.createdAt = transaction.getCreatedAt();
        this.createdBy = transaction.getCreatedBy();
        this.clientIp = transaction.getClientIp();
        this.idempotencyKey = transaction.getIdempotencyKey();
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public int getRewardPoints() { return rewardPoints; }
    public Instant getCreatedAt() { return createdAt; }
    public String getCreatedBy() { return createdBy; }
    public String getClientIp() { return clientIp; }
    public String getIdempotencyKey() { return idempotencyKey; }
}
