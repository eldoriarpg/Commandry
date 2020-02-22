package de.eldoria.commandry.dispatch;

import de.eldoria.commandry.exception.CommandExecutionException;
import de.eldoria.commandry.util.StringReader;

/**
 * Implementations of this class handle the execution of commands.
 *
 * @param <C> the context type.
 */
public interface CommandDispatcher<C> {

    /**
     * Executes a command given as a string reader with a context.
     * The method will read from the raw input to determine which command is called and
     * which arguments are provided.
     *
     * @param reader  the reader holding the raw command input.
     * @param context the context of the command.
     * @throws CommandExecutionException if the command couldn't be executed successfully.
     */
    void dispatch(StringReader reader, C context) throws CommandExecutionException;

    /**
     * Executes a command given as a string reader with a context.
     * The method will read from the raw input to determine which command is called and
     * which arguments are provided.
     *
     * @param input   the reader holding the raw command input.
     * @param context the context of the command.
     * @throws CommandExecutionException if the command couldn't be executed successfully.
     */
    default void dispatch(String input, C context) throws CommandExecutionException {
        dispatch(new StringReader(input), context);
    }
}
