package com.alotra.service.command;

public interface AdminCommand {
    void execute();
    void undo();
    String getDescription();
}
