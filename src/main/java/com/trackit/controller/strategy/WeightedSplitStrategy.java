package com.trackit.controller.strategy;

import com.trackit.exception.InvalidSplitException;
import com.trackit.model.SplitDetail;
import java.util.ArrayList;
import java.util.List;

/**
 * Weighted Split Strategy - Divides based on proportional units (e.g. days stayed, consumption ratio).
 * Defensively guards against division-by-zero and negative weights with InvalidSplitException.
 * 
 * LSP: Properly substitutes parent interface
 * OCP: Dynamically selectable split algorithm
 */
public class WeightedSplitStrategy implements SplitStrategy {
    
    @Override
    public ArrayList<SplitDetail> computeShares(double totalAmount, 
                                               List<String> participants, 
                                               List<Double> weights) {
        ArrayList<SplitDetail> splits = new ArrayList<>();
        
        if (participants == null || participants.isEmpty()) {
            if (weights != null && !weights.isEmpty()) {
                throw new InvalidSplitException("Participants and weights must match in size");
            }
            return splits;
        }
        
        if (weights == null || weights.size() != participants.size()) {
            throw new InvalidSplitException("Participants and weights must match in size");
        }
        
        // Defensively check for negative weights
        for (double w : weights) {
            if (w < 0) {
                throw new InvalidSplitException("Weight value cannot be negative: " + w);
            }
        }
        
        // Calculate total weight to guard against division-by-zero
        double totalWeight = weights.stream()
                                    .mapToDouble(Double::doubleValue)
                                    .sum();
        
        if (totalWeight <= 0.0000001) {
            throw new InvalidSplitException("Total weight cannot be zero (division-by-zero prevention)");
        }

        if (totalAmount <= 0) {
            for (String participant : participants) {
                splits.add(new SplitDetail(participant, 0.0));
            }
            return splits;
        }
        
        // Calculate proportional shares with cent-level accuracy
        long totalCents = Math.round(totalAmount * 100.0);
        long allocatedCents = 0;
        int size = participants.size();
        
        for (int i = 0; i < size; i++) {
            String participant = participants.get(i);
            double weight = weights.get(i);
            
            long shareCents;
            if (i == size - 1) {
                // Last participant absorbs any fractional cent rounding difference
                shareCents = totalCents - allocatedCents;
            } else {
                shareCents = Math.round((weight / totalWeight) * totalCents);
                allocatedCents += shareCents;
            }
            
            splits.add(new SplitDetail(participant, shareCents / 100.0));
        }
        
        return splits;
    }
    
    @Override
    public String getStrategyType() {
        return "WEIGHTED";
    }
}
