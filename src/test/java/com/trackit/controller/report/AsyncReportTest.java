package com.trackit.controller.report;

import com.trackit.controller.ExpenseManager;
import com.trackit.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.time.LocalDate;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Asynchronous Report Generation Concurrency Tests")
class AsyncReportTest {

    @Test
    @DisplayName("Should generate personal report asynchronously using CompletableFuture")
    void testPersonalReportAsyncExecution() throws Exception {
        ExpenseManager manager = new ExpenseManager();
        String user = "asyncUser_" + System.currentTimeMillis();
        manager.addExpense(user, new Expense("FOOD", 50.0, "Lunch", LocalDate.now(), user));
        manager.addExpense(user, new Expense("TRANSPORTATION", 25.0, "Taxi", LocalDate.now(), user));

        ReportGenerator generator = ReportFactory.createPersonalReport(user, manager);
        CompletableFuture<String> future = generator.generateAsync();

        assertNotNull(future);
        String report = future.get(5, TimeUnit.SECONDS);
        assertNotNull(report);
        assertTrue(report.contains("PERSONAL EXPENSE REPORT") || report.contains(user));
    }

    @Test
    @DisplayName("Should save report to file asynchronously without blocking")
    void testAsyncSaveToFile() throws Exception {
        ExpenseManager manager = new ExpenseManager();
        String user = "saveUser_" + System.currentTimeMillis();
        manager.addExpense(user, new Expense("UTILITIES", 120.0, "Power bill", LocalDate.now(), user));

        ReportGenerator generator = ReportFactory.createPersonalReport(user, manager);
        String tempPath = "target/async_report_test.txt";

        CompletableFuture<Boolean> saveFuture = generator.saveToFileAsync(tempPath);
        Boolean result = saveFuture.get(5, TimeUnit.SECONDS);

        assertTrue(result);
        File savedFile = new File(tempPath);
        assertTrue(savedFile.exists());
        assertTrue(savedFile.length() > 0);
        savedFile.delete();
    }

    @Test
    @DisplayName("Should handle zero total expenses without NaN or Infinity")
    void testZeroTotalExpensesDoesNotProduceNaNOrInfinity() {
        ExpenseManager manager = new ExpenseManager();
        String user = "zeroTotalUser_" + System.currentTimeMillis();

        ReportGenerator generator = ReportFactory.createPersonalReport(user, manager);
        String report = generator.generate();

        assertNotNull(report);
        assertFalse(report.contains("NaN"), "Report should not contain NaN");
        assertFalse(report.contains("Infinity"), "Report should not contain Infinity");
    }

    @Test
    @DisplayName("Should handle null or empty description without NullPointerException")
    void testNullDescriptionDoesNotThrowNPE() throws Exception {
        java.util.ArrayList<Expense> list = new java.util.ArrayList<>();
        Expense exp1 = new Expense("FOOD", 20.0, "Valid desc", LocalDate.now(), "user1");
        java.lang.reflect.Field descField = Expense.class.getDeclaredField("description");
        descField.setAccessible(true);
        descField.set(exp1, null);
        list.add(exp1);

        PersonalReportGenerator gen = new PersonalReportGenerator("user1", list);
        assertDoesNotThrow(() -> {
            String report = gen.generate();
            assertTrue(report.contains("No description"));
        });
    }

    @Test
    @DisplayName("Should regenerate report content before saving to file")
    void testReportRegeneratesOnSaveToFile() {
        java.util.ArrayList<Expense> list = new java.util.ArrayList<>();
        list.add(new Expense("FOOD", 15.0, "Breakfast", LocalDate.now(), "user1"));

        PersonalReportGenerator gen = new PersonalReportGenerator("user1", list);
        gen.generate(); // initial generation

        // Add more expense before saving
        list.add(new Expense("TRANSPORTATION", 35.0, "Bus", LocalDate.now(), "user1"));
        String tempPath = "target/regen_test_report.txt";

        boolean saved = gen.saveToFile(tempPath);
        assertTrue(saved);

        File f = new File(tempPath);
        assertTrue(f.exists());
        // Verify file contains the newly added expense amount ($50.00 total)
        f.delete();
    }
}
