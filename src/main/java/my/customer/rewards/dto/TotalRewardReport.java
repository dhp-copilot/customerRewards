package my.customer.rewards.dto;

import java.math.BigDecimal;
import java.util.List;

public class TotalRewardReport {
    private final String username;
    private final BigDecimal totalAmount;
    private final int rewardPoints;
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
