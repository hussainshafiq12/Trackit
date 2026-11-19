package com.trackit.controller;

import com.trackit.controller.strategy.*;
import com.trackit.dao.GroupDAO;
import com.trackit.model.Group;
import com.trackit.model.SharedExpense;
import com.trackit.model.SplitDetail;

import java.time.LocalDate;
import java.util.*;

/**
 * GroupController - Coordinates group creation, member management,
 * shared expense recording, and runtime selection of split strategies.
 * 
 * Implements Controller / Business Logic layer in adherence with Single Responsibility
 * and Open-Closed Principles.
 */
public class GroupController {
    private final GroupDAO groupDAO;
    private final Map<String, SplitStrategy> strategies;
    
    public GroupController() {
        this.groupDAO = new GroupDAO();
        this.strategies = new HashMap<>();
        initializeStrategies();
    }

    public GroupController(GroupDAO groupDAO) {
        this.groupDAO = groupDAO;
        this.strategies = new HashMap<>();
        initializeStrategies();
    }
    
    private void initializeStrategies() {
        registerStrategy(new EqualSplitStrategy());
        registerStrategy(new WeightedSplitStrategy());
        registerStrategy(new PercentageSplitStrategy());
    }

    /**
     * Runtime registration of pluggable split strategies (OCP adherence).
     */
    public void registerStrategy(SplitStrategy strategy) {
        if (strategy != null && strategy.getStrategyType() != null) {
            strategies.put(strategy.getStrategyType().toUpperCase(), strategy);
        }
    }

    /**
     * Runtime resolution of split strategy. Defaults to EQUAL if not found.
     */
    public SplitStrategy getStrategy(String strategyType) {
        if (strategyType == null) {
            return strategies.get("EQUAL");
        }
        return strategies.getOrDefault(strategyType.toUpperCase(), strategies.get("EQUAL"));
    }
    
    /**
     * Create a new expense group with the specified creator.
     */
    public boolean createGroup(String groupName, String createdBy) {
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (createdBy == null || createdBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Creator username cannot be empty");
        }
        Group group = new Group(groupName.trim(), createdBy.trim());
        return groupDAO.createGroup(group);
    }
    
    /**
     * Add a new member to an existing group.
     */
    public boolean addMemberToGroup(String groupName, String username) {
        if (groupName == null || username == null || username.trim().isEmpty()) {
            return false;
        }
        Group group = groupDAO.getGroup(groupName);
        if (group == null) {
            return false;
        }
        if (group.getMembers().contains(username.trim())) {
            return false;
        }
        group.addMember(username.trim());
        groupDAO.updateGroup(group);
        return true;
    }
    
    /**
     * Remove a member from a group.
     */
    public boolean removeMemberFromGroup(String groupName, String username) {
        if (groupName == null || username == null) {
            return false;
        }
        Group group = groupDAO.getGroup(groupName);
        if (group == null) {
            return false;
        }
        if (!group.getMembers().contains(username.trim())) {
            return false;
        }
        group.removeMember(username.trim());
        groupDAO.updateGroup(group);
        return true;
    }
    
    public ArrayList<Group> getUserGroups(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }
        return groupDAO.getUserGroups(username.trim());
    }
    
    public Group getGroup(String groupName) {
        if (groupName == null) return null;
        return groupDAO.getGroup(groupName);
    }
    
    /**
     * Records a shared expense in the group with dynamic split strategy computation.
     */
    public void addSharedExpense(String category, double totalAmount, String description,
                                 String paidBy, String groupName, String strategyType,
                                 List<String> participants, List<Double> weights) {
        
        if (totalAmount <= 0) {
            throw new IllegalArgumentException("Expense amount must be strictly greater than zero");
        }
        if (groupName == null || groupName.trim().isEmpty()) {
            throw new IllegalArgumentException("Group name cannot be empty");
        }
        if (paidBy == null || paidBy.trim().isEmpty()) {
            throw new IllegalArgumentException("Payer username cannot be empty");
        }
        if (participants == null || participants.isEmpty()) {
            throw new IllegalArgumentException("Participants list cannot be empty");
        }
        
        Group group = groupDAO.getGroup(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }
        
        SplitStrategy strategy = getStrategy(strategyType);
        ArrayList<SplitDetail> splitDetails = strategy.computeShares(totalAmount, participants, weights);
        
        SharedExpense expense = new SharedExpense(category, totalAmount, description,
                                                 LocalDate.now(), paidBy.trim(), groupName.trim());
        expense.setParticipantUserIds(new ArrayList<>(participants));
        expense.setSplitStrategyType(strategy.getStrategyType());
        
        groupDAO.addSharedExpense(expense);
    }
    
    public ArrayList<SharedExpense> getGroupExpenses(String groupName) {
        if (groupName == null) return new ArrayList<>();
        return groupDAO.getGroupExpenses(groupName);
    }
    
    /**
     * Computes net member balances for all participants across group shared expenses.
     * Positive = owed money; Negative = owes money.
     */
    public Map<String, Double> calculateGroupBalances(String groupName) {
        Map<String, Double> balances = new HashMap<>();
        if (groupName == null) return balances;
        
        Group group = groupDAO.getGroup(groupName);
        if (group == null) return balances;
        
        for (String member : group.getMembers()) {
            balances.put(member, 0.0);
        }
        
        ArrayList<SharedExpense> expenses = groupDAO.getGroupExpenses(groupName);
        for (SharedExpense expense : expenses) {
            String paidBy = expense.getPaidByUsername();
            double totalAmount = expense.getTotalAmount();
            List<String> participants = expense.getParticipantUserIds();
            
            SplitStrategy strategy = getStrategy(expense.getSplitStrategyType());
            ArrayList<SplitDetail> shares = strategy.computeShares(totalAmount, participants, null);
            
            balances.put(paidBy, balances.getOrDefault(paidBy, 0.0) + totalAmount);
            
            for (SplitDetail share : shares) {
                String person = share.getPersonName();
                double amount = share.getAmount();
                balances.put(person, balances.getOrDefault(person, 0.0) - amount);
            }
        }
        
        // Round all balances to exact cents to prevent floating point inaccuracies
        for (Map.Entry<String, Double> entry : balances.entrySet()) {
            double rounded = Math.round(entry.getValue() * 100.0) / 100.0;
            balances.put(entry.getKey(), rounded);
        }
        
        return balances;
    }
    
    /**
     * Uses SettlementOptimizer (Min-Cash-Flow Greedy Algorithm) to calculate
     * the optimal minimal transactions to clear all debts within the group.
     */
    public List<SettlementOptimizer.Transaction> getOptimizedSettlement(String groupName) {
        Map<String, Double> balances = calculateGroupBalances(groupName);
        return SettlementOptimizer.optimizeSettlement(balances);
    }
    
    public List<String> getAvailableStrategies() {
        return new ArrayList<>(strategies.keySet());
    }
    
    public void deleteGroup(String groupName) {
        if (groupName != null) {
            groupDAO.deleteGroup(groupName);
        }
    }
}
