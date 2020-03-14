package de.eldoria.commandry.exception;

import de.eldoria.commandry.annotation.Command;

/**
 * This exception is thrown if a command couldn't be registered successfully.
 */
public class CommandRegistrationException extends RuntimeCommandException {

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

    /**
     * Creates a new exception wrapping another one with a specific command.
     *
     * @param cause   the throwable causing this exception.
     * @param command the command involved in the exception.
     */
    public CommandRegistrationException(Throwable cause, Command command) {
        super("Couldn't register command", cause, command);
    }

    /**
     * Creates a new exception wrapping another one with a given message.
     *
     * @param message the message describing the exception.
     * @param cause   the throwable causing this exception.
     */
    public CommandRegistrationException(String message, Throwable cause) {
        super(message, cause);
    }
}
