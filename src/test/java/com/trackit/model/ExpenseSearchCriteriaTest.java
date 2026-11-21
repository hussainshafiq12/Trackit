package com.trackit.model;

import com.trackit.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ExpenseSearchCriteria Domain Model & Builder Tests")
class ExpenseSearchCriteriaTest {

    @Test
    @DisplayName("Should create empty criteria with default unconstrained values")
    void testDefaultCriteria() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder().build();
        assertNull(criteria.getStartDate());
        assertNull(criteria.getEndDate());
        assertNull(criteria.getCategory());
        assertNull(criteria.getKeyword());
        assertEquals(0.0, criteria.getMinAmount());
        assertEquals(Double.MAX_VALUE, criteria.getMaxAmount());
        assertFalse(criteria.hasActiveFilters());
    }

    @Test
    @DisplayName("Should construct fully populated search criteria via fluent builder")
    void testPopulatedCriteria() {
        LocalDate start = LocalDate.of(2026, 3, 1);
        LocalDate end = LocalDate.of(2026, 3, 31);

        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .startDate(start)
                .endDate(end)
                .category("FOOD")
                .minAmount(10.0)
                .maxAmount(200.0)
                .keyword("lunch")
                .build();

        assertEquals(start, criteria.getStartDate());
        assertEquals(end, criteria.getEndDate());
        assertEquals("FOOD", criteria.getCategory());
        assertEquals(10.0, criteria.getMinAmount());
        assertEquals(200.0, criteria.getMaxAmount());
        assertEquals("lunch", criteria.getKeyword());
        assertTrue(criteria.hasActiveFilters());
    }

    @Test
    @DisplayName("Should throw ValidationException when minAmount is greater than maxAmount")
    void testInvalidAmountRangeThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            new ExpenseSearchCriteria.Builder()
                    .minAmount(100.0)
                    .maxAmount(50.0)
                    .build();
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when minAmount is negative")
    void testNegativeMinAmountThrowsValidationException() {
        assertThrows(ValidationException.class, () -> {
            new ExpenseSearchCriteria.Builder()
                    .minAmount(-15.0)
                    .build();
        });
    }

    @Test
    @DisplayName("Should treat whitespace-only keyword and category as unconstrained null filters")
    void testWhitespaceKeywordAndCategorySanitization() {
        ExpenseSearchCriteria criteria = new ExpenseSearchCriteria.Builder()
                .keyword("   ")
                .category("   ")
                .build();

        assertNull(criteria.getKeyword());
        assertNull(criteria.getCategory());
        assertFalse(criteria.hasActiveFilters());
    }

    @Test
    @DisplayName("Should throw ValidationException when startDate is after endDate")
    void testInvalidDateRangeThrowsValidationException() {
        LocalDate start = LocalDate.of(2026, 4, 1);
        LocalDate end = LocalDate.of(2026, 3, 1);

        assertThrows(ValidationException.class, () -> {
            new ExpenseSearchCriteria.Builder()
                    .startDate(start)
                    .endDate(end)
                    .build();
        });
    }
}
