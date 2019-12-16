package de.eldoria.commandry;

import java.util.concurrent.CompletableFuture;

public class AsyncCommandry<C> extends CommandryDecorator<C> {

    public AsyncCommandry(Commandry<C> delegate) {
        super(delegate);
    }

    @Override
    public void runCommand(C context, String input) {
        CompletableFuture.runAsync(() -> delegate.runCommand(context, input));
    }
}
