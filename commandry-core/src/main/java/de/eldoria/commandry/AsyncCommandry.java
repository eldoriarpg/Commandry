package de.eldoria.commandry;

import de.eldoria.commandry.context.CommandContext;

import java.util.concurrent.CompletableFuture;

public class AsyncCommandry<C extends CommandContext<C>> extends CommandryDecorator<C> {

    public AsyncCommandry(Commandry<C> delegate) {
        super(delegate);
    }

    @Override
    public void runCommand(C context, String input) {
        CompletableFuture.runAsync(() -> super.runCommand(context, input));
    }
}
