package com.trackit.model;

import com.trackit.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("RecurringExpense Domain Model & Cadence Tests")
class RecurringExpenseTest {

    @Test
    @DisplayName("Should create valid RecurringExpense with correct properties")
    void testValidRecurringExpenseCreation() {
        LocalDate start = LocalDate.of(2026, 1, 1);
        RecurringExpense re = new RecurringExpense(
                "shafiq", "RENT", 1200.0, "Monthly Apartment Rent",
                RecurringExpense.RecurrenceType.MONTHLY, 0, start
        );

        assertEquals("shafiq", re.getUsername());
        assertEquals("RENT", re.getCategory());
        assertEquals(1200.0, re.getAmount(), 0.001);
        assertEquals(RecurringExpense.RecurrenceType.MONTHLY, re.getRecurrenceType());
        assertEquals(start, re.getStartDate());
        assertTrue(re.isActive());
    }

    @Test
    @DisplayName("Should calculate next occurrence accurately for WEEKLY and MONTHLY")
    void testNextOccurrenceCalculation() {
        LocalDate start = LocalDate.of(2026, 2, 1);
        RecurringExpense monthly = new RecurringExpense(
                "shafiq", "SUBSCRIPTION", 15.0, "Streaming",
                RecurringExpense.RecurrenceType.MONTHLY, 0, start
        );
        assertEquals(LocalDate.of(2026, 3, 1), monthly.getNextOccurrence());

        RecurringExpense weekly = new RecurringExpense(
                "shafiq", "GROCERIES", 50.0, "Weekly groceries",
                RecurringExpense.RecurrenceType.WEEKLY, 0, start
        );
        assertEquals(LocalDate.of(2026, 2, 8), weekly.getNextOccurrence());
    }

    @Test
    @DisplayName("Should throw ValidationException when amount is zero or negative")
    void testInvalidAmountThrowsValidationException() {
        LocalDate start = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "FOOD", 0.0, "Free food",
                    RecurringExpense.RecurrenceType.WEEKLY, 0, start);
        });

        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "FOOD", -25.0, "Negative expense",
                    RecurringExpense.RecurrenceType.WEEKLY, 0, start);
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when username or category or startDate is invalid")
    void testInvalidFieldsThrowValidationException() {
        LocalDate start = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            new RecurringExpense(null, "FOOD", 10.0, "Valid",
                    RecurringExpense.RecurrenceType.WEEKLY, 0, start);
        });

        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "", 10.0, "Valid",
                    RecurringExpense.RecurrenceType.WEEKLY, 0, start);
        });

        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "FOOD", 10.0, "Valid",
                    RecurringExpense.RecurrenceType.WEEKLY, 0, null);
        });
    }

    @Test
    @DisplayName("Should throw ValidationException when CUSTOM recurrence has customDays <= 0")
    void testCustomRecurrenceValidation() {
        LocalDate start = LocalDate.now();
        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "UTILITIES", 60.0, "Internet",
                    RecurringExpense.RecurrenceType.CUSTOM, 0, start);
        });

        assertThrows(ValidationException.class, () -> {
            new RecurringExpense("shafiq", "UTILITIES", 60.0, "Internet",
                    RecurringExpense.RecurrenceType.CUSTOM, -5, start);
        });
    }
}
