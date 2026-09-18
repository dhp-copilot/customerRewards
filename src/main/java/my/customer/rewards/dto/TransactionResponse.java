package my.customer.rewards.dto;

import my.customer.rewards.domain.PurchaseTransaction;

import java.math.BigDecimal;
import java.time.LocalDate;

public class TransactionResponse {
    private final Long id;
    private final String username;
    private final BigDecimal amount;
    private final LocalDate transactionDate;
    private final int rewardPoints;

    public TransactionResponse(PurchaseTransaction transaction, int rewardPoints) {
        this.id = transaction.getId();
        this.username = transaction.getCustomer().getUsername();
        this.amount = transaction.getAmount();
        this.transactionDate = transaction.getTransactionDate();
        this.rewardPoints = rewardPoints;
    }
    public Long getId() { return id; }
    public String getUsername() { return username; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public int getRewardPoints() { return rewardPoints; }
}
