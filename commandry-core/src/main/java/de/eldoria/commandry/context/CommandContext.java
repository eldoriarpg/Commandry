package de.eldoria.commandry.context;

/**
 * This class is used to pass context other than the user input to the command handlers.
 * It's meant to be extended so all required context data can be accessed in the command handling
 * methods.
 *
 * @param <C> the extending command context type.
 */
public abstract class CommandContext<C extends CommandContext<C>> {
    private final String command;

    /**
     * Creates a new command context for a specific input command.
     *
     * @param command the command which is called with this context.
     */
    public CommandContext(String command) {
        this.command = command;
    }

    /**
     * Returns the command which is called with this context.
     *
     * @return the command.
     */
    public String getCommand() {
        return command;
    }
}
