package com.trackit.controller.report;

import com.trackit.controller.ExpenseManager;
import com.trackit.controller.GroupController;
import com.trackit.model.Expense;
import com.trackit.model.Group;
import com.trackit.model.SharedExpense;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * FACTORY METHOD PATTERN - ReportFactory
 * Encapsulates instantiation behind a common ReportGenerator interface.
 * Extensible for new report formats (CSV, JSON, PDF) without modifying client code (OCP).
 */
public class ReportFactory {
    
    /**
     * Create a personal expense text report.
     */
    public static ReportGenerator createPersonalReport(String username, 
                                                      ExpenseManager expenseManager) {
        ArrayList<Expense> expenses = expenseManager.getAllExpenses(username);
        return new PersonalReportGenerator(username, expenses);
    }
    
    /**
     * Create a group expense text report.
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
     * Create a CSV format financial export.
     */
    public static ReportGenerator createCsvReport(String username, List<Expense> expenses) {
        return new CsvReportGenerator(username, expenses);
    }

    /**
     * Create a JSON format financial export.
     */
    public static ReportGenerator createJsonReport(String username, List<Expense> expenses) {
        return new JsonReportGenerator(username, expenses);
    }

    /**
     * Create an HTML format financial export.
     */
    public static ReportGenerator createHtmlReport(String username, List<Expense> expenses) {
        return new HtmlReportGenerator(username, expenses);
    }
    
    /**
     * Create report based on type string.
     * Factory Method: Determines which concrete generator to instantiate.
     */
    @SuppressWarnings("unchecked")
    public static ReportGenerator createReport(String reportType, String identifier, 
                                              Object dataSource) {
        if (reportType == null || reportType.trim().isEmpty()) {
            throw new IllegalArgumentException("Report type cannot be null or empty");
        }
        switch (reportType.trim().toUpperCase()) {
            case "PERSONAL":
                if (dataSource instanceof ExpenseManager) {
                    return createPersonalReport(identifier, (ExpenseManager) dataSource);
                }
                throw new IllegalArgumentException("Invalid controller for personal report");
                
            case "GROUP":
                if (dataSource instanceof GroupController) {
                    return createGroupReport(identifier, (GroupController) dataSource);
                }
                throw new IllegalArgumentException("Invalid controller for group report");

            case "CSV":
                if (dataSource instanceof List) {
                    return createCsvReport(identifier, (List<Expense>) dataSource);
                } else if (dataSource instanceof ExpenseManager) {
                    return createCsvReport(identifier, ((ExpenseManager) dataSource).getAllExpenses(identifier));
                }
                throw new IllegalArgumentException("Invalid data source for CSV report");

            case "JSON":
                if (dataSource instanceof List) {
                    return createJsonReport(identifier, (List<Expense>) dataSource);
                } else if (dataSource instanceof ExpenseManager) {
                    return createJsonReport(identifier, ((ExpenseManager) dataSource).getAllExpenses(identifier));
                }
                throw new IllegalArgumentException("Invalid data source for JSON report");

            case "HTML":
                if (dataSource instanceof List) {
                    return createHtmlReport(identifier, (List<Expense>) dataSource);
                } else if (dataSource instanceof ExpenseManager) {
                    return createHtmlReport(identifier, ((ExpenseManager) dataSource).getAllExpenses(identifier));
                }
                throw new IllegalArgumentException("Invalid data source for HTML report");
                
            default:
                throw new IllegalArgumentException("Unknown report type: " + reportType);
        }
    }
}
