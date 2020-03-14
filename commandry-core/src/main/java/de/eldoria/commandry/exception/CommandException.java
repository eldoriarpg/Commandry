package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

/**
 * This exception is used to represent a general exception caused by the
 * framework.
 */
public class CommandException extends Exception {
    private Command command;

    /**
     * Creates a new exception with the given message for a given command.
     *
     * @param message the exception message.
     * @param command the command which is involved.
     */
    public CommandException(String message, Command command) {
        super(message);
        this.command = command;
    }

    /**
     * Creates a new exception wrapping another one with the given message for a given command.
     *
     * @param message the exception message.
     * @param cause   the throwable causing this exception.
     * @param command the command which is involved.
     */
    public CommandException(String message, Throwable cause, Command command) {
        super(message, cause);
        this.command = command;
    }

    /**
     * Creates a new exception with the given message.
     *
     * @param message the exception message.
     */
    public CommandException(String message) {
        super(message);
    }

    /**
     * Returns the involved command. Since not every time the exception is used
     * a specific command is involved, this method may return null.
     *
     * @return the involved command.
     */
    public Command getCommand() {
        return command;
    }
}
