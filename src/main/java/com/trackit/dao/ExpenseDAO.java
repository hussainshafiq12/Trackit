package com.trackit.dao;

import com.trackit.model.Expense;
import java.util.ArrayList;

/**
 * Data Access Object for Expense operations.
 * Implements file-based object serialization (.dat files) via DatabaseManager singleton.
 */
public class ExpenseDAO {
    private final DatabaseManager dbManager;
    
    public ExpenseDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    private String getUserExpenseKey(String username) {
        return "expenses_" + (username != null ? username.trim() : "anonymous");
    }
    
    @SuppressWarnings("unchecked")
    public ArrayList<Expense> loadExpenses(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }
        String key = getUserExpenseKey(username);
        ArrayList<Expense> expenses = dbManager.loadData(key, ArrayList.class);
        return expenses != null ? new ArrayList<>(expenses) : new ArrayList<>();
    }
    
    public void saveExpenses(String username, ArrayList<Expense> expenses) {
        if (username == null || username.trim().isEmpty()) {
            return;
        }
        String key = getUserExpenseKey(username);
        dbManager.saveData(key, expenses != null ? new ArrayList<>(expenses) : new ArrayList<>());
    }
    
    public void addExpense(String username, Expense expense) {
        if (username == null || expense == null) {
            return;
        }
        ArrayList<Expense> expenses = loadExpenses(username);
        expenses.add(expense);
        saveExpenses(username, expenses);
    }
    
    public void deleteExpense(String username, Expense expense) {
        if (username == null || expense == null) {
            return;
        }
        ArrayList<Expense> expenses = loadExpenses(username);
        expenses.removeIf(e -> 
            e.getCategory().equalsIgnoreCase(expense.getCategory()) &&
            Math.abs(e.getAmount() - expense.getAmount()) < 0.01 &&
            e.getDate().equals(expense.getDate()) &&
            e.getDescription().equals(expense.getDescription())
        );
        saveExpenses(username, expenses);
    }
    
    public void updateExpense(String username, Expense oldExpense, Expense newExpense) {
        if (username == null || oldExpense == null || newExpense == null) {
            return;
        }
        ArrayList<Expense> expenses = loadExpenses(username);
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            if (e.getCategory().equalsIgnoreCase(oldExpense.getCategory()) &&
                Math.abs(e.getAmount() - oldExpense.getAmount()) < 0.01 &&
                e.getDate().equals(oldExpense.getDate())) {
                expenses.set(i, newExpense);
                break;
            }
        }
        saveExpenses(username, expenses);
    }
    
    public void clearAllExpenses(String username) {
        if (username == null) return;
        String key = getUserExpenseKey(username);
        dbManager.deleteData(key);
    }
}
