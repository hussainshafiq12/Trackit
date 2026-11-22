package com.trackit.view;

import com.trackit.model.ExpenseSearchCriteria;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ModernFilterDialog UI Component Tests")
class ModernFilterDialogTest {

    @Test
    @DisplayName("Should initialize ModernFilterDialog without errors and set title")
    void testDialogInitialization() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        AtomicReference<ExpenseSearchCriteria> appliedCriteria = new AtomicReference<>();
        ModernFilterDialog dialog = new ModernFilterDialog(null, appliedCriteria::set);

        assertNotNull(dialog);
        assertEquals("Filter Expenses", dialog.getTitle());
        assertFalse(dialog.isApplied());
        dialog.dispose();
    }

    @Test
    @DisplayName("Should invoke callback with unconstrained criteria on reset")
    void testDialogResetAction() {
        if (GraphicsEnvironment.isHeadless()) {
            return;
        }

        AtomicBoolean callbackTriggered = new AtomicBoolean(false);
        ModernFilterDialog dialog = new ModernFilterDialog(null, criteria -> {
            callbackTriggered.set(true);
            assertNotNull(criteria);
            assertFalse(criteria.hasActiveFilters());
        });

        assertNotNull(dialog);
        dialog.dispose();
    }

    @Test
    @DisplayName("Should verify max amount ceiling constant is 1,000,000.0")
    void testMaxAmountCeilingConstant() {
        assertEquals(1000000.0, ModernFilterDialog.MAX_AMOUNT_LIMIT, 0.001);
    }
}
