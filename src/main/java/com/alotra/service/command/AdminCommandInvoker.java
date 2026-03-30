package com.alotra.service.command;

import org.springframework.stereotype.Service;
import java.util.ArrayDeque;
import java.util.Deque;

@Service
public class AdminCommandInvoker {
    private final Deque<AdminCommand> history = new ArrayDeque<>();
    private static final int MAX_HISTORY = 50;

    public void execute(AdminCommand command) {
        command.execute();
        history.push(command);
        if (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
        System.out.println("[Admin Action] " + command.getDescription());
    }

    public boolean undo() {
        if (history.isEmpty()) return false;
        AdminCommand last = history.pop();
        last.undo();
        System.out.println("[Admin Undo] " + last.getDescription());
        return true;
    }
}
