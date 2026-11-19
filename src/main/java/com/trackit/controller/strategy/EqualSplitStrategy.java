package com.trackit.controller.strategy;

import com.trackit.model.SplitDetail;
import java.util.ArrayList;
import java.util.List;

/**
 * Equal Split Strategy Implementation
 * Divides total amount evenly among group members.
 * Handles fractional cent rounding errors (e.g. $100 / 3) by explicitly
 * allocating remainder cents to the payer (mitigating risk RSK-02).
 * 
 * LSP: Can substitute SplitStrategy without breaking behavior
 * OCP: Extends functionality without modifying interface
 */
public class EqualSplitStrategy implements SplitStrategy {
    
    @Override
    public ArrayList<SplitDetail> computeShares(double totalAmount, 
                                               List<String> participants, 
                                               List<Double> weights) {
        ArrayList<SplitDetail> splits = new ArrayList<>();
        
        if (participants == null || participants.isEmpty()) {
            return splits;
        }

        if (totalAmount <= 0) {
            for (String participant : participants) {
                splits.add(new SplitDetail(participant, 0.0));
            }
            return splits;
        }
        
        int count = participants.size();
        long totalCents = Math.round(totalAmount * 100.0);
        long baseCentsPerPerson = totalCents / count;
        long remainderCents = totalCents % count;
        
        for (int i = 0; i < count; i++) {
            // Explicitly allocate remainder cents to the payer (first participant) to prevent rounding drift
            long shareCents = (i == 0) ? (baseCentsPerPerson + remainderCents) : baseCentsPerPerson;
            double shareAmount = shareCents / 100.0;
            splits.add(new SplitDetail(participants.get(i), shareAmount));
        }
        
        return splits;
    }
    
    @Override
    public String getStrategyType() {
        return "EQUAL";
    }
}
