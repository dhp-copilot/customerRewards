package my.customer.rewards.dto;

import java.math.BigDecimal;

public class MonthlyRewardReport {
    private final String username;
    private final int year;
    private final int month;
    private final BigDecimal totalAmount;
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
