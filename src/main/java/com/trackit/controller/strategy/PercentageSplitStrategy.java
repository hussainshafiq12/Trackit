package com.trackit.controller.strategy;

import com.trackit.exception.InvalidSplitException;
import com.trackit.model.SplitDetail;
import java.util.ArrayList;
import java.util.List;

/**
 * Percentage Split Strategy - Divides based on custom percentages.
 * Defensively validates that percentage sum equals exactly 100.0%,
 * throwing InvalidSplitException on violation.
 * 
 * LSP: Properly substitutes parent interface
 * OCP: Strategy can be selected without modifying caller
 */
public class PercentageSplitStrategy implements SplitStrategy {
    
    @Override
    public ArrayList<SplitDetail> computeShares(double totalAmount, 
                                               List<String> participants, 
                                               List<Double> percentages) {
        ArrayList<SplitDetail> splits = new ArrayList<>();
        
        if (participants == null || participants.isEmpty()) {
            if (percentages != null && !percentages.isEmpty()) {
                throw new InvalidSplitException("Participants and percentages must match in size");
            }
            return splits;
        }
        
        if (percentages == null || percentages.size() != participants.size()) {
            throw new InvalidSplitException("Participants and percentages must match in size");
        }
        
        if (totalAmount < 0) {
            throw new InvalidSplitException("Expense amount cannot be negative: " + totalAmount);
        }
        
        // Defensively check for negative percentages
        for (double p : percentages) {
            if (p < 0) {
                throw new InvalidSplitException("Percentage values cannot be negative: " + p);
            }
        }
        
        // Validate percentages sum to exactly 100.0% (within 0.01 tolerance)
        double totalPercentage = percentages.stream()
                                           .mapToDouble(Double::doubleValue)
                                           .sum();
        
        if (Math.abs(totalPercentage - 100.0) > 0.01) {
            throw new InvalidSplitException("Percentages must sum to 100%. Current sum: " + totalPercentage);
        }

        if (totalAmount == 0.0) {
            for (String participant : participants) {
                splits.add(new SplitDetail(participant, 0.0));
            }
            return splits;
        }
        
        // Calculate shares with cent-level accuracy
        long totalCents = Math.round(totalAmount * 100.0);
        int size = participants.size();
        
        for (int i = 0; i < size; i++) {
            String participant = participants.get(i);
            double percentage = percentages.get(i);
            long shareCents = Math.round((percentage / 100.0) * totalCents);
            splits.add(new SplitDetail(participant, shareCents / 100.0));
        }
        
        // Allocate any fractional cent rounding difference to the first person (payer) for consistency with EqualSplitStrategy
        long sumCents = splits.stream().mapToLong(s -> Math.round(s.getAmount() * 100.0)).sum();
        long diffCents = totalCents - sumCents;
        if (diffCents != 0 && !splits.isEmpty()) {
            SplitDetail first = splits.get(0);
            splits.set(0, new SplitDetail(first.getPersonName(), (Math.round(first.getAmount() * 100.0) + diffCents) / 100.0));
        }
        
        return splits;
    }
    
    @Override
    public String getStrategyType() {
        return "PERCENTAGE";
    }
}
