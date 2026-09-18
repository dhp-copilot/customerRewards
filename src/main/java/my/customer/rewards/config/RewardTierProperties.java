package my.customer.rewards.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Component
@ConfigurationProperties(prefix = "rewards")
public class RewardTierProperties {
    private List<RewardTier> tiers = new ArrayList<RewardTier>(Arrays.asList(
            new RewardTier(0, 0),
            new RewardTier(50, 1),
            new RewardTier(100, 2)));

    @PostConstruct
    public void validate() {
        if (tiers == null || tiers.isEmpty() || tiers.get(0).getThreshold() != 0) {
            throw new IllegalStateException("Reward tiers must start with threshold 0");
        }
        long previousThreshold = -1;
        for (RewardTier tier : tiers) {
            if (tier == null || tier.getThreshold() <= previousThreshold) {
                throw new IllegalStateException("Reward tier thresholds must be strictly increasing");
            }
            if (tier.getRate() < 0) {
                throw new IllegalStateException("Reward tier rates cannot be negative");
            }
            previousThreshold = tier.getThreshold();
        }
    }

    public List<RewardTier> getTiers() {
        return tiers;
    }

    public void setTiers(List<RewardTier> tiers) {
        this.tiers = tiers;
    }

    public static class RewardTier {
        private long threshold;
        private long rate;

        public RewardTier() {
        }

        public RewardTier(long threshold, long rate) {
            this.threshold = threshold;
            this.rate = rate;
        }

        public long getThreshold() {
            return threshold;
        }

        public void setThreshold(long threshold) {
            this.threshold = threshold;
        }

        public long getRate() {
            return rate;
        }

        public void setRate(long rate) {
            this.rate = rate;
        }
    }
}
