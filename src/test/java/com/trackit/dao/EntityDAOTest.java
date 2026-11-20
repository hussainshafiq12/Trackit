package com.trackit.dao;

import com.trackit.model.Budget;
import com.trackit.model.Expense;
import com.trackit.model.Group;
import com.trackit.model.SharedExpense;
import com.trackit.model.User;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Entity DAOs Object Serialization & Persistence Tests")
class EntityDAOTest {

    private ExpenseDAO expenseDAO;
    private GroupDAO groupDAO;
    private UserDAO userDAO;
    private BudgetDAO budgetDAO;

    private static final String TEST_USER = "niaz_test_user";
    private static final String TEST_GROUP = "Trip_Group_Test";

    @BeforeEach
    void setUp() {
        expenseDAO = new ExpenseDAO();
        groupDAO = new GroupDAO();
        userDAO = new UserDAO();
        budgetDAO = new BudgetDAO();

        cleanTestData();
    }

    @AfterEach
    void tearDown() {
        cleanTestData();
    }

    private void cleanTestData() {
        expenseDAO.clearAllExpenses(TEST_USER);
        groupDAO.deleteGroup(TEST_GROUP);
        userDAO.deleteUser(TEST_USER);
        budgetDAO.deleteBudget(TEST_USER);
        DatabaseManager.getInstance().clearCache();
    }

    @Test
    @DisplayName("ExpenseDAO: Should persist, load, update and delete serialized expenses (.dat)")
    void testExpenseDAOLifecycle() {
        Expense expense1 = new Expense("FOOD", 45.0, "Lunch meeting", LocalDate.now(), TEST_USER);
        expenseDAO.addExpense(TEST_USER, expense1);

        ArrayList<Expense> expenses = expenseDAO.loadExpenses(TEST_USER);
        assertEquals(1, expenses.size());
        assertEquals("FOOD", expenses.get(0).getCategory());
        assertEquals(45.0, expenses.get(0).getAmount(), 0.001);

        // Update
        Expense expense2 = new Expense("FOOD", 55.0, "Lunch meeting upgraded", LocalDate.now(), TEST_USER);
        expenseDAO.updateExpense(TEST_USER, expense1, expense2);

        ArrayList<Expense> updated = expenseDAO.loadExpenses(TEST_USER);
        assertEquals(1, updated.size());
        assertEquals(55.0, updated.get(0).getAmount(), 0.001);

        // Delete
        expenseDAO.deleteExpense(TEST_USER, expense2);
        assertTrue(expenseDAO.loadExpenses(TEST_USER).isEmpty());
    }

    @Test
    @DisplayName("GroupDAO: Should persist and query groups and shared expenses across restarts")
    void testGroupDAOLifecycle() {
        Group group = new Group(TEST_GROUP, TEST_USER);
        group.addMember("shafiq");
        boolean created = groupDAO.createGroup(group);
        assertTrue(created);

        // Duplicate rejection
        assertFalse(groupDAO.createGroup(group));

        Group loaded = groupDAO.getGroup(TEST_GROUP);
        assertNotNull(loaded);
        assertEquals(TEST_GROUP, loaded.getGroupName());
        assertTrue(loaded.getMembers().contains(TEST_USER));
        assertTrue(loaded.getMembers().contains("shafiq"));

        // Add shared expense
        SharedExpense shared = new SharedExpense("TRAVEL", 120.0, "Gas", LocalDate.now(), TEST_USER, TEST_GROUP);
        groupDAO.addSharedExpense(shared);

        ArrayList<SharedExpense> groupExpenses = groupDAO.getGroupExpenses(TEST_GROUP);
        assertEquals(1, groupExpenses.size());
        assertEquals(120.0, groupExpenses.get(0).getTotalAmount(), 0.001);

        // User groups query
        ArrayList<Group> userGroups = groupDAO.getUserGroups(TEST_USER);
        assertEquals(1, userGroups.size());
    }

    @Test
    @DisplayName("UserDAO: Should persist user entities with salted SHA-256 credentials")
    void testUserDAOLifecycle() {
        User user = new User(TEST_USER, User.hashPassword("Password123!"), "niaz@example.com");
        assertTrue(userDAO.createUser(user));
        assertFalse(userDAO.createUser(user)); // Reject duplicate

        assertTrue(userDAO.userExists(TEST_USER));
        User loaded = userDAO.getUser(TEST_USER);
        assertNotNull(loaded);
        assertEquals(TEST_USER, loaded.getUsername());
        assertTrue(loaded.verifyPassword("Password123!"));

        userDAO.deleteUser(TEST_USER);
        assertFalse(userDAO.userExists(TEST_USER));
        assertNull(userDAO.getUser(TEST_USER));
    }

    @Test
    @DisplayName("BudgetDAO: Should persist, check existence, and delete user budget thresholds")
    void testBudgetDAOLifecycle() {
        assertFalse(budgetDAO.hasBudget(TEST_USER));

        Budget budget = new Budget(TEST_USER, 1500.0);
        budgetDAO.saveBudget(budget);

        assertTrue(budgetDAO.hasBudget(TEST_USER));
        Budget loaded = budgetDAO.loadBudget(TEST_USER);
        assertNotNull(loaded);
        assertEquals(1500.0, loaded.getMonthlyBudget(), 0.001);

        budgetDAO.deleteBudget(TEST_USER);
        assertFalse(budgetDAO.hasBudget(TEST_USER));
    }
}
