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
}
