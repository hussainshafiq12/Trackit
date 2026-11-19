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
    @DisplayName("Should compute net member balances (Paid - Owed) accurately")
    void testComputeNetBalances() {
        Map<String, Double> paid = new HashMap<>();
        paid.put("Alice", 100.0);
        paid.put("Bob", 50.0);
        paid.put("Charlie", 0.0);

        Map<String, Double> owed = new HashMap<>();
        owed.put("Alice", 50.0);
        owed.put("Bob", 50.0);
        owed.put("Charlie", 50.0);

        Map<String, Double> net = SettlementOptimizer.computeNetBalances(paid, owed);

        assertEquals(50.0, net.get("Alice"), 0.001); // 100 - 50 = +50
        assertEquals(0.0, net.get("Bob"), 0.001);    // 50 - 50 = 0
        assertEquals(-50.0, net.get("Charlie"), 0.001); // 0 - 50 = -50
    }

    @Test
    @DisplayName("Should resolve 4-person circular debt with zero transactions")
    void testFourPersonCircularDebtZeroTransactions() {
        // In a perfect circular debt, net balances sum to zero for everyone
        Map<String, Double> balances = new HashMap<>();
        balances.put("Alice", 0.0);
        balances.put("Bob", 0.0);
        balances.put("Charlie", 0.0);
        balances.put("David", 0.0);

        List<SettlementOptimizer.Transaction> transactions = SettlementOptimizer.optimizeSettlement(balances);

        assertNotNull(transactions);
        assertTrue(transactions.isEmpty(), "Circular balanced debts should require 0 transactions");
    }

    @Test
    @DisplayName("Should settle complex 5-person group debt down to at most N-1 transactions")
    void testComplexFivePersonSettlement() {
        Map<String, Double> balances = new HashMap<>();
        balances.put("Alice", -40.0);
        balances.put("Bob", -25.0);
        balances.put("Charlie", -15.0);
        balances.put("David", 30.0);
        balances.put("Emma", 50.0);
        // Total negative = -80, total positive = +80

        List<SettlementOptimizer.Transaction> transactions = SettlementOptimizer.optimizeSettlement(balances);

        assertNotNull(transactions);
        assertTrue(transactions.size() <= 4, "5 participants should settle in at most 4 transactions");

        // Verify that after applying all transactions, every member's balance is zeroed
        Map<String, Double> verificationBalances = new HashMap<>(balances);
        for (SettlementOptimizer.Transaction tx : transactions) {
            verificationBalances.put(tx.from, verificationBalances.get(tx.from) + tx.amount);
            verificationBalances.put(tx.to, verificationBalances.get(tx.to) - tx.amount);
        }

        for (Map.Entry<String, Double> entry : verificationBalances.entrySet()) {
            assertEquals(0.0, entry.getValue(), 0.01, "Member " + entry.getKey() + " must have 0 balance");
        }
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
    @DisplayName("Should safely handle null or empty balances map")
    void testNullOrEmptyBalances() {
        List<SettlementOptimizer.Transaction> resultNull = SettlementOptimizer.optimizeSettlement(null);
        assertNotNull(resultNull);
        assertTrue(resultNull.isEmpty());

        List<SettlementOptimizer.Transaction> resultEmpty = SettlementOptimizer.optimizeSettlement(new HashMap<>());
        assertNotNull(resultEmpty);
        assertTrue(resultEmpty.isEmpty());
    }
}
