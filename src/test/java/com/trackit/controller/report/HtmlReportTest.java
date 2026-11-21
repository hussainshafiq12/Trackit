package com.trackit.controller.report;

import com.trackit.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("HtmlReportGenerator & Factory Registration Tests")
class HtmlReportTest {

    private List<Expense> createSampleExpenses() {
        List<Expense> list = new ArrayList<>();
        list.add(new Expense("FOOD", 35.00, "Team Dinner", LocalDate.of(2026, 3, 1), "shafiq"));
        list.add(new Expense("TRANSPORT", 12.50, "Metro Card", LocalDate.of(2026, 3, 2), "shafiq"));
        return list;
    }

    @Test
    @DisplayName("Should generate valid HTML structure with styled summary and expense rows")
    void testHtmlReportStructure() {
        List<Expense> expenses = createSampleExpenses();
        ReportGenerator generator = ReportFactory.createHtmlReport("shafiq", expenses);

        assertNotNull(generator);
        assertEquals("HTML", generator.getReportType());

        String html = generator.generate();
        assertTrue(html.contains("<!DOCTYPE html>"));
        assertTrue(html.contains("<html"));
        assertTrue(html.contains("Trackit Financial Report"));
        assertTrue(html.contains("shafiq"));
        assertTrue(html.contains("$47.50"));
        assertTrue(html.contains("Team Dinner"));
        assertTrue(html.contains("Metro Card"));
        assertTrue(html.contains("</table>"));
    }

    @Test
    @DisplayName("Should instantiate HtmlReportGenerator via generic ReportFactory method")
    void testGenericFactoryCreation() {
        List<Expense> expenses = createSampleExpenses();
        ReportGenerator generator = ReportFactory.createReport("HTML", "shafiq", expenses);

        assertNotNull(generator);
        assertTrue(generator instanceof HtmlReportGenerator);
        assertEquals("HTML", generator.getReportType());
    }

    @Test
    @DisplayName("Should safely escape special HTML characters in descriptions and categories")
    void testHtmlEscapesSpecialCharacters() {
        List<Expense> list = new ArrayList<>();
        list.add(new Expense("FOOD & DRINKS", 25.0, "Dinner <Team & Friends>", LocalDate.of(2026, 3, 1), "shafiq"));
        ReportGenerator generator = ReportFactory.createHtmlReport("shafiq", list);
        String html = generator.generate();

        assertTrue(html.contains("FOOD &amp; DRINKS"));
        assertTrue(html.contains("Dinner &lt;Team &amp; Friends&gt;"));
        assertFalse(html.contains("<Team & Friends>"));
    }
}
