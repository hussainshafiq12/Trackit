package com.trackit.controller;

import com.trackit.controller.strategy.EqualSplitStrategy;
import com.trackit.controller.strategy.PercentageSplitStrategy;
import com.trackit.controller.strategy.WeightedSplitStrategy;
import com.trackit.dao.GroupDAO;
import com.trackit.model.Group;
import com.trackit.model.SharedExpense;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("GroupController Business Logic & Strategy Coordination Tests")
class GroupControllerTest {

    private GroupController groupController;
    private static final String TEST_GROUP = "Colleagues_Lunch_Group";
    private static final String CREATOR = "niaz";

    @BeforeEach
    void setUp() {
        groupController = new GroupController();
        groupController.deleteGroup(TEST_GROUP);
    }

    @AfterEach
    void tearDown() {
        groupController.deleteGroup(TEST_GROUP);
    }

    @Test
    @DisplayName("Should create group and manage member lifecycle defensively")
    void testCreateGroupAndMemberManagement() {
        assertTrue(groupController.createGroup(TEST_GROUP, CREATOR));
        assertFalse(groupController.createGroup(TEST_GROUP, CREATOR), "Duplicate group name must be rejected");

        Group group = groupController.getGroup(TEST_GROUP);
        assertNotNull(group);
        assertEquals(TEST_GROUP, group.getGroupName());
        assertTrue(group.getMembers().contains(CREATOR));

        // Add member
        assertTrue(groupController.addMemberToGroup(TEST_GROUP, "shafiq"));
        assertFalse(groupController.addMemberToGroup(TEST_GROUP, "shafiq"), "Duplicate member should return false");

        Group updated = groupController.getGroup(TEST_GROUP);
        assertEquals(2, updated.getMembers().size());

        // Remove member
        assertTrue(groupController.removeMemberFromGroup(TEST_GROUP, "shafiq"));
        assertFalse(groupController.removeMemberFromGroup(TEST_GROUP, "non_existent"));
        assertEquals(1, groupController.getGroup(TEST_GROUP).getMembers().size());
    }

    @Test
    @DisplayName("Should coordinate shared expense with runtime strategy selection (EqualSplit)")
    void testAddSharedExpenseEqualSplit() {
        groupController.createGroup(TEST_GROUP, CREATOR);
        groupController.addMemberToGroup(TEST_GROUP, "shafiq");
        groupController.addMemberToGroup(TEST_GROUP, "ali");

        List<String> participants = Arrays.asList(CREATOR, "shafiq", "ali");
        groupController.addSharedExpense("FOOD", 90.0, "Team lunch", CREATOR, TEST_GROUP, "EQUAL", participants, null);

        ArrayList<SharedExpense> expenses = groupController.getGroupExpenses(TEST_GROUP);
        assertEquals(1, expenses.size());
        assertEquals(90.0, expenses.get(0).getTotalAmount(), 0.001);
        assertEquals("EQUAL", expenses.get(0).getSplitStrategyType());
        assertNotNull(expenses.get(0).getSplitDetails());
        assertEquals(3, expenses.get(0).getSplitDetails().size());
    }

    @Test
    @DisplayName("Should calculate group balances safely for Percentage splits without null weight errors")
    void testCalculateGroupBalancesWithPercentageSplit() {
        groupController.createGroup(TEST_GROUP, CREATOR);
        groupController.addMemberToGroup(TEST_GROUP, "shafiq");

        List<String> participants = Arrays.asList(CREATOR, "shafiq");
        List<Double> percentages = Arrays.asList(60.0, 40.0);

        // Niaz pays $100 total. Niaz share = $60, Shafiq share = $40
        groupController.addSharedExpense("TRAVEL", 100.0, "Train tickets", CREATOR, TEST_GROUP, 
                                         "PERCENTAGE", participants, percentages);

        Map<String, Double> balances = groupController.calculateGroupBalances(TEST_GROUP);
        // Niaz paid 100, owes 60 -> net +40.0
        // Shafiq paid 0, owes 40 -> net -40.0
        assertEquals(40.0, balances.get(CREATOR), 0.01);
        assertEquals(-40.0, balances.get("shafiq"), 0.01);
    }

    @Test
    @DisplayName("Should calculate group balances safely for Weighted splits without null weight errors")
    void testCalculateGroupBalancesWithWeightedSplit() {
        groupController.createGroup(TEST_GROUP, CREATOR);
        groupController.addMemberToGroup(TEST_GROUP, "shafiq");

        List<String> participants = Arrays.asList(CREATOR, "shafiq");
        List<Double> weights = Arrays.asList(3.0, 1.0); // 3:1 ratio (75% and 25%)

        // Niaz pays $200 total. Niaz share = $150, Shafiq share = $50
        groupController.addSharedExpense("LODGING", 200.0, "Hotel room", CREATOR, TEST_GROUP, 
                                         "WEIGHTED", participants, weights);

        Map<String, Double> balances = groupController.calculateGroupBalances(TEST_GROUP);
        // Niaz paid 200, owes 150 -> net +50.0
        // Shafiq paid 0, owes 50 -> net -50.0
        assertEquals(50.0, balances.get(CREATOR), 0.01);
        assertEquals(-50.0, balances.get("shafiq"), 0.01);
    }

    @Test
    @DisplayName("Should accurately calculate member balances and wire to SettlementOptimizer")
    void testCalculateGroupBalancesAndOptimization() {
        groupController.createGroup(TEST_GROUP, CREATOR);
        groupController.addMemberToGroup(TEST_GROUP, "shafiq");
        groupController.addMemberToGroup(TEST_GROUP, "ali");

        List<String> participants = Arrays.asList(CREATOR, "shafiq", "ali");
        // Niaz pays $90 for Niaz, Shafiq, Ali (Equal split: $30 each)
        // Niaz paid 90, owes 30 -> Net = +60
        // Shafiq paid 0, owes 30 -> Net = -30
        // Ali paid 0, owes 30 -> Net = -30
        groupController.addSharedExpense("FOOD", 90.0, "Lunch", CREATOR, TEST_GROUP, "EQUAL", participants, null);

        Map<String, Double> balances = groupController.calculateGroupBalances(TEST_GROUP);
        assertEquals(60.0, balances.get(CREATOR), 0.01);
        assertEquals(-30.0, balances.get("shafiq"), 0.01);
        assertEquals(-30.0, balances.get("ali"), 0.01);

        // Verify SettlementOptimizer integration
        List<SettlementOptimizer.Transaction> transactions = groupController.getOptimizedSettlement(TEST_GROUP);
        assertNotNull(transactions);
        assertEquals(2, transactions.size());
        for (SettlementOptimizer.Transaction tx : transactions) {
            assertEquals(CREATOR, tx.to, "Creditor should be Niaz");
            assertEquals(30.0, tx.amount, 0.01);
        }
    }

    @Test
    @DisplayName("Should throw IllegalArgumentException when boundary parameters are violated")
    void testBoundaryValidations() {
        assertThrows(IllegalArgumentException.class, () -> {
            groupController.createGroup("", CREATOR);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            groupController.createGroup(TEST_GROUP, null);
        });
        assertThrows(IllegalArgumentException.class, () -> {
            groupController.addSharedExpense("FOOD", -10.0, "Invalid", CREATOR, TEST_GROUP, "EQUAL", 
                                           Arrays.asList("a", "b"), null);
        });
    }

    @Test
    @DisplayName("Should report available split strategies matching Strategy pattern contracts")
    void testAvailableStrategies() {
        List<String> strats = groupController.getAvailableStrategies();
        assertTrue(strats.contains("EQUAL"));
        assertTrue(strats.contains("WEIGHTED"));
        assertTrue(strats.contains("PERCENTAGE"));
        assertInstanceOf(EqualSplitStrategy.class, groupController.getStrategy("EQUAL"));
        assertInstanceOf(WeightedSplitStrategy.class, groupController.getStrategy("WEIGHTED"));
        assertInstanceOf(PercentageSplitStrategy.class, groupController.getStrategy("PERCENTAGE"));
    }
}
