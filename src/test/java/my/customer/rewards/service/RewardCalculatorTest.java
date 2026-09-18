package my.customer.rewards.service;

import my.customer.rewards.config.RewardTierProperties;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertEquals;

class RewardCalculatorTest {
    private final RewardTierProperties defaultTiers = new RewardTierProperties();

    @Test
    void appliesTierBoundariesAndWholeDollarRule() {
        RewardCalculator calculator = new RewardCalculator(defaultTiers);
        assertEquals(0, calculator.pointsFor(new BigDecimal("50.99")));
        assertEquals(1, calculator.pointsFor(new BigDecimal("51.00")));
        assertEquals(50, calculator.pointsFor(new BigDecimal("100.00")));
        assertEquals(52, calculator.pointsFor(new BigDecimal("101.00")));
        assertEquals(90, calculator.pointsFor(new BigDecimal("120.00")));
        assertEquals(0, calculator.pointsFor(new BigDecimal("-1.00")));
    }

    @Test
    void usesConfiguredThresholdsAndRates() {
        RewardTierProperties tiers = new RewardTierProperties();
        tiers.setTiers(Arrays.asList(
                new RewardTierProperties.RewardTier(0, 0),
                new RewardTierProperties.RewardTier(10, 3),
                new RewardTierProperties.RewardTier(20, 4),
                new RewardTierProperties.RewardTier(30, 6)));

        assertEquals(100, new RewardCalculator(tiers).pointsFor(new BigDecimal("35.99")));
    }
}
