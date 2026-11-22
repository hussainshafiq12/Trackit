package com.trackit.controller;

import java.util.*;

/**
 * SettlementOptimizer - Implements minimal transaction settlement algorithm.
 * Uses a recursive greedy Min-Cash-Flow approach to minimize the number of transactions
 * needed to settle complex group balances.
 * 
 * Algorithm: Min-Cash-Flow Greedy Algorithm
 * 1. Computes net member balances (Paid - Owed) with safe null handling
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
     * Includes null-safety checks to prevent NullPointerException when map values are null.
     * Positive balance indicates the member is owed money (creditor).
     * Negative balance indicates the member owes money (debtor).
     */
    public static Map<String, Double> computeNetBalances(Map<String, Double> paid, Map<String, Double> owed) {
        Map<String, Double> netBalances = new HashMap<>();
        Set<String> allMembers = new HashSet<>();
        if (paid != null) allMembers.addAll(paid.keySet());
        if (owed != null) allMembers.addAll(owed.keySet());
        
        for (String member : allMembers) {
            Double pVal = (paid != null) ? paid.get(member) : null;
            double amountPaid = (pVal != null) ? pVal : 0.0;
            Double oVal = (owed != null) ? owed.get(member) : null;
            double amountOwed = (oVal != null) ? oVal : 0.0;
            
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
            Double valObj = entry.getValue();
            if (valObj != null) {
                double val = Math.round(valObj * 100.0) / 100.0;
                if (Math.abs(val) > EPSILON) {
                    workingBalances.put(entry.getKey(), val);
                }
            }
        }
        
        // Phase 1: Fast path for exact-match debts (Pairwise Cancellation)
        // If debtor owes exactly what a creditor is owed, settle immediately to reduce recursion depth.
        eliminateExactMatches(workingBalances, transactions);
        
        // Phase 2: Execute recursive greedy debt settlement for multi-party remaining balances
        settleMinCashFlowRecursive(workingBalances, transactions);
        
        return transactions;
    }
    
    /**
     * Fast-path optimization: scans for exact matching pairs where debtor owes
     * precisely what a creditor is owed (|debit| == credit). Settling them directly
     * reduces the problem size and greedy recursion depth from O(N) to O(N - 2k).
     */
    private static void eliminateExactMatches(Map<String, Double> balances, List<Transaction> transactions) {
        boolean matchFound = true;
        while (matchFound) {
            matchFound = false;
            String matchedDebtor = null;
            String matchedCreditor = null;
            double matchAmount = 0.0;
            
            for (Map.Entry<String, Double> dEntry : balances.entrySet()) {
                if (dEntry.getValue() < -EPSILON) {
                    double debt = Math.abs(dEntry.getValue());
                    for (Map.Entry<String, Double> cEntry : balances.entrySet()) {
                        if (cEntry.getValue() > EPSILON && Math.abs(debt - cEntry.getValue()) <= EPSILON) {
                            matchedDebtor = dEntry.getKey();
                            matchedCreditor = cEntry.getKey();
                            matchAmount = debt;
                            matchFound = true;
                            break;
                        }
                    }
                }
                if (matchFound) break;
            }
            
            if (matchFound && matchedDebtor != null && matchedCreditor != null) {
                transactions.add(new Transaction(matchedDebtor, matchedCreditor, matchAmount));
                balances.remove(matchedDebtor);
                balances.remove(matchedCreditor);
            }
        }
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
        
        long debtors = balances.values().stream().filter(b -> b != null && b < -EPSILON).count();
        long creditors = balances.values().stream().filter(b -> b != null && b > EPSILON).count();
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
