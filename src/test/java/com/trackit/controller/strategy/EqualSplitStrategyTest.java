package com.trackit.controller.strategy;

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
    @DisplayName("Strategy type should be EQUAL")
    void testStrategyType() {
        assertEquals("EQUAL", strategy.getStrategyType());
    }
}
