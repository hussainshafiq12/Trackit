package com.trackit.controller.report;

import com.trackit.controller.ExpenseManager;
import com.trackit.controller.GroupController;
import com.trackit.model.Expense;
import com.trackit.model.Group;
import com.trackit.model.SharedExpense;
import java.util.ArrayList;
import java.util.Map;

/**
 * FACTORY METHOD PATTERN - ReportFactory
 * Problem Solved: Need flexible instantiation of various report formats
 * Benefit: Open for new report types without modifying client code (OCP)
 * 
 * Creational Pattern: Encapsulates object creation logic
 */
public class ReportFactory {
    
    /**
     * Create a personal expense report
     * Factory Method: Creates appropriate report generator
     */
    public static ReportGenerator createPersonalReport(String username, 
                                                      ExpenseManager expenseManager) {
        ArrayList<Expense> expenses = expenseManager.getAllExpenses(username);
        return new PersonalReportGenerator(username, expenses);
    }
    
    /**
     * Create a group expense report
     * Factory Method: Creates appropriate report generator
     */
    public static ReportGenerator createGroupReport(String groupName, 
                                                    GroupController groupController) {
        Group group = groupController.getGroup(groupName);
        if (group == null) {
            throw new IllegalArgumentException("Group not found: " + groupName);
        }
        
        ArrayList<SharedExpense> expenses = groupController.getGroupExpenses(groupName);
        Map<String, Double> balances = groupController.calculateGroupBalances(groupName);
        
        return new GroupReportGenerator(group, expenses, balances);
    }
    
    /**
     * Create report based on type string
     * Factory Method: Determines which report to create
     */
    public static ReportGenerator createReport(String reportType, String identifier, 
                                              Object controller) {
        if (reportType == null || reportType.trim().isEmpty()) {
            throw new IllegalArgumentException("Report type cannot be null or empty");
        }
        switch (reportType.trim().toUpperCase()) {
            case "PERSONAL":
                if (controller instanceof ExpenseManager) {
                    return createPersonalReport(identifier, (ExpenseManager) controller);
                }
                throw new IllegalArgumentException("Invalid controller for personal report");
                
            case "GROUP":
                if (controller instanceof GroupController) {
                    return createGroupReport(identifier, (GroupController) controller);
                }
                throw new IllegalArgumentException("Invalid controller for group report");
                
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }
}