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

@DisplayName("Percentage Split Strategy Unit Tests")
class PercentageSplitStrategyTest {

    private PercentageSplitStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new PercentageSplitStrategy();
    }

    @Test
    @DisplayName("Should split amount correctly when percentages sum to 100")
    void testValidPercentageSplit() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> percentages = Arrays.asList(50.0, 30.0, 20.0);

        ArrayList<SplitDetail> result = strategy.computeShares(200.0, participants, percentages);

        assertEquals(3, result.size());
        assertEquals(100.0, result.get(0).getAmount(), 0.001);
        assertEquals(60.0, result.get(1).getAmount(), 0.001);
        assertEquals(40.0, result.get(2).getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should defensively throw InvalidSplitException when percentages do not sum to 100%")
    void testInvalidPercentageSumThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> invalidPercentages = Arrays.asList(40.0, 40.0); // Sum is 80%, not 100%

        InvalidSplitException thrown = assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, invalidPercentages);
        });

        assertTrue(thrown.getMessage().contains("100%"));
    }

    @Test
    @DisplayName("Should throw InvalidSplitException when percentage is negative")
    void testNegativePercentageThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> invalidPercentages = Arrays.asList(-10.0, 110.0);

        InvalidSplitException thrown = assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, invalidPercentages);
        });

        assertTrue(thrown.getMessage().toLowerCase().contains("negative") || thrown.getMessage().contains("100%"));
    }

    @Test
    @DisplayName("Should throw InvalidSplitException when participant count and percentage count mismatch")
    void testMismatchCountThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> percentages = Arrays.asList(50.0, 50.0);

        assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(100.0, participants, percentages);
        });
    }

    @Test
    @DisplayName("Should conserve total amount without rounding drift")
    void testCentAccuracyPercentageSplit() {
        // 100 / 3 roughly: 33.33%, 33.33%, 33.34%
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> percentages = Arrays.asList(33.33, 33.33, 33.34);

        ArrayList<SplitDetail> result = strategy.computeShares(100.0, participants, percentages);

        assertEquals(3, result.size());
        double totalSum = result.stream().mapToDouble(SplitDetail::getAmount).sum();
        assertEquals(100.0, totalSum, 0.001);
    }

    @Test
    @DisplayName("Strategy type should be PERCENTAGE")
    void testStrategyType() {
        assertEquals("PERCENTAGE", strategy.getStrategyType());
    }
}
