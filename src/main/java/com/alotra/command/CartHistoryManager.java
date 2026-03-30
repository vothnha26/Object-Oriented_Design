package com.alotra.command;

import java.util.Stack;
import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.SessionScope;

@Component
@SessionScope
public class CartHistoryManager {
    private final Stack<CartCommand> undoStack = new Stack<>();
    private final Stack<CartCommand> redoStack = new Stack<>();

    public void executeCommand(CartCommand command) {
        command.execute();
        undoStack.push(command);
        redoStack.clear(); // Clear redo on new action
    }

    public void clearHistory() {
        undoStack.clear();
        redoStack.clear();
    }

    public void undo() {
        if (!undoStack.isEmpty()) {
            CartCommand last = undoStack.pop();
            last.undo();
            redoStack.push(last);
        }
    }

    public void redo() {
        if (!redoStack.isEmpty()) {
            CartCommand command = redoStack.pop();
            command.execute();
            undoStack.push(command);
        }
    }
    
    public boolean canUndo() { return !undoStack.isEmpty(); }
    public boolean canRedo() { return !redoStack.isEmpty(); }
    
    public String getLastActionDescription() {
        return undoStack.isEmpty() ? "Không có" : undoStack.peek().getDescription();
    }
}
