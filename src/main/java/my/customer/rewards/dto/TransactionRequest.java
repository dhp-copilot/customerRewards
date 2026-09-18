package my.customer.rewards.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import io.swagger.v3.oas.annotations.media.Schema;

import javax.validation.constraints.*;
import java.math.BigDecimal;
import java.time.LocalDate;

@Schema(description = "Purchase transaction payload.")
public class TransactionRequest {
    @NotNull
    @DecimalMin(value = "0.00", inclusive = false)
    @Digits(integer = 10, fraction = 2)
    @Schema(description = "Purchase amount in dollars", example = "120.00", required = true)
    private BigDecimal amount;

    @NotNull
    @JsonAlias({"date", "transactionDate"})
    @Schema(description = "Date of the purchase", example = "2024-01-15", required = true)
    private LocalDate transactionDate;

    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getTransactionDate() { return transactionDate; }
    public void setTransactionDate(LocalDate transactionDate) { this.transactionDate = transactionDate; }
}
