package com.trackit.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * SharedExpense Model - Represents group shared expense.
 * Stores participant list, split strategy type, custom weights, and computed split details.
 */
public class SharedExpense implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String category;
    private double totalAmount;
    private String description;
    private LocalDate date;
    private String paidByUsername;
    private String groupName;
    private List<String> participantUserIds;
    private String splitStrategyType;
    private ArrayList<SplitDetail> splitDetails;
    private List<Double> weights;

    public SharedExpense(String category, double totalAmount, String description, 
                        LocalDate date, String paidByUsername, String groupName) {
        this.category = category;
        this.totalAmount = totalAmount;
        this.description = description;
        this.date = date;
        this.paidByUsername = paidByUsername;
        this.groupName = groupName;
        this.participantUserIds = new ArrayList<>();
        this.splitStrategyType = "EQUAL";
        this.splitDetails = new ArrayList<>();
        this.weights = new ArrayList<>();
    }

    // Getters and Setters
    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public String getPaidByUsername() {
        return paidByUsername;
    }

    public void setPaidByUsername(String paidByUsername) {
        this.paidByUsername = paidByUsername;
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public List<String> getParticipantUserIds() {
        return participantUserIds != null ? new ArrayList<>(participantUserIds) : new ArrayList<>();
    }

    public void setParticipantUserIds(List<String> participantUserIds) {
        this.participantUserIds = participantUserIds != null ? new ArrayList<>(participantUserIds) : new ArrayList<>();
    }

    public String getSplitStrategyType() {
        return splitStrategyType;
    }

    public void setSplitStrategyType(String splitStrategyType) {
        this.splitStrategyType = splitStrategyType;
    }

    public ArrayList<SplitDetail> getSplitDetails() {
        return splitDetails != null ? new ArrayList<>(splitDetails) : new ArrayList<>();
    }

    public void setSplitDetails(List<SplitDetail> splitDetails) {
        this.splitDetails = splitDetails != null ? new ArrayList<>(splitDetails) : new ArrayList<>();
    }

    public List<Double> getWeights() {
        return weights != null ? new ArrayList<>(weights) : new ArrayList<>();
    }

    public void setWeights(List<Double> weights) {
        this.weights = weights != null ? new ArrayList<>(weights) : new ArrayList<>();
    }
}
