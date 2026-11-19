package com.trackit.dao;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DatabaseManager Thread Safety & Concurrency Stress Tests (REQ-QA-03)")
class DatabaseManagerTest {

    private DatabaseManager dbManager;

    @BeforeEach
    void setUp() {
        dbManager = DatabaseManager.getInstance();
        dbManager.clearCache();
    }

    @AfterEach
    void tearDown() {
        for (int i = 0; i < 20; i++) {
            dbManager.deleteData("test_concurrent_key_" + i);
        }
        dbManager.deleteData("test_disk_reload_key");
        dbManager.deleteData("test_shared_key");
        dbManager.clearCache();
    }

    @Test
    @DisplayName("Should maintain Singleton invariant across multiple concurrent threads")
    void testSingletonInstanceAcrossConcurrentThreads() throws InterruptedException, ExecutionException {
        final int THREADS = 16;
        ExecutorService executor = Executors.newFixedThreadPool(THREADS);
        CountDownLatch startLatch = new CountDownLatch(1);
        Future<?>[] futures = new Future<?>[THREADS];

        for (int i = 0; i < THREADS; i++) {
            futures[i] = executor.submit(() -> {
                try {
                    startLatch.await(); // Simultaneous execution
                    DatabaseManager instance = DatabaseManager.getInstance();
                    assertSame(dbManager, instance, "Every thread must receive the exact same singleton instance");
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
        }

        startLatch.countDown();
        for (Future<?> f : futures) {
            f.get();
        }
        executor.shutdown();
    }

    @Test
    @DisplayName("Should support 15+ concurrent threads performing read/write I/O without data corruption")
    void testConcurrentReadWriteOperations() throws InterruptedException {
        final int THREAD_COUNT = 15;
        ExecutorService executor = Executors.newFixedThreadPool(THREAD_COUNT);
        CountDownLatch latch = new CountDownLatch(THREAD_COUNT);
        AtomicInteger successCounter = new AtomicInteger(0);

        for (int i = 0; i < THREAD_COUNT; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    String testKey = "test_concurrent_key_" + id;
                    String testData = "Payload_Data_" + id;
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

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "All concurrent tasks should complete within timeout");
        assertEquals(THREAD_COUNT, successCounter.get(), "All concurrent thread operations must succeed");
    }

    @Test
    @DisplayName("Should safely handle concurrent reads and writes to a shared key using ReentrantReadWriteLock")
    void testConcurrentSharedKeyAccess() throws InterruptedException {
        final int READERS = 10;
        final int WRITERS = 5;
        final String sharedKey = "test_shared_key";
        dbManager.saveData(sharedKey, "InitialValue");

        ExecutorService executor = Executors.newFixedThreadPool(READERS + WRITERS);
        CountDownLatch latch = new CountDownLatch(READERS + WRITERS);
        AtomicInteger readCount = new AtomicInteger(0);

        // Writers
        for (int i = 0; i < WRITERS; i++) {
            final int id = i;
            executor.submit(() -> {
                try {
                    dbManager.saveData(sharedKey, "UpdatedValue_" + id);
                } finally {
                    latch.countDown();
                }
            });
        }

        // Concurrent Readers
        for (int i = 0; i < READERS; i++) {
            executor.submit(() -> {
                try {
                    String val = dbManager.loadData(sharedKey, String.class);
                    if (val != null && val.startsWith("Update") || "InitialValue".equals(val)) {
                        readCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        boolean completed = latch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertTrue(completed, "Shared key concurrent operations should complete without deadlock");
        assertEquals(READERS, readCount.get(), "All concurrent readers must read valid consistent data");
    }

    @Test
    @DisplayName("Should persist data to disk file and reload after cache eviction")
    void testDiskSerializationAndReload() {
        String key = "test_disk_reload_key";
        String payload = "Persistent disk object payload";

        dbManager.saveData(key, payload);

        // Clear in-memory cache to force disk load
        dbManager.clearCache();

        String reloaded = dbManager.loadData(key, String.class);
        assertEquals(payload, reloaded, "Data must be loaded accurately from serialized disk file");
        assertTrue(dbManager.dataExists(key), "dataExists should return true for persisted disk file");
    }
}
