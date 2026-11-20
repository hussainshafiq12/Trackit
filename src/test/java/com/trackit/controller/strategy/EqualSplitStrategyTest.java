package com.trackit.controller.strategy;

import com.trackit.exception.InvalidSplitException;
import com.trackit.model.SplitDetail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Equal Split Strategy Unit Tests")
class EqualSplitStrategyTest {

    private EqualSplitStrategy strategy;

    @BeforeEach
    void setUp() {
        strategy = new EqualSplitStrategy();
    }

    @Test
    @DisplayName("Should split amount equally among 4 participants")
    void testEqualSplitStandard() {
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie", "David");
        ArrayList<SplitDetail> result = strategy.computeShares(100.0, participants, null);

        assertNotNull(result);
        assertEquals(4, result.size());
        for (SplitDetail split : result) {
            assertEquals(25.0, split.getAmount(), 0.001);
        }
    }

    @Test
    @DisplayName("Should allocate fractional cent remainder to payer (mitigating risk RSK-02)")
    void testFractionalCentRoundingAllocationToPayer() {
        // $100.00 divided by 3 participants: 33.33 each with 1 cent remainder
        List<String> participants = Arrays.asList("Alice", "Bob", "Charlie");
        ArrayList<SplitDetail> result = strategy.computeShares(100.0, participants, null);

        assertNotNull(result);
        assertEquals(3, result.size());
        // Alice (payer / first participant) receives remainder cent
        assertEquals(33.34, result.get(0).getAmount(), 0.001, "Payer should absorb the remainder cent");
        assertEquals(33.33, result.get(1).getAmount(), 0.001);
        assertEquals(33.33, result.get(2).getAmount(), 0.001);

        double totalShares = result.stream().mapToDouble(SplitDetail::getAmount).sum();
        assertEquals(100.0, totalShares, 0.001, "Total allocated shares must equal exactly total amount");
    }

    @Test
    @DisplayName("Should allocate remainder cents correctly for irregular divisions")
    void testIrregularFractionalDivision() {
        // $10.00 divided by 3: 3.34 for payer, 3.33 for others
        List<String> participants = Arrays.asList("Payer", "Member1", "Member2");
        ArrayList<SplitDetail> result = strategy.computeShares(10.0, participants, null);

        assertEquals(3.34, result.get(0).getAmount(), 0.001);
        assertEquals(3.33, result.get(1).getAmount(), 0.001);
        assertEquals(3.33, result.get(2).getAmount(), 0.001);
        assertEquals(10.0, result.stream().mapToDouble(SplitDetail::getAmount).sum(), 0.001);
    }

    @Test
    @DisplayName("Should return empty list when participants list is null or empty")
    void testEmptyParticipants() {
        ArrayList<SplitDetail> resultNull = strategy.computeShares(100.0, null, null);
        assertNotNull(resultNull);
        assertTrue(resultNull.isEmpty());

        ArrayList<SplitDetail> resultEmpty = strategy.computeShares(100.0, Collections.emptyList(), null);
        assertNotNull(resultEmpty);
        assertTrue(resultEmpty.isEmpty());
    }

    @Test
    @DisplayName("Should handle single participant taking full amount")
    void testSingleParticipant() {
        List<String> participants = Collections.singletonList("Alice");
        ArrayList<SplitDetail> result = strategy.computeShares(150.50, participants, null);

        assertEquals(1, result.size());
        assertEquals("Alice", result.get(0).getPersonName());
        assertEquals(150.50, result.get(0).getAmount(), 0.001);
    }

    @Test
    @DisplayName("Should throw InvalidSplitException when expense amount is negative")
    void testNegativeAmountThrowsInvalidSplitException() {
        List<String> participants = Arrays.asList("Alice", "Bob");
        assertThrows(InvalidSplitException.class, () -> {
            strategy.computeShares(-50.0, participants, null);
        });
    }

    @Test
    @DisplayName("Strategy type should be EQUAL")
    void testStrategyType() {
        assertEquals("EQUAL", strategy.getStrategyType());
    }
}
