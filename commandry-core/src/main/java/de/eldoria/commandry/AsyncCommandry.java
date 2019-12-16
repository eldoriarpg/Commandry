package de.eldoria.commandry;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;

/**
 * This class is an async implementation of {@link Commandry}. It wraps a source Commandry
 * instance and delegates the {@link #runCommand(Object, String)} method call to an available
 * Thread of the {@link ForkJoinPool#commonPool()}. Registering commands and context although
 * is still called synchronous.
 *
 * @param <C> the context type.
 */
public class AsyncCommandry<C> extends CommandryDecorator<C> {

    /**
     * Creates a new async commandry instance.
     *
     * @param delegate the underlying commandry instance.
     */
    public AsyncCommandry(Commandry<C> delegate) {
        super(delegate);
    }

    @Override
    public void runCommand(C context, String input) {
        CompletableFuture.runAsync(() -> delegate.runCommand(context, input));
    }
}
