package com.trackit.controller.report;

import com.trackit.model.Expense;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * JsonReportGenerator - Exports financial transactions to structured JSON format.
 * Adheres to Factory Method Pattern (implements ReportGenerator).
 */
public class JsonReportGenerator implements ReportGenerator {
    
    private final String username;
    private final List<Expense> expenses;
    private String reportContent;
    
    public JsonReportGenerator(String username, List<Expense> expenses) {
        this.username = username != null ? username : "anonymous";
        this.expenses = expenses != null ? new ArrayList<>(expenses) : Collections.emptyList();
    }
    
    @Override
    public String generate() {
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"username\": \"").append(escapeJson(username)).append("\",\n");
        sb.append(String.format(Locale.US, "  \"totalExpenses\": %.2f,\n", total));
        sb.append("  \"count\": ").append(expenses.size()).append(",\n");
        sb.append("  \"items\": [\n");
        
        for (int i = 0; i < expenses.size(); i++) {
            Expense e = expenses.get(i);
            sb.append("    {\n");
            sb.append("      \"category\": \"").append(escapeJson(e.getCategory())).append("\",\n");
            sb.append(String.format(Locale.US, "      \"amount\": %.2f,\n", e.getAmount()));
            sb.append("      \"description\": \"").append(escapeJson(e.getDescription())).append("\",\n");
            sb.append("      \"date\": \"").append(e.getDate()).append("\"\n");
            sb.append("    }").append(i < expenses.size() - 1 ? "," : "").append("\n");
        }
        sb.append("  ]\n");
        sb.append("}");
        this.reportContent = sb.toString();
        return this.reportContent;
    }
    
    @Override
    public boolean saveToFile(String filepath) {
        if (reportContent == null) {
            generate();
        }
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filepath))) {
            writer.write(reportContent);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving JSON report: " + e.getMessage());
            return false;
        }
    }
    
    private String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
    
    @Override
    public String getReportType() {
        return "JSON";
    }
}
