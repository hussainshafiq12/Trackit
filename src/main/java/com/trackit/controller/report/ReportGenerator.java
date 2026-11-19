package com.trackit.controller.report;

import java.util.concurrent.CompletableFuture;

/**
 * FACTORY PATTERN - ReportGenerator Interface
 * Problem Solved: Flexible instantiation of various report formats
 * Benefit: Open for new report types without modifying client code (OCP)
 * 
 * Abstraction: Defines contract for report generation
 * ISP: Clients depend only on methods they use
 * Concurrency: Provides asynchronous non-blocking report generation and saving
 */
public interface ReportGenerator {
    /**
     * Generate report content synchronously as string
     * @return Formatted report string
     */
    String generate();
    
    /**
     * Generate report content asynchronously without blocking UI thread
     * @return CompletableFuture containing formatted report string
     */
    default CompletableFuture<String> generateAsync() {
        return CompletableFuture.supplyAsync(this::generate);
    }
    
    /**
     * Save report to file synchronously
     * @param filepath Path where to save the report
     * @return true if successful, false otherwise
     */
    boolean saveToFile(String filepath);
    
    /**
     * Save report to file asynchronously without blocking UI thread
     * @param filepath Path where to save the report
     * @return CompletableFuture resolving to true if successful
     */
    default CompletableFuture<Boolean> saveToFileAsync(String filepath) {
        return CompletableFuture.supplyAsync(() -> saveToFile(filepath));
    }
    
    /**
     * Get report type identifier
     */
    String getReportType();
}