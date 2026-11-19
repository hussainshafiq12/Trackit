package com.trackit.controller.report;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("ReportFactory Factory Method Pattern Unit Tests")
class ReportFactoryTest {

    @Test
    @DisplayName("Should throw IllegalArgumentException when creating report with unknown type")
    void testUnknownReportType() {
        IllegalArgumentException thrown = assertThrows(IllegalArgumentException.class, () -> {
            ReportFactory.createReport("UNKNOWN_TYPE", "user123", new Object());
        });
        assertTrue(thrown.getMessage().contains("Unknown report type"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when reportType is null or empty")
    void testNullOrEmptyReportType() {
        IllegalArgumentException thrownNull = assertThrows(IllegalArgumentException.class, () -> {
            ReportFactory.createReport(null, "user123", new Object());
        });
        assertTrue(thrownNull.getMessage().contains("cannot be null or empty"));

        IllegalArgumentException thrownEmpty = assertThrows(IllegalArgumentException.class, () -> {
            ReportFactory.createReport("   ", "user123", new Object());
        });
        assertTrue(thrownEmpty.getMessage().contains("cannot be null or empty"));
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when controller type is invalid for personal report")
    void testInvalidControllerTypePersonal() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReportFactory.createReport("PERSONAL", "user123", "WrongControllerObject");
        });
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when controller type is invalid for group report")
    void testInvalidControllerTypeGroup() {
        assertThrows(IllegalArgumentException.class, () -> {
            ReportFactory.createReport("GROUP", "group123", "WrongControllerObject");
        });
    }
}
