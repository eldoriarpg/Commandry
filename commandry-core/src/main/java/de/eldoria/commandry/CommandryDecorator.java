package de.eldoria.commandry;

/**
 * This class can be extended to modify or extend functionality
 * of {@link Commandry}.
 *
 * @param <C> the type of context of the decorated commandry instance.
 */
public abstract class CommandryDecorator<C> extends Commandry<C> {

    /**
     * The underlying commandry instance to which sub classes can delegate their method
     * calls to.
     */
    protected final Commandry<C> delegate;

    /**
     * Creates a new commandry decorator instance with an underlying commandry instance.
     *
     * @param delegate the underlying commandry instance.
     */
    public CommandryDecorator(Commandry<C> delegate) {
        this.delegate = delegate;
    }
}
