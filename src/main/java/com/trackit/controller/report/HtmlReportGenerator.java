package com.trackit.controller.report;

import com.trackit.model.Expense;

import java.io.BufferedWriter;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * HtmlReportGenerator - Generates a styled, browser-ready HTML report for financial expenses.
 * Adheres to Factory Method Pattern (implements ReportGenerator).
 */
public class HtmlReportGenerator implements ReportGenerator {

    private final String username;
    private final List<Expense> expenses;
    private String reportContent;

    public HtmlReportGenerator(String username, List<Expense> expenses) {
        this.username = username != null ? username : "anonymous";
        this.expenses = expenses != null ? new ArrayList<>(expenses) : Collections.emptyList();
    }

    private String escapeHtml(String text) {
        if (text == null) return "";
        return text.replace("&", "&amp;")
                   .replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#39;");
    }

    @Override
    public String generate() {
        double total = expenses.stream().mapToDouble(Expense::getAmount).sum();
        StringBuilder sb = new StringBuilder();

        sb.append("<!DOCTYPE html>\n<html lang=\"en\">\n<head>\n")
          .append("<meta charset=\"UTF-8\">\n")
          .append("<title>Trackit Financial Report</title>\n")
          .append("<style>\n")
          .append("body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #f8fafc; color: #1e293b; padding: 24px; }\n")
          .append(".container { max-width: 800px; margin: 0 auto; background: #ffffff; border-radius: 12px; box-shadow: 0 4px 6px -1px rgba(0,0,0,0.1); padding: 32px; }\n")
          .append("h1 { color: #0f172a; margin-bottom: 8px; }\n")
          .append(".meta { color: #64748b; font-size: 14px; margin-bottom: 24px; }\n")
          .append(".summary { display: flex; gap: 16px; margin-bottom: 24px; }\n")
          .append(".card { flex: 1; background: #f1f5f9; padding: 16px; border-radius: 8px; font-weight: bold; }\n")
          .append("table { width: 100%; border-collapse: collapse; margin-top: 16px; }\n")
          .append("th, td { text-align: left; padding: 12px; border-bottom: 1px solid #e2e8f0; }\n")
          .append("th { background: #f8fafc; color: #475569; font-size: 13px; text-transform: uppercase; }\n")
          .append(".badge { display: inline-block; padding: 4px 8px; border-radius: 6px; font-size: 12px; background: #e0e7ff; color: #3730a3; }\n")
          .append("</style>\n</head>\n<body>\n")
          .append("<div class=\"container\">\n")
          .append("<h1>Trackit Financial Report</h1>\n")
          .append("<div class=\"meta\">Generated for user: <strong>").append(escapeHtml(username)).append("</strong></div>\n")
          .append("<div class=\"summary\">\n")
          .append("<div class=\"card\">Total Spent: ").append(String.format(Locale.US, "$%.2f", total)).append("</div>\n")
          .append("<div class=\"card\">Transactions: ").append(expenses.size()).append("</div>\n")
          .append("</div>\n")
          .append("<table>\n<thead>\n<tr><th>Date</th><th>Category</th><th>Description</th><th>Amount</th></tr>\n</thead>\n<tbody>\n");

        for (Expense e : expenses) {
            String desc = e.getDescription() != null ? escapeHtml(e.getDescription()) : "";
            String category = e.getCategory() != null ? escapeHtml(e.getCategory()) : "UNCATEGORIZED";
            String dateStr = e.getDate() != null ? e.getDate().toString() : "";

            sb.append("<tr>")
              .append("<td>").append(dateStr).append("</td>")
              .append("<td><span class=\"badge\">").append(category).append("</span></td>")
              .append("<td>").append(desc).append("</td>")
              .append("<td>").append(String.format(Locale.US, "$%.2f", e.getAmount())).append("</td>")
              .append("</tr>\n");
        }

        sb.append("</tbody>\n</table>\n</div>\n</body>\n</html>");
        this.reportContent = sb.toString();
        return this.reportContent;
    }

    @Override
    public boolean saveToFile(String filepath) {
        if (reportContent == null) {
            generate();
        }
        try (BufferedWriter writer = new BufferedWriter(
                new OutputStreamWriter(new FileOutputStream(filepath), StandardCharsets.UTF_8))) {
            writer.write(reportContent);
            return true;
        } catch (IOException e) {
            System.err.println("Error saving HTML report: " + e.getMessage());
            return false;
        }
    }

    @Override
    public String getReportType() {
        return "HTML";
    }
}
