package com.trackit.controller.report;

import com.trackit.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Multi-Format ReportFactory & Exporter Tests (Factory Method Pattern)")
class MultiFormatReportTest {

    private ArrayList<Expense> createSampleExpenses() {
        ArrayList<Expense> list = new ArrayList<>();
        list.add(new Expense("FOOD", 45.50, "Team lunch", LocalDate.of(2026, 3, 1), "shafiq"));
        list.add(new Expense("TRANSPORT", 15.00, "Taxi ride", LocalDate.of(2026, 3, 2), "shafiq"));
        return list;
    }

    @Test
    @DisplayName("Should create CsvReportGenerator and generate valid comma-separated format")
    void testCsvReportGenerator() {
        ArrayList<Expense> expenses = createSampleExpenses();
        ReportGenerator generator = ReportFactory.createCsvReport("shafiq", expenses);

        assertNotNull(generator);
        assertTrue(generator instanceof CsvReportGenerator);
        assertEquals("CSV", generator.getReportType());

        String content = generator.generate();
        assertTrue(content.contains("Date,Category,Description,Amount,User"));
        assertTrue(content.contains("2026-03-01,FOOD,\"Team lunch\",45.50,shafiq"));
        assertTrue(content.contains("2026-03-02,TRANSPORT,\"Taxi ride\",15.00,shafiq"));
    }

    @Test
    @DisplayName("Should create JsonReportGenerator and generate valid JSON structure")
    void testJsonReportGenerator() {
        ArrayList<Expense> expenses = createSampleExpenses();
        ReportGenerator generator = ReportFactory.createJsonReport("shafiq", expenses);

        assertNotNull(generator);
        assertTrue(generator instanceof JsonReportGenerator);
        assertEquals("JSON", generator.getReportType());

        String content = generator.generate();
        assertTrue(content.startsWith("{") && content.endsWith("}"));
        assertTrue(content.contains("\"username\": \"shafiq\""));
        assertTrue(content.contains("\"totalExpenses\": 60.50"));
        assertTrue(content.contains("\"items\": ["));
        assertTrue(content.contains("\"category\": \"FOOD\""));
    }

    @Test
    @DisplayName("Should dynamically instantiate CSV and JSON via generic factory method (OCP)")
    void testFactoryGenericCreation() {
        ArrayList<Expense> expenses = createSampleExpenses();
        ReportGenerator csvGen = ReportFactory.createReport("CSV", "shafiq", expenses);
        assertEquals("CSV", csvGen.getReportType());

        ReportGenerator jsonGen = ReportFactory.createReport("JSON", "shafiq", expenses);
        assertEquals("JSON", jsonGen.getReportType());
    }

    @Test
    @DisplayName("Should sanitize multiline descriptions in CSV export into a single row")
    void testCsvReportHandlesMultilineDescription() {
        ArrayList<Expense> expenses = new ArrayList<>();
        expenses.add(new Expense("FOOD", 25.0, "Line1\nLine2\r\nLine3", LocalDate.of(2026, 3, 5), "shafiq"));
        ReportGenerator generator = ReportFactory.createCsvReport("shafiq", expenses);
        String content = generator.generate();

        assertFalse(content.contains("Line1\nLine2"));
        assertFalse(content.contains("\r"));
        assertTrue(content.contains("\"Line1 Line2 Line3\""));
    }

    @Test
    @DisplayName("Should safely handle null category and null date in JSON export")
    void testJsonReportHandlesNullCategoryAndDate() {
        ArrayList<Expense> expenses = new ArrayList<>();
        Expense expWithNulls = new Expense("FOOD", 12.0, "Misc item", LocalDate.of(2026, 3, 1), "shafiq") {
            @Override
            public String getCategory() {
                return null;
            }
            @Override
            public LocalDate getDate() {
                return null;
            }
        };
        expenses.add(expWithNulls);
        ReportGenerator generator = ReportFactory.createJsonReport("shafiq", expenses);
        String content = generator.generate();

        assertNotNull(content);
        assertTrue(content.contains("\"category\": \"UNCATEGORIZED\""));
        assertTrue(content.contains("\"date\": \"\""));
    }
}
