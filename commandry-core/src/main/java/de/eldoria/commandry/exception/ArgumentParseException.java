package de.eldoria.commandry.exception;

/**
 * This exception is used for parsing if an input couldn't be parsed correctly.
 */
public class ArgumentParseException extends CommandExecutionException {

    /**
     * Creates a new exception instance with a specifying message and the input.
     *
     * @param message the message describing the exception.
     * @param input   the input causing the exception.
     */
    public ArgumentParseException(String message, String input) {
        super(message, input);
    }
}
