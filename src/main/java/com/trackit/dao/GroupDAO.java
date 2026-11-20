package com.trackit.dao;

import com.trackit.model.Group;
import com.trackit.model.SharedExpense;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Data Access Object for Group and Shared Expense operations.
 * Implements file-based object serialization (.dat files) via DatabaseManager.
 */
public class GroupDAO {
    private static final String GROUPS_KEY = "groups";
    private static final String SHARED_EXPENSES_KEY = "shared_expenses";
    private final DatabaseManager dbManager;
    
    public GroupDAO() {
        this.dbManager = DatabaseManager.getInstance();
    }
    
    @SuppressWarnings("unchecked")
    private Map<String, Group> loadGroups() {
        Map<String, Group> groups = dbManager.loadData(GROUPS_KEY, Map.class);
        return groups != null ? new HashMap<>(groups) : new HashMap<>();
    }
    
    private void saveGroups(Map<String, Group> groups) {
        dbManager.saveData(GROUPS_KEY, groups != null ? new HashMap<>(groups) : new HashMap<>());
    }
    
    @SuppressWarnings("unchecked")
    private ArrayList<SharedExpense> loadSharedExpenses() {
        ArrayList<SharedExpense> expenses = dbManager.loadData(SHARED_EXPENSES_KEY, ArrayList.class);
        return expenses != null ? new ArrayList<>(expenses) : new ArrayList<>();
    }
    
    private void saveSharedExpenses(ArrayList<SharedExpense> expenses) {
        dbManager.saveData(SHARED_EXPENSES_KEY, expenses != null ? new ArrayList<>(expenses) : new ArrayList<>());
    }
    
    public boolean createGroup(Group group) {
        if (group == null || group.getGroupName() == null || group.getGroupName().trim().isEmpty()) {
            return false;
        }
        Map<String, Group> groups = loadGroups();
        if (groups.containsKey(group.getGroupName())) {
            return false;
        }
        groups.put(group.getGroupName(), group);
        saveGroups(groups);
        return true;
    }
    
    public Group getGroup(String groupName) {
        if (groupName == null) return null;
        return loadGroups().get(groupName);
    }
    
    public ArrayList<Group> getUserGroups(String username) {
        if (username == null || username.trim().isEmpty()) {
            return new ArrayList<>();
        }
        Map<String, Group> groups = loadGroups();
        ArrayList<Group> userGroups = new ArrayList<>();
        for (Group group : groups.values()) {
            if (group.getMembers() != null && group.getMembers().contains(username)) {
                userGroups.add(group);
            }
        }
        return userGroups;
    }
    
    public void updateGroup(Group group) {
        if (group == null || group.getGroupName() == null) return;
        Map<String, Group> groups = loadGroups();
        groups.put(group.getGroupName(), group);
        saveGroups(groups);
    }
    
    public void addSharedExpense(SharedExpense expense) {
        if (expense == null) return;
        ArrayList<SharedExpense> expenses = loadSharedExpenses();
        expenses.add(expense);
        saveSharedExpenses(expenses);
    }
    
    public ArrayList<SharedExpense> getGroupExpenses(String groupName) {
        if (groupName == null) return new ArrayList<>();
        ArrayList<SharedExpense> allExpenses = loadSharedExpenses();
        ArrayList<SharedExpense> groupExpenses = new ArrayList<>();
        for (SharedExpense expense : allExpenses) {
            if (groupName.equals(expense.getGroupName())) {
                groupExpenses.add(expense);
            }
        }
        return groupExpenses;
    }
    
    public void deleteGroup(String groupName) {
        if (groupName == null) return;
        Map<String, Group> groups = loadGroups();
        groups.remove(groupName);
        saveGroups(groups);
        
        // Also cleanup group expenses
        ArrayList<SharedExpense> allExpenses = loadSharedExpenses();
        allExpenses.removeIf(e -> groupName.equals(e.getGroupName()));
        saveSharedExpenses(allExpenses);
    }
}
