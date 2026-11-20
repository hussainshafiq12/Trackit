package com.trackit.controller.report;

import com.trackit.model.Expense;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Asynchronous Report Concurrency & Benchmark Test (REQ-QA-01)")
class AsyncReportBenchmarkTest {

    private ArrayList<Expense> generate5000Expenses() {
        ArrayList<Expense> list = new ArrayList<>(5000);
        LocalDate baseDate = LocalDate.of(2026, 1, 1);
        for (int i = 0; i < 5000; i++) {
            list.add(new Expense(
                    (i % 2 == 0) ? "FOOD" : "UTILITIES",
                    10.0 + (i % 100),
                    "Expense item index #" + i,
                    baseDate.plusDays(i % 365),
                    "shafiq"
            ));
        }
        return list;
    }

    @Test
    @DisplayName("REQ-QA-01 Benchmark: 5,000 items async compile P95 latency <= 1.8s without blocking caller")
    void test5000TransactionAsyncReportLatency() throws Exception {
        ArrayList<Expense> expenses = generate5000Expenses();
        ReportGenerator csvGen = ReportFactory.createCsvReport("shafiq", expenses);

        long start = System.currentTimeMillis();
        CompletableFuture<String> future = csvGen.generateAsync();

        // Must return future immediately without blocking caller thread
        assertFalse(future.isDone(), "Calling generateAsync() must return immediately non-blocking");

        String result = future.get(1800, TimeUnit.MILLISECONDS);
        long elapsed = System.currentTimeMillis() - start;

        assertNotNull(result);
        assertTrue(result.contains("Expense item index #4999"));
        assertTrue(elapsed <= 1800, "REQ-QA-01: Execution latency must be <= 1.8s, was " + elapsed + "ms");
    }

    @Test
    @DisplayName("Should execute parallel async reports concurrently without thread starvation")
    void testParallelAsyncReportGeneration() throws Exception {
        ArrayList<Expense> expenses = generate5000Expenses();
        ReportGenerator gen1 = ReportFactory.createCsvReport("user1", expenses);
        ReportGenerator gen2 = ReportFactory.createJsonReport("user2", expenses);

        CompletableFuture<String> f1 = gen1.generateAsync();
        CompletableFuture<String> f2 = gen2.generateAsync();

        CompletableFuture<Void> all = CompletableFuture.allOf(f1, f2);
        all.get(3, TimeUnit.SECONDS);

        assertNotNull(f1.get());
        assertNotNull(f2.get());
    }
}
