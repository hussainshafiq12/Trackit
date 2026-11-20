package com.trackit.model;

import com.trackit.exception.ValidationException;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * RecurringExpense - Model for expenses that repeat automatically.
 * Supports weekly, monthly, and custom interval recurrence.
 * Defensively validates inputs and calculates future cadence.
 */
public class RecurringExpense implements Serializable {
    private static final long serialVersionUID = 1L;
    
    public enum RecurrenceType {
        WEEKLY("Weekly"),
        MONTHLY("Monthly"),
        CUSTOM("Custom Days");
        
        private final String displayName;
        
        RecurrenceType(String displayName) {
            this.displayName = displayName;
        }
        
        public String getDisplayName() {
            return displayName;
        }
    }
    
    private int id;
    private String username;
    private String category;
    private double amount;
    private String description;
    private RecurrenceType recurrenceType;
    private int customDays; // For custom recurrence
    private LocalDate startDate;
    private LocalDate lastGenerated;
    private boolean active;
    
    public RecurringExpense(String username, String category, double amount, 
                           String description, RecurrenceType recurrenceType, 
                           int customDays, LocalDate startDate) {
        validateInputs(username, category, amount, recurrenceType, customDays, startDate);
        
        this.username = username.trim();
        this.category = category.trim();
        this.amount = amount;
        this.description = description != null ? description.trim() : "";
        this.recurrenceType = recurrenceType;
        this.customDays = customDays;
        this.startDate = startDate;
        this.lastGenerated = startDate;
        this.active = true;
        this.id = generateId();
    }
    
    private void validateInputs(String username, String category, double amount, 
                                RecurrenceType recurrenceType, int customDays, LocalDate startDate) {
        if (username == null || username.trim().isEmpty()) {
            throw new ValidationException("Username cannot be empty");
        }
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty");
        }
        if (amount <= 0) {
            throw new ValidationException("Recurring expense amount must be greater than zero");
        }
        if (recurrenceType == null) {
            throw new ValidationException("Recurrence type cannot be null");
        }
        if (recurrenceType == RecurrenceType.CUSTOM && customDays <= 0) {
            throw new ValidationException("Custom recurrence requires customDays > 0");
        }
        if (startDate == null) {
            throw new ValidationException("Start date cannot be null");
        }
    }
    
    private int generateId() {
        return (username + category + startDate.toString()).hashCode();
    }
    
    /**
     * Calculate next occurrence date based on recurrence type
     */
    public LocalDate getNextOccurrence() {
        switch (recurrenceType) {
            case WEEKLY:
                return lastGenerated.plusWeeks(1);
            case MONTHLY:
                return lastGenerated.plusMonths(1);
            case CUSTOM:
                return lastGenerated.plusDays(customDays);
            default:
                return lastGenerated.plusDays(1);
        }
    }
    
    /**
     * Check if expense should be generated today
     */
    public boolean shouldGenerateToday() {
        if (!active) return false;
        LocalDate today = LocalDate.now();
        LocalDate nextDate = getNextOccurrence();
        return !today.isBefore(nextDate);
    }
    
    /**
     * Create expense instance from this recurring template
     */
    public Expense createExpenseInstance(LocalDate date) {
        return new Expense(category, amount, description, date, username);
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public String getUsername() {
        return username;
    }
    
    public String getCategory() {
        return category;
    }
    
    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be empty");
        }
        this.category = category.trim();
    }
    
    public double getAmount() {
        return amount;
    }
    
    public void setAmount(double amount) {
        if (amount <= 0) {
            throw new ValidationException("Amount must be greater than zero");
        }
        this.amount = amount;
    }
    
    public String getDescription() {
        return description;
    }
    
    public void setDescription(String description) {
        this.description = description != null ? description.trim() : "";
    }
    
    public RecurrenceType getRecurrenceType() {
        return recurrenceType;
    }
    
    public void setRecurrenceType(RecurrenceType recurrenceType) {
        if (recurrenceType == null) {
            throw new ValidationException("Recurrence type cannot be null");
        }
        this.recurrenceType = recurrenceType;
    }
    
    public int getCustomDays() {
        return customDays;
    }
    
    public void setCustomDays(int customDays) {
        if (recurrenceType == RecurrenceType.CUSTOM && customDays <= 0) {
            throw new ValidationException("Custom recurrence requires customDays > 0");
        }
        this.customDays = customDays;
    }
    
    public LocalDate getStartDate() {
        return startDate;
    }
    
    public LocalDate getLastGenerated() {
        return lastGenerated;
    }
    
    public void setLastGenerated(LocalDate lastGenerated) {
        this.lastGenerated = lastGenerated;
    }
    
    public boolean isActive() {
        return active;
    }
    
    public void setActive(boolean active) {
        this.active = active;
    }
}
