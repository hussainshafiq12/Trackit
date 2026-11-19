package com.trackit.controller.strategy;

import com.trackit.model.SplitDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Weighted Split Strategy Unit Tests")
class WeightedSplitStrategyTest {

    private WeightedSplitStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new WeightedSplitStrategy();
    }

    @Test
    @DisplayName("Should split proportionally according to custom weights")
    void testValidWeightedSplit() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> weights = Arrays.asList(3.0, 1.0); // 3:1 ratio (75% and 25%)

        ArrayList<SplitDetail> result = strategy.computeShares(100.0, participants, weights);

        assertEquals(2, result.size());
        assertEquals(75.0, result.get(0).getAmount(), 0.001);
        assertEquals(25.0, result.get(1).getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should throw exception when total weight is zero")
    void testZeroWeightSum() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> zeroWeights = Arrays.asList(0.0, 0.0);

        assertThrows(IllegalArgumentException.class, () -> {
            strategy.computeShares(100.0, participants, zeroWeights);
        });
    }

    @Test
    @DisplayName("Should throw exception when participant count and weights count mismatch")
    void testMismatchCount() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> weights = Arrays.asList(1.0, 2.0);

        assertThrows(IllegalArgumentException.class, () -> {
            strategy.computeShares(100.0, participants, weights);
        });
    }

    @Test
    @DisplayName("Strategy type should be WEIGHTED")
    void testStrategyType() {
        assertEquals("WEIGHTED", strategy.getStrategyType());
    }
}
