package com.trackit.controller.strategy;

import com.trackit.exception.InvalidSplitException;
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
    @DisplayName("Should defensively throw InvalidSplitException on division-by-zero (total weight is zero)")
    void testZeroWeightSumThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> zeroWeights = Arrays.asList(0.0, 0.0);

        InvalidSplitException ex = assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, zeroWeights);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("zero"));
    }

    @Test
    @DisplayName("Should defensively throw InvalidSplitException when any weight is negative")
    void testNegativeWeightThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> negativeWeights = Arrays.asList(-2.0, 5.0);

        InvalidSplitException ex = assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, negativeWeights);
        });
        assertTrue(ex.getMessage().toLowerCase().contains("negative"));
    }

    @Test
    @DisplayName("Should throw InvalidSplitException when participant count and weights count mismatch")
    void testMismatchCountThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> weights = Arrays.asList(1.0, 2.0);

        assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, weights);
        });
    }

    @Test
    @DisplayName("Should split by proportional units (e.g. days stayed) with cent accuracy")
    void testProportionalUnitsCentAccuracy() {
        // e.g. Hotel stay: Alice 2 days, Bob 3 days, Charlie 5 days (total 10 days) on $333.33
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> weights = Arrays.asList(2.0, 3.0, 5.0);

        ArrayList<SplitDetail> result = strategy.computeShares(333.33, participants, weights);

        assertEquals(3, result.size());
        double sum = result.stream().mapToDouble(SplitDetail::getAmount).sum();
        assertEquals(333.33, sum, 0.001, "Total allocated must match exactly $333.33");
    }

    @Test
    @DisplayName("Strategy type should be WEIGHTED")
    void testStrategyType() {
        assertEquals("WEIGHTED", strategy.getStrategyType());
    }
}
