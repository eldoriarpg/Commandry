package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

/**
 * This exception is thrown if a command couldn't be registered successfully.
 */
public class CommandRegistrationException extends CommandException {

    /**
     * Creates a new exception with a given message and a command.
     *
     * @param message the message describing the exception.
     * @param command the command involved in the exception.
     */
    public CommandRegistrationException(String message, Command command) {
        super(message, command);
    }

    /**
     * Creates a new exception with a given message.
     *
     * @param message the message describing the exception.
     */
    public CommandRegistrationException(String message) {
        super(message);
    }
}
