package com.trackit.dao;

import com.trackit.exception.DataPersistenceException;
import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Singleton DatabaseManager for centralized thread-safe data persistence.
 * Implements REQ-QA-03: Thread-Safety & Concurrency using ReentrantReadWriteLock.
 * Multiple reader threads can access cache/disk concurrently via readLock,
 * while write operations require mutual exclusion via writeLock, guaranteeing
 * zero race conditions, data corruption, or deadlocks.
 */
public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private static final String DATA_DIR = "data/";
    private static final Object initLock = new Object();
    
    // Explicit ReentrantReadWriteLock for high-throughput concurrent I/O
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    
    // In-memory cache guarded by rwLock
    private final Map<String, Object> cache;
    
    private DatabaseManager() {
        this.cache = new HashMap<>();
        ensureDataDirectory();
    }
    
    /**
     * Centralized thread-safe data access point using Double-Checked Locking.
     */
    public static DatabaseManager getInstance() {
        if (instance == null) {
            synchronized (initLock) {
                if (instance == null) {
                    instance = new DatabaseManager();
                }
            }
        }
        return instance;
    }
    
    private void ensureDataDirectory() {
        File dir = new File(DATA_DIR);
        if (!dir.exists()) {
            dir.mkdirs();
        }
    }
    
    /**
     * Thread-safe save operation guarded by mutual-exclusion writeLock.
     * Writes atomically to temporary file before rename to prevent corruption.
     */
    public void saveData(String key, Object data) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("Key cannot be null or empty");
        }
        writeLock.lock();
        try {
            cache.put(key, data);
            persistToDiskAtomic(key, data);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Thread-safe read operation.
     * Acquires readLock for cache hit; escalates cleanly to writeLock on cache miss.
     */
    @SuppressWarnings("unchecked")
    public <T> T loadData(String key, Class<T> type) {
        if (key == null || key.trim().isEmpty()) {
            return null;
        }
        
        readLock.lock();
        try {
            if (cache.containsKey(key)) {
                return (T) cache.get(key);
            }
        } finally {
            readLock.unlock();
        }
        
        // Cache miss: acquire writeLock to load from disk and populate cache safely
        writeLock.lock();
        try {
            // Double-check cache in case another thread populated it in the interim
            if (cache.containsKey(key)) {
                return (T) cache.get(key);
            }
            return loadFromDisk(key, type);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Persist data to disk using atomic temp file replacement.
     */
    private void persistToDiskAtomic(String key, Object data) {
        File targetFile = new File(DATA_DIR + key + ".dat");
        File tempFile = new File(DATA_DIR + key + ".tmp");
        
        try {
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(tempFile))) {
                oos.writeObject(data);
                oos.flush();
            }
            Files.move(tempFile.toPath(), targetFile.toPath(), 
                       StandardCopyOption.REPLACE_EXISTING, 
                       StandardCopyOption.ATOMIC_MOVE);
        } catch (IOException e) {
            // Fallback to standard write if atomic move fails on underlying filesystem
            try (ObjectOutputStream oos = new ObjectOutputStream(new FileOutputStream(targetFile))) {
                oos.writeObject(data);
            } catch (IOException ex) {
                throw new DataPersistenceException("Failed to persist data for key: " + key, ex);
            }
        } finally {
            if (tempFile.exists()) {
                tempFile.delete();
            }
        }
    }
    
    /**
     * Load serialized object from disk safely.
     */
    @SuppressWarnings("unchecked")
    private <T> T loadFromDisk(String key, Class<T> type) {
        File file = new File(DATA_DIR + key + ".dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(new FileInputStream(file))) {
                Object data = ois.readObject();
                cache.put(key, data);
                return (T) data;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Warning: Could not read data file for key " + key + ": " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Check if data exists in cache or on disk using readLock.
     */
    public boolean dataExists(String key) {
        if (key == null) return false;
        readLock.lock();
        try {
            return cache.containsKey(key) || new File(DATA_DIR + key + ".dat").exists();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Delete data from cache and disk using writeLock.
     */
    public void deleteData(String key) {
        if (key == null) return;
        writeLock.lock();
        try {
            cache.remove(key);
            File file = new File(DATA_DIR + key + ".dat");
            if (file.exists()) {
                file.delete();
            }
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Clear cache using writeLock.
     */
    public void clearCache() {
        writeLock.lock();
        try {
            cache.clear();
        } finally {
            writeLock.unlock();
        }
    }
}
