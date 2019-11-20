package de.eldoria.commandry;

import de.eldoria.commandry.context.CommandContext;

public class CommandryDecorator<C extends CommandContext<C>> extends Commandry<C> {
    protected final Commandry<C> delegate;

    public CommandryDecorator(Commandry<C> delegate) {
        this.delegate = delegate;
    }
}
