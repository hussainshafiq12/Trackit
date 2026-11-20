package com.trackit.dao;

import com.trackit.model.Budget;

/**
 * BudgetDAO - Data Access Object for Budget operations.
 * Handles persistence of user budget settings using file-based serialization (.dat files).
 */
public class BudgetDAO {
    private final DatabaseManager dbManager;
    
    public BudgetDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    private String getBudgetKey(String username) {
        return "budgets_" + (username != null ? username.trim() : "anonymous");
    }
    
    /**
     * Load budget for a user
     */
    public Budget loadBudget(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        String key = getBudgetKey(username);
        return dbManager.loadData(key, Budget.class);
    }
    
    /**
     * Save budget for a user
     */
    public void saveBudget(Budget budget) {
        if (budget == null || budget.getUsername() == null) {
            return;
        }
        String key = getBudgetKey(budget.getUsername());
        dbManager.saveData(key, budget);
    }
    
    /**
     * Check if user has budget configured
     */
    public boolean hasBudget(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        String key = getBudgetKey(username);
        return dbManager.dataExists(key);
    }
    
    /**
     * Delete budget for a user
     */
    public void deleteBudget(String username) {
        if (username == null) return;
        String key = getBudgetKey(username);
        dbManager.deleteData(key);
    }
}
