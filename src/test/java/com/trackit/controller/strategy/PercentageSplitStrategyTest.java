package com.trackit.controller.strategy;

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
    @DisplayName("Should throw exception when percentages do not sum to 100")
    void testInvalidPercentageSum() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        List<Double> invalidPercentages = Arrays.asList(40.0, 40.0); // Sum is 80, not 100

        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            strategy.computeShares(100.0, participants, invalidPercentages);
        });

        assertTrue(thrown.getMessage().contains("Percentages must sum to 100%"));
    }

    @Test
    @DisplayName("Should throw exception when participant count and percentage count mismatch")
    void testMismatchCount() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        List<Double> percentages = Arrays.asList(50.0, 50.0);

        assertThrows(IllegalArgumentException.class, () -> {
            strategy.computeShares(100.0, participants, percentages);
        });
    }

    @Test
    @DisplayName("Strategy type should be PERCENTAGE")
    void testStrategyType() {
        assertEquals("PERCENTAGE", strategy.getStrategyType());
    }
}
