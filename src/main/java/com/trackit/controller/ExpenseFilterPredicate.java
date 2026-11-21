package com.trackit.controller;

import com.trackit.model.Expense;
import com.trackit.model.ExpenseSearchCriteria;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * ExpenseFilterPredicate - Evaluates multi-criteria queries against expense collections.
 * Supports filtering by category, date range, amount threshold, and keywords.
 */
public class ExpenseFilterPredicate {

    /**
     * Filters a list of expenses matching all active search criteria.
     *
     * @param expenses the source list of expenses
     * @param criteria search constraints to apply
     * @return filtered list of matching expenses
     */
    public static List<Expense> filter(List<Expense> expenses, ExpenseSearchCriteria criteria) {
        if (expenses == null || expenses.isEmpty()) {
            return Collections.emptyList();
        }
        if (criteria == null || !criteria.hasActiveFilters()) {
            return new ArrayList<>(expenses);
        }

        List<Expense> matched = new ArrayList<>();
        for (Expense expense : expenses) {
            if (matches(expense, criteria)) {
                matched.add(expense);
            }
        }
        return matched;
    }

    private static boolean matches(Expense expense, ExpenseSearchCriteria criteria) {
        if (expense == null) {
            return false;
        }

        // Check start date constraint
        if (criteria.getStartDate() != null) {
            LocalDate date = expense.getDate();
            if (date == null || !date.isAfter(criteria.getStartDate())) {
                return false;
            }
        }

        // Check end date constraint
        if (criteria.getEndDate() != null) {
            LocalDate date = expense.getDate();
            if (date == null || date.isAfter(criteria.getEndDate())) {
                return false;
            }
        }

        // Check category constraint
        if (criteria.getCategory() != null) {
            if (expense.getCategory() == null || 
                !expense.getCategory().equalsIgnoreCase(criteria.getCategory())) {
                return false;
            }
        }

        // Check amount constraints
        double amount = expense.getAmount();
        if (amount < criteria.getMinAmount() || amount > criteria.getMaxAmount()) {
            return false;
        }

        // Check keyword constraint
        if (criteria.getKeyword() != null) {
            String desc = expense.getDescription();
            if (desc == null || !desc.toLowerCase().contains(criteria.getKeyword().toLowerCase())) {
                return false;
            }
        }

        return true;
    }
}
