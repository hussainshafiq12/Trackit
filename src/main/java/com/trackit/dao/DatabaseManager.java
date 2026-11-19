package com.trackit.dao;

import com.trackit.exception.DataPersistenceException;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.concurrent.locks.Lock;

/**
 * Singleton DatabaseManager for centralized data access.
 * Implements thread safety using ReentrantReadWriteLock (allowing concurrent reads and exclusive writes).
 */
public class DatabaseManager {
    private static volatile DatabaseManager instance;
    private static final String DATA_DIR = "data/";
    private static final Object initLock = new Object();
    
    // Explicit ReadWriteLock for thread-safe concurrent access
    private final ReentrantReadWriteLock rwLock = new ReentrantReadWriteLock();
    private final Lock readLock = rwLock.readLock();
    private final Lock writeLock = rwLock.writeLock();
    
    // In-memory cache for enhanced performance
    private final Map<String, Object> cache;
    
    private DatabaseManager() {
        this.cache = new HashMap<>();
        ensureDataDirectory();
    }
    
    /**
     * Thread-safe singleton instance getter (Double-checked locking with volatile)
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
     * Thread-safe Save data to storage with exclusive write lock.
     */
    public void saveData(String key, Object data) {
        writeLock.lock();
        try {
            cache.put(key, data);
            persistToDisk(key, data);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Thread-safe Load data from storage with read lock / upgrade to write lock.
     */
    @SuppressWarnings("unchecked")
    public <T> T loadData(String key, Class<T> type) {
        readLock.lock();
        try {
            if (cache.containsKey(key)) {
                return (T) cache.get(key);
            }
        } finally {
            readLock.unlock();
        }
        
        // Cache miss: acquire write lock to load and populate cache
        writeLock.lock();
        try {
            // Double-check cache in case another thread populated it
            if (cache.containsKey(key)) {
                return (T) cache.get(key);
            }
            return loadFromDisk(key, type);
        } finally {
            writeLock.unlock();
        }
    }
    
    /**
     * Persist data to disk safely.
     */
    private void persistToDisk(String key, Object data) {
        try (ObjectOutputStream oos = new ObjectOutputStream(
                new FileOutputStream(DATA_DIR + key + ".dat"))) {
            oos.writeObject(data);
        } catch (IOException e) {
            System.err.println("Error persisting data for key " + key + ": " + e.getMessage());
        }
    }
    
    /**
     * Load data from disk safely.
     */
    @SuppressWarnings("unchecked")
    private <T> T loadFromDisk(String key, Class<T> type) {
        File file = new File(DATA_DIR + key + ".dat");
        if (file.exists()) {
            try (ObjectInputStream ois = new ObjectInputStream(
                    new FileInputStream(file))) {
                Object data = ois.readObject();
                cache.put(key, data);
                return (T) data;
            } catch (IOException | ClassNotFoundException e) {
                System.err.println("Error loading data for key " + key + ": " + e.getMessage());
            }
        }
        return null;
    }
    
    /**
     * Check if data exists using read lock.
     */
    public boolean dataExists(String key) {
        readLock.lock();
        try {
            return cache.containsKey(key) || 
                   new File(DATA_DIR + key + ".dat").exists();
        } finally {
            readLock.unlock();
        }
    }
    
    /**
     * Delete data using write lock.
     */
    public void deleteData(String key) {
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
     * Clear all cache using write lock.
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