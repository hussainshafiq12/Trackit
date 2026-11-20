package com.trackit.view;

import com.trackit.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ChartsDashboardPanel Spending Aggregation & Analytics Tests")
class ChartsDashboardPanelTest {

    private List<Expense> createSampleExpenses() {
        List<Expense> list = new ArrayList<>();
        list.add(new Expense("FOOD", 50.0, "Groceries", LocalDate.of(2026, 3, 10), "shafiq"));
        list.add(new Expense("FOOD", 30.0, "Dinner", LocalDate.of(2026, 3, 12), "shafiq"));
        list.add(new Expense("UTILITIES", 120.0, "Electricity", LocalDate.of(2026, 2, 5), "shafiq"));
        list.add(new Expense("ENTERTAINMENT", 40.0, "Movies", LocalDate.of(2026, 1, 15), "shafiq"));
        return list;
    }

    @Test
    @DisplayName("Should aggregate total spending accurately across all categories")
    void testComputeTotalSpending() {
        List<Expense> expenses = createSampleExpenses();
        double total = ChartsDashboardPanel.computeTotalSpending(expenses);
        assertEquals(240.0, total, 0.001);
    }

    @Test
    @DisplayName("Should group and sum expenses by category")
    void testComputeCategoryTotals() {
        List<Expense> expenses = createSampleExpenses();
        Map<String, Double> totals = ChartsDashboardPanel.computeCategoryTotals(expenses);

        assertEquals(3, totals.size());
        assertEquals(80.0, totals.get("FOOD"), 0.001);
        assertEquals(120.0, totals.get("UTILITIES"), 0.001);
        assertEquals(40.0, totals.get("ENTERTAINMENT"), 0.001);
    }

    @Test
    @DisplayName("Should compute chronological monthly totals within specified month window")
    void testComputeMonthlyTotals() {
        List<Expense> expenses = createSampleExpenses();
        Map<String, Double> monthly = ChartsDashboardPanel.computeMonthlyTotals(expenses, 6);

        assertNotNull(monthly);
        assertTrue(monthly.containsKey("2026-03"));
        assertEquals(80.0, monthly.get("2026-03"), 0.001);
        assertTrue(monthly.containsKey("2026-02"));
        assertEquals(120.0, monthly.get("2026-02"), 0.001);
    }

    @Test
    @DisplayName("Should handle empty and null expense lists safely without throwing exceptions")
    void testNullAndEmptyHandling() {
        assertEquals(0.0, ChartsDashboardPanel.computeTotalSpending(null));
        assertTrue(ChartsDashboardPanel.computeCategoryTotals(null).isEmpty());
        assertTrue(ChartsDashboardPanel.computeMonthlyTotals(null, 6).isEmpty());
    }
}
