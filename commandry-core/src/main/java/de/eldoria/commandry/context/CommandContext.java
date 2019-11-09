package de.eldoria.commandry.context;

public abstract class CommandContext<C extends CommandContext<C>> {
    private final String command;

    public CommandContext(String command) {
        this.command = command;
    }

    public String getCommand() {
        return command;
    }
}
