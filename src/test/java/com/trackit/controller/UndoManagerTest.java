package com.trackit.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UndoManager Command Pattern Unit Tests")
class UndoManagerTest {

    private UndoManager undoManager;
    private MockCommand command1;
    private MockCommand command2;

    static class MockCommand implements Command {
        boolean executed = false;
        boolean undone = false;
        String desc;

        MockCommand(String desc) {
            this.desc = desc;
        }

        @Override
        public void execute() {
            executed = true;
            undone = false;
        }

        @Override
        public void undo() {
            executed = false;
            undone = true;
        }

        @Override
        public String getDescription() {
            return desc;
        }
    }

    @BeforeEach
    void setUp() {
        undoManager = new UndoManager(5);
        command1 = new MockCommand("Add Expense #1");
        command2 = new MockCommand("Add Expense #2");
    }

    @Test
    @DisplayName("Should execute command and push to undo stack")
    void testExecuteCommand() {
        undoManager.executeCommand(command1);

        assertTrue(command1.executed);
        assertTrue(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
        assertEquals("Add Expense #1", undoManager.getUndoDescription());
        assertEquals(1, undoManager.getHistorySize());
    }

    @Test
    @DisplayName("Should undo and redo commands accurately")
    void testUndoAndRedo() {
        undoManager.executeCommand(command1);
        undoManager.executeCommand(command2);

        // Undo command2
        boolean undoSuccess = undoManager.undo();
        assertTrue(undoSuccess);
        assertTrue(command2.undone);
        assertTrue(undoManager.canRedo());
        assertEquals("Add Expense #2", undoManager.getRedoDescription());

        // Redo command2
        boolean redoSuccess = undoManager.redo();
        assertTrue(redoSuccess);
        assertTrue(command2.executed);
        assertFalse(undoManager.canRedo());
    }

    @Test
    @DisplayName("Undo on empty stack should return false")
    void testEmptyUndo() {
        assertFalse(undoManager.undo());
        assertFalse(undoManager.redo());
        assertNull(undoManager.getUndoDescription());
        assertNull(undoManager.getRedoDescription());
    }

    @Test
    @DisplayName("Clear should reset both undo and redo stacks")
    void testClear() {
        undoManager.executeCommand(command1);
        undoManager.clear();

        assertEquals(0, undoManager.getHistorySize());
        assertFalse(undoManager.canUndo());
        assertFalse(undoManager.canRedo());
    }
}
