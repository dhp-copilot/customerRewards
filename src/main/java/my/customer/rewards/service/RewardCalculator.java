package my.customer.rewards.service;

import my.customer.rewards.config.RewardTierProperties;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class RewardCalculator {
    private final RewardTierProperties tiers;

    public RewardCalculator(RewardTierProperties tiers) {
        this.tiers = tiers;
    }

    /**
     * Rewards use whole dollars and apply each configured tier progressively.
     */
    public int pointsFor(BigDecimal amount) {
        if (amount == null || amount.signum() <= 0) {
            return 0;
        }
        long dollars = amount.setScale(0, RoundingMode.FLOOR).longValue();
        long points = 0;
        List<RewardTierProperties.RewardTier> configuredTiers = tiers.getTiers();
        for (int index = 1; index < configuredTiers.size() - 1; index++) {
            RewardTierProperties.RewardTier tier = configuredTiers.get(index);
            long start = tier.getThreshold();
            long end = Math.min(dollars, startOfNextTier(configuredTiers, index));
            if (end > start) {
                points = addCapped(points, (end - start) * tier.getRate());
            }
        }
        RewardTierProperties.RewardTier finalTier = configuredTiers.get(configuredTiers.size() - 1);
        if (dollars > finalTier.getThreshold()) {
            points = addCapped(points, (dollars - finalTier.getThreshold()) * finalTier.getRate());
        }
        return (int) points;
    }

    private long startOfNextTier(List<RewardTierProperties.RewardTier> configuredTiers, int index) {
        return configuredTiers.get(index + 1).getThreshold();
    }

    private long addCapped(long current, long additional) {
        if (additional > Integer.MAX_VALUE - current) {
            return Integer.MAX_VALUE;
        }
        return current + additional;
    }
}
