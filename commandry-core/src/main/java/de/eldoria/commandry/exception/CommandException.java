package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

public class CommandException extends RuntimeException {
    private Command command;

    public CommandException(String message, Command command) {
        super(message);
        this.command = command;
    }

    public CommandException(String message) {
        super(message);
    }

    public Command getCommand() {
        return command;
    }
}
