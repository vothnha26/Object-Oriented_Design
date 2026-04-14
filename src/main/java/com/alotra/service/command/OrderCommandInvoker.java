package com.alotra.service.command;

import org.springframework.stereotype.Service;
import org.springframework.web.context.annotation.SessionScope;

import java.util.ArrayDeque;
import java.util.Deque;

@Service
@SessionScope
public class OrderCommandInvoker {
    private static final int MAX_HISTORY = 50;

    private final Deque<AdminCommand> history = new ArrayDeque<>();

    public void execute(AdminCommand command) {
        command.execute();
        history.push(command);
        if (history.size() > MAX_HISTORY) {
            history.removeLast();
        }
        System.out.println("[Order Action] " + command.getDescription());
    }

    public boolean undo() {
        if (history.isEmpty()) {
            return false;
        }
        AdminCommand last = history.pop();
        last.undo();
        System.out.println("[Order Undo] " + last.getDescription());
        return true;
    }
}
