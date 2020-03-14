package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

/**
 * This exception is used for exceptions occurring during the execution of a command.
 */
public class CommandExecutionException extends CommandException {
    private final String input;

    /**
     * Creates a new exception with a message, an involved command and the raw user input.
     *
     * @param message the exception message.
     * @param command the involved command.
     * @param input   the raw user input which is involved.
     */
    public CommandExecutionException(String message, Command command, String input) {
        super(message, command);
        this.input = input;
    }

    /**
     * Creates a new exception with a message and the raw user input.
     *
     * @param message the exception message.
     * @param input   the raw user input which is involved.
     */
    public CommandExecutionException(String message, String input) {
        super(message);
        this.input = input;
    }

    /**
     * Returns the raw user input involved in this exception.
     *
     * @return the user input.
     */
    public String getInput() {
        return input;
    }
}
