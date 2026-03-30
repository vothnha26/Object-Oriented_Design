package com.alotra.command;

public interface CartCommand {
    void execute();
    void undo();
    String getDescription();
}
