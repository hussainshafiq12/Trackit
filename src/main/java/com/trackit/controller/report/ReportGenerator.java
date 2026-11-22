package com.trackit.controller.report;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FACTORY PATTERN - ReportGenerator Interface
 * Problem Solved: Flexible instantiation of various report formats
 * Benefit: Open for new report types without modifying client code (OCP)
 * 
 * Concurrency (REQ-QA-01): Provides optimized non-blocking asynchronous report generation
 * running on a dedicated worker pool to protect the Swing Event Dispatch Thread (EDT).
 */
public interface ReportGenerator {

    /**
     * Dedicated daemon executor pool for async reporting tasks to prevent UI thread freeze.
     */
    ExecutorService ASYNC_REPORT_POOL = Executors.newFixedThreadPool(
            Math.max(4, Runtime.getRuntime().availableProcessors()),
            runnable -> {
                Thread t = new Thread(runnable, "Trackit-Async-Report-Worker");
                t.setDaemon(true);
                return t;
            }
    );

    /**
     * Generate report content synchronously as string
     * @return Formatted report string
     */
    String generate();
    
    /**
     * Generate report content asynchronously without blocking UI thread (REQ-QA-01).
     * @return CompletableFuture containing formatted report string
     */
    default CompletableFuture<String> generateAsync() {
        return CompletableFuture.supplyAsync(this::generate, ASYNC_REPORT_POOL);
    }
    
    /**
     * Save report to file synchronously
     * @param filepath Path where to save the report
     * @return true if successful, false otherwise
     */
    boolean saveToFile(String filepath);
    
    /**
     * Save report to file asynchronously without blocking UI thread (REQ-QA-01).
     * @param filepath Path where to save the report
     * @return CompletableFuture resolving to true if successful
     */
    default CompletableFuture<Boolean> saveToFileAsync(String filepath) {
        return CompletableFuture.supplyAsync(() -> saveToFile(filepath), ASYNC_REPORT_POOL);
    }
    
    /**
     * Get report type identifier
     */
    String getReportType();
}
