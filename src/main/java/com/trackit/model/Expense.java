package com.trackit.model;

import com.trackit.exception.ValidationException;
import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;

/**
 * Represents a personal expense with defensive boundary validation.
 */
public class Expense implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String category;
    private double amount;
    private String description;
    private LocalDate date;
    private String username;
    private boolean isSplit;
    private ArrayList<SplitDetail> splitDetails;
    private Receipt receipt;

    public Expense(String category, double amount, String description, LocalDate date, String username) {
        setCategory(category);
        setAmount(amount);
        setDescription(description);
        setDate(date);
        setUsername(username);
        this.isSplit = false;
        this.splitDetails = new ArrayList<>();
        this.receipt = null;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        if (category == null || category.trim().isEmpty()) {
            throw new ValidationException("Category cannot be null or empty");
        }
        this.category = category.trim();
    }

    public double getAmount() {
        return amount;
    }

    public void setAmount(double amount) {
        if (amount <= 0.0) {
            throw new ValidationException("Expense amount must be strictly greater than zero");
        }
        this.amount = amount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        if (description == null || description.trim().isEmpty()) {
            throw new ValidationException("Description cannot be null or empty");
        }
        this.description = description.trim();
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        if (date == null) {
            throw new ValidationException("Date cannot be null");
        }
        this.date = date;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = (username != null) ? username.trim() : null;
    }

    public boolean isSplit() {
        return isSplit;
    }

    public void setSplit(boolean split) {
        isSplit = split;
    }

    public ArrayList<SplitDetail> getSplitDetails() {
        return splitDetails != null ? new ArrayList<>(splitDetails) : new ArrayList<>();
    }

    public void setSplitDetails(ArrayList<SplitDetail> splitDetails) {
        this.splitDetails = splitDetails != null ? new ArrayList<>(splitDetails) : new ArrayList<>();
    }
    
    public Receipt getReceipt() {
        return receipt;
    }
    
    public void setReceipt(Receipt receipt) {
        this.receipt = receipt;
    }
    
    public boolean hasReceipt() {
        return receipt != null;
    }
}
