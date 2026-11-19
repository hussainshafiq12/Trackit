package com.trackit.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseManager Thread Safety & Concurrency Tests")
class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
        dbManager.clearCache();
    }

    @AfterEach
    void tearDown() {
        dbManager.deleteData("test_concurrent_key");
        dbManager.clearCache();
    }

    @Test
    @DisplayName("Should maintain Singleton invariant across multiple threads")
    void testSingletonInstance() {
        DatabaseManager instance1 = DatabaseManager.getInstance();
        DatabaseManager instance2 = DatabaseManager.getInstance();
        assertSame(instance1, instance2, "Both instances must be identical (Singleton)");
    }

    @Test
    @DisplayName("Should support concurrent reads and writes without data corruption")
    void testConcurrentReadWriteOperations() throws InterruptedException {
        final int THREAD_COUNT = 10;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    String testKey = "test_concurrent_key_" + id;
                    String testData = "Payload_" + id;
                    dbManager.saveData(testKey, testData);
                    String retrieved = dbManager.loadData(testKey, String.class);
                    if (testData.equals(retrieved)) {
                        successCounter.incrementAndGet();
                    }
                    dbManager.deleteData(testKey);
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent tasks should complete within timeout");
        assertEquals(THREAD_COUNT, successCounter.get(), "All concurrent thread operations must succeed");
    }
}
