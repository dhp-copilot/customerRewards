package my.customer.rewards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;

@Schema(description = "Reward points earned during one calendar month.")
public class MonthlyRewardReport {
    @Schema(example = "alice")
    private final String username;
    @Schema(example = "2024")
    private final int year;
    @Schema(example = "1")
    private final int month;
    @Schema(example = "195.00")
    private final BigDecimal totalAmount;
    @Schema(example = "90")
    private final int rewardPoints;

    public MonthlyRewardReport(String username, int year, int month, BigDecimal totalAmount, int rewardPoints) {
        this.username = username;
        this.year = year;
        this.month = month;
        this.totalAmount = totalAmount;
        this.rewardPoints = rewardPoints;
    }
    public String getUsername() { return username; }
    public int getYear() { return year; }
    public int getMonth() { return month; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getRewardPoints() { return rewardPoints; }
}
