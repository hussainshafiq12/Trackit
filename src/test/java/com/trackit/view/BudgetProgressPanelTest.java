package com.trackit.view;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.Color;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("BudgetProgressPanel Visual Alert & Threshold Tests (REQ-FUN-04)")
class BudgetProgressPanelTest {

    @Test
    @DisplayName("Should classify percentage into NORMAL, WARNING, and EXCEEDED states")
    void testDetermineAlertStatus() {
        assertEquals("NORMAL", BudgetProgressPanel.determineAlertStatus(0.0));
        assertEquals("NORMAL", BudgetProgressPanel.determineAlertStatus(50.0));
        assertEquals("NORMAL", BudgetProgressPanel.determineAlertStatus(79.9));

        assertEquals("WARNING", BudgetProgressPanel.determineAlertStatus(80.0));
        assertEquals("WARNING", BudgetProgressPanel.determineAlertStatus(99.9));

        assertEquals("EXCEEDED", BudgetProgressPanel.determineAlertStatus(100.0));
        assertEquals("EXCEEDED", BudgetProgressPanel.determineAlertStatus(125.5));
    }

    @Test
    @DisplayName("Should assign Green, Orange, and Red colors according to threshold rules")
    void testDetermineAlertColor() {
        Color safeColor = BudgetProgressPanel.determineAlertColor(50.0);
        assertEquals(UITheme.SUCCESS, safeColor);

        Color warnColor = BudgetProgressPanel.determineAlertColor(85.0);
        assertEquals(new Color(255, 159, 10), warnColor);

        Color dangerColor = BudgetProgressPanel.determineAlertColor(105.0);
        assertEquals(UITheme.DANGER, dangerColor);
    }

    @Test
    @DisplayName("Should generate descriptive alert warnings for UI breach notification")
    void testFormatAlertWarning() {
        String safeMsg = BudgetProgressPanel.formatAlertWarning(60.0, "FOOD");
        assertEquals("OK", safeMsg);

        String warnMsg = BudgetProgressPanel.formatAlertWarning(85.0, "FOOD");
        assertTrue(warnMsg.contains("warning") && warnMsg.contains("85.0%"));

        String breachMsg = BudgetProgressPanel.formatAlertWarning(110.0, "TRAVEL");
        assertTrue(breachMsg.contains("exceeded") && breachMsg.contains("10.0%"));
    }
}
