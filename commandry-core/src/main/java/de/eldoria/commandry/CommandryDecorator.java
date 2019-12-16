package de.eldoria.commandry;

public class CommandryDecorator<C> extends Commandry<C> {
    protected final Commandry<C> delegate;

    public CommandryDecorator(Commandry<C> delegate) {
        this.delegate = delegate;
    }
}
