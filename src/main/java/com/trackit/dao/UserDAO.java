package com.trackit.dao;

import com.trackit.model.User;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for User operations.
 * Implements file-based object serialization (.dat files) via DatabaseManager.
 */
public class UserDAO {
    private static final String USERS_KEY = "users";
    private final DatabaseManager dbManager;
    
    public UserDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, User> loadUsers() {
        Map<String, User> users = dbManager.loadData(USERS_KEY, Map.class);
        return users != null ? new HashMap<>(users) : new HashMap<>();
    }
    
    private void saveUsers(Map<String, User> users) {
        dbManager.saveData(USERS_KEY, users != null ? new HashMap<>(users) : new HashMap<>());
    }
    
    public boolean createUser(User user) {
        if (user == null || user.getUsername() == null || user.getUsername().trim().isEmpty()) {
            return false;
        }
        Map<String, User> users = loadUsers();
        if (users.containsKey(user.getUsername())) {
            return false;
        }
        users.put(user.getUsername(), user);
        saveUsers(users);
        return true;
    }
    
    public User getUser(String username) {
        if (username == null || username.trim().isEmpty()) {
            return null;
        }
        return loadUsers().get(username.trim());
    }
    
    public boolean userExists(String username) {
        if (username == null || username.trim().isEmpty()) {
            return false;
        }
        return loadUsers().containsKey(username.trim());
    }
    
    public void updateUser(User user) {
        if (user == null || user.getUsername() == null) return;
        Map<String, User> users = loadUsers();
        users.put(user.getUsername(), user);
        saveUsers(users);
    }
    
    public void deleteUser(String username) {
        if (username == null) return;
        Map<String, User> users = loadUsers();
        users.remove(username.trim());
        saveUsers(users);
    }
}
