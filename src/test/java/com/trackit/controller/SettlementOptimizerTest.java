package com.trackit.controller;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("SettlementOptimizer Greedy Min-Cash-Flow Algorithm Tests")
class SettlementOptimizerTest {

    @Test
    @DisplayName("Should minimize transactions in a 3-person cycle")
    void testThreePersonCycleSettlement() {
        // Alice owes Bob $10, Bob owes Charlie $10 -> Alice pays Charlie $10 (1 transaction instead of 2)
        Map<String, Double> balances = new HashMap<>();
        balances.put("Alice", -10.0);
        balances.put("Bob", 0.0);
        balances.put("Charlie", 10.0);

        List<SettlementOptimizer.Transaction> transactions = SettlementOptimizer.optimizeSettlement(balances);

        assertNotNull(transactions);
        assertEquals(1, transactions.size());
        assertEquals("Alice", transactions.get(0).from);
        assertEquals("Charlie", transactions.get(0).to);
        assertEquals(10.0, transactions.get(0).amount, 0.001);
    }

    @Test
    @DisplayName("Should correctly calculate transaction savings")
    void testCalculateSavings() {
        Map<String, Double> balances = new HashMap<>();
        balances.put("Alice", -20.0);
        balances.put("Bob", -30.0);
        balances.put("Charlie", 50.0);

        SettlementOptimizer.SettlementSavings savings = SettlementOptimizer.calculateSavings(balances);

        assertNotNull(savings);
        assertEquals(2, savings.directTransactions); // 2 debtors * 1 creditor = 2
        assertEquals(2, savings.optimizedTransactions);
        assertEquals(0, savings.transactionsSaved);
    }

    @Test
    @DisplayName("Should return empty transaction list when all balances are zero")
    void testZeroBalances() {
        Map<String, Double> balances = new HashMap<>();
        balances.put("Alice", 0.0);
        balances.put("Bob", 0.0);

        List<SettlementOptimizer.Transaction> transactions = SettlementOptimizer.optimizeSettlement(balances);

        assertNotNull(transactions);
        assertTrue(transactions.isEmpty());
    }
}
