package com.trackit.model;

import com.trackit.exception.ValidationException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Expense and User Defensive Validation Tests")
public class ExpenseValidationTest {

    @Test
    @DisplayName("Valid expense creation should succeed")
    void testValidExpenseCreation() {
        Expense expense = new Expense("FOOD", 45.50, "Dinner with team", LocalDate.now(), "shafiq");
        assertEquals("FOOD", expense.getCategory());
        assertEquals(45.50, expense.getAmount());
        assertEquals("Dinner with team", expense.getDescription());
        assertEquals("shafiq", expense.getUsername());
        assertFalse(expense.isSplit());
        assertFalse(expense.hasReceipt());
    }

    @Test
    @DisplayName("Negative expense amount should throw ValidationException")
    void testNegativeAmountThrowsException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", -10.0, "Lunch", LocalDate.now(), "shafiq");
        });
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("Zero expense amount should throw ValidationException")
    void testZeroAmountThrowsException() {
        ValidationException ex = assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", 0.0, "Lunch", LocalDate.now(), "shafiq");
        });
        assertTrue(ex.getMessage().contains("strictly greater than zero"));
    }

    @Test
    @DisplayName("Empty or null description should throw ValidationException")
    void testEmptyDescriptionThrowsException() {
        assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", 25.0, "", LocalDate.now(), "shafiq");
        });
        assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", 25.0, "   ", LocalDate.now(), "shafiq");
        });
        assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", 25.0, null, LocalDate.now(), "shafiq");
        });
    }

    @Test
    @DisplayName("Null date should throw ValidationException")
    void testNullDateThrowsException() {
        assertThrows(ValidationException.class, () -> {
            new Expense("FOOD", 25.0, "Lunch", null, "shafiq");
        });
    }

    @Test
    @DisplayName("User password should be hashed with SHA-256")
    void testUserPasswordHashing() {
        User user = new User("shafiq", User.hashPassword("SecurePass123!"), "shafiq@example.com");
        assertEquals("shafiq", user.getUsername());
        assertNotEquals("SecurePass123!", user.getPasswordHash());
        assertTrue(user.verifyPassword("SecurePass123!"));
        assertFalse(user.verifyPassword("WrongPassword"));
    }

    @Test
    @DisplayName("Invalid user email should throw ValidationException")
    void testInvalidUserEmailThrowsException() {
        assertThrows(ValidationException.class, () -> {
            new User("shafiq", "hashed_pass", "invalid_email_no_at");
        });
    }
}
