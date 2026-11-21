package com.trackit.controller;

import com.trackit.model.Expense;
import com.trackit.model.ExpenseSearchCriteria;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpenseFilterPredicate Multi-Criteria Engine Tests")
class ExpenseFilterPredicateTest {

    private List<Expense> sampleExpenses;

    @BeforeEach
    void setUp() {
        sampleExpenses = new ArrayList<>();
        sampleExpenses.add(new Expense("FOOD", 25.50, "Team lunch", LocalDate.of(2026, 3, 5), "shafiq"));
        sampleExpenses.add(new Expense("TRANSPORT", 15.00, "Airport taxi", LocalDate.of(2026, 3, 10), "shafiq"));
        sampleExpenses.add(new Expense("UTILITIES", 120.00, "Electricity bill", LocalDate.of(2026, 3, 15), "shafiq"));
        sampleExpenses.add(new Expense("FOOD", 85.00, "Grocery shopping", LocalDate.of(2026, 3, 20), "shafiq"));
    }

    @Test
    @DisplayName("Should return all expenses when criteria is null or has no active filters")
    void testNoFilterReturnsAll() {
        List<Expense> resultNull = ExpenseFilterPredicate.filter(sampleExpenses, null);
        assertEquals(4, resultNull.size());

        ExpenseSearchCriteria emptyCriteria = new ExpenseSearchCriteria.Builder().build();
        List<Expense> resultEmpty = ExpenseFilterPredicate.filter(sampleExpenses, emptyCriteria);
        assertEquals(4, resultEmpty.size());
    }

    @Test
    @DisplayName("Should filter expenses accurately by category")
    void testFilterByCategory() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .category("FOOD")
                .build();

        List<Expense> filtered = ExpenseFilterPredicate.filter(sampleExpenses, criteria);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().allMatch(e -> e.getCategory().equalsIgnoreCase("FOOD")));
    }

    @Test
    @DisplayName("Should filter expenses accurately by amount range")
    void testFilterByAmountRange() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .minAmount(20.0)
                .maxAmount(100.0)
                .build();

        List<Expense> filtered = ExpenseFilterPredicate.filter(sampleExpenses, criteria);
        assertEquals(2, filtered.size());
        assertTrue(filtered.stream().anyMatch(e -> e.getDescription().equals("Team lunch")));
        assertTrue(filtered.stream().anyMatch(e -> e.getDescription().equals("Grocery shopping")));
    }

    @Test
    @DisplayName("Should filter expenses accurately by text search keyword")
    void testFilterByKeyword() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .keyword("taxi")
                .build();

        List<Expense> filtered = ExpenseFilterPredicate.filter(sampleExpenses, criteria);
        assertEquals(1, filtered.size());
        assertEquals("Airport taxi", filtered.get(0).getDescription());
    }

    @Test
    @DisplayName("Should filter expenses within strict date range")
    void testFilterByDateRange() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .startDate(LocalDate.of(2026, 3, 1))
                .endDate(LocalDate.of(2026, 3, 12))
                .build();

        List<Expense> filtered = ExpenseFilterPredicate.filter(sampleExpenses, criteria);
        assertEquals(2, filtered.size());
    }
}
