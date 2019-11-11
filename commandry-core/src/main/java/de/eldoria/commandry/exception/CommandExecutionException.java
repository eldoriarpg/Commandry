package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

public class CommandExecutionException extends CommandException {
    private final String input;

    public CommandExecutionException(String message, Command command, String input) {
        super(message, command);
        this.input = input;
    }

    public CommandExecutionException(String message, String input) {
        super(message);
        this.input = input;
    }

    public String getInput() {
        return input;
    }
}
