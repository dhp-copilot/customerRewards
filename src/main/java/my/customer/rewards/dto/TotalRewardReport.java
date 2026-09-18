package my.customer.rewards.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Customer reward totals grouped by month.")
public class TotalRewardReport {
    @Schema(example = "alice")
    private final String username;
    @Schema(example = "395.00")
    private final BigDecimal totalAmount;
    @Schema(example = "250")
    private final int rewardPoints;
    @Schema(description = "Monthly reports in ascending calendar order")
    private final List<MonthlyRewardReport> monthly;

    public TotalRewardReport(String username, BigDecimal totalAmount, int rewardPoints,
                             List<MonthlyRewardReport> monthly) {
        this.username = username;
        this.totalAmount = totalAmount;
        this.rewardPoints = rewardPoints;
        this.monthly = monthly;
    }
    public String getUsername() { return username; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public int getRewardPoints() { return rewardPoints; }
    public List<MonthlyRewardReport> getMonthly() { return monthly; }
}
