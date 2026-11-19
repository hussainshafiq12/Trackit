package com.trackit.controller;

import java.util.*;

/**
 * SettlementOptimizer - Implements minimal transaction settlement algorithm.
 * Uses a recursive greedy Min-Cash-Flow approach to minimize the number of transactions
 * needed to settle complex group balances.
 * 
 * Algorithm: Min-Cash-Flow Greedy Algorithm
 * 1. Computes net member balances (Paid - Owed)
 * 2. Recursively settles maximum debtors against maximum creditors
 * 3. Reduces complex N-person debt networks to at most N-1 transactions
 */
public class SettlementOptimizer {
    
    private static final double EPSILON = 0.009; // Cent-level threshold
    
    /**
     * Transaction represents a payment from debtor to creditor.
     */
    public static class Transaction {
        public final String from;
        public final String to;
        public final double amount;
        
        public Transaction(String from, String to, double amount) {
            this.from = from;
            this.to = to;
            this.amount = Math.round(amount * 100.0) / 100.0;
        }
        
        @Override
        public String toString() {
            return String.format("%s → %s: $%.2f", from, to, amount);
        }
    }
    
    /**
     * Computes net member balances: Paid - Owed.
     * Positive balance indicates the member is owed money (creditor).
     * Negative balance indicates the member owes money (debtor).
     */
    public static Map<String, Double> computeNetBalances(Map<String, Double> paid, Map<String, Double> owed) {
        Map<String, Double> netBalances = new HashMap<>();
        Set<String> allMembers = new HashSet<>();
        if (paid != null) allMembers.addAll(paid.keySet());
        if (owed != null) allMembers.addAll(owed.keySet());
        
        for (String member : allMembers) {
            double amountPaid = (paid != null) ? paid.getOrDefault(member, 0.0) : 0.0;
            double amountOwed = (owed != null) ? owed.getOrDefault(member, 0.0) : 0.0;
            double net = Math.round((amountPaid - amountOwed) * 100.0) / 100.0;
            netBalances.put(member, net);
        }
        return netBalances;
    }
    
    /**
     * Optimize settlement transactions using recursive greedy Min-Cash-Flow.
     * @param balances Map of username to net balance (positive = owed money, negative = owes money)
     * @return List of minimal transactions to completely clear all balances
     */
    public static List<Transaction> optimizeSettlement(Map<String, Double> balances) {
        List<Transaction> transactions = new ArrayList<>();
        if (balances == null || balances.isEmpty()) {
            return transactions;
        }
        
        // Filter out zero balances and maintain cent precision
        Map<String, Double> workingBalances = new HashMap<>();
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double val = Math.round(entry.getValue() * 100.0) / 100.0;
            if (Math.abs(val) > EPSILON) {
                workingBalances.put(entry.getKey(), val);
            }
        }
        
        // Execute recursive greedy debt settlement
        settleMinCashFlowRecursive(workingBalances, transactions);
        
        return transactions;
    }
    
    /**
     * Recursive Min-Cash-Flow engine:
     * Identifies the person with max debt and person with max credit, settles the minimum
     * of their amounts, updates the state, and recurses until all accounts are cleared.
     */
    private static void settleMinCashFlowRecursive(Map<String, Double> balances, List<Transaction> transactions) {
        if (balances.isEmpty()) {
            return;
        }
        
        String maxCreditor = null;
        double maxCredit = 0.0;
        
        String maxDebtor = null;
        double maxDebit = 0.0;
        
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double amount = entry.getValue();
            if (amount > maxCredit) {
                maxCredit = amount;
                maxCreditor = entry.getKey();
            }
            if (amount < maxDebit) {
                maxDebit = amount;
                maxDebtor = entry.getKey();
            }
        }
        
        // Stop recursion when no creditor or debtor remains
        if (maxCreditor == null || maxDebtor == null || maxCredit <= EPSILON || maxDebit >= -EPSILON) {
            return;
        }
        
        // Settle the minimum of what debtor owes and what creditor is owed
        double settlementAmount = Math.round(Math.min(maxCredit, Math.abs(maxDebit)) * 100.0) / 100.0;
        
        if (settlementAmount > EPSILON) {
            transactions.add(new Transaction(maxDebtor, maxCreditor, settlementAmount));
            
            double updatedCredit = Math.round((maxCredit - settlementAmount) * 100.0) / 100.0;
            double updatedDebit = Math.round((maxDebit + settlementAmount) * 100.0) / 100.0;
            
            if (Math.abs(updatedCredit) <= EPSILON) {
                balances.remove(maxCreditor);
            } else {
                balances.put(maxCreditor, updatedCredit);
            }
            
            if (Math.abs(updatedDebit) <= EPSILON) {
                balances.remove(maxDebtor);
            } else {
                balances.put(maxDebtor, updatedDebit);
            }
            
            // Recurse for remaining balances
            settleMinCashFlowRecursive(balances, transactions);
        }
    }
    
    /**
     * Calculate direct vs optimized transaction count savings.
     */
    public static SettlementSavings calculateSavings(Map<String, Double> balances) {
        if (balances == null || balances.isEmpty()) {
            return new SettlementSavings(0, 0, 0);
        }
        
        List<Transaction> optimized = optimizeSettlement(balances);
        
        long debtors = balances.values().stream().filter(b -> b < -EPSILON).count();
        long creditors = balances.values().stream().filter(b -> b > EPSILON).count();
        int directTransactions = (int) (debtors * creditors);
        int optimizedTransactions = optimized.size();
        int saved = Math.max(0, directTransactions - optimizedTransactions);
        
        return new SettlementSavings(directTransactions, optimizedTransactions, saved);
    }
    
    /**
     * Holds metrics regarding transaction minimization efficiency.
     */
    public static class SettlementSavings {
        public final int directTransactions;
        public final int optimizedTransactions;
        public final int transactionsSaved;
        
        public SettlementSavings(int direct, int optimized, int saved) {
            this.directTransactions = direct;
            this.optimizedTransactions = optimized;
            this.transactionsSaved = saved;
        }
    }
}
