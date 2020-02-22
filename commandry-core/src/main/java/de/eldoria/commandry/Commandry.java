package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.dispatch.CommandDispatcher;
import de.eldoria.commandry.dispatch.SimpleCommandDispatcher;
import de.eldoria.commandry.exception.CommandExecutionException;
import de.eldoria.commandry.exception.CommandRegistrationException;
import de.eldoria.commandry.parser.ParserManager;
import de.eldoria.commandry.registration.CommandRegistrationProcessor;
import de.eldoria.commandry.tree.Node;
import de.eldoria.commandry.util.reflection.ReflectionUtils;

import java.util.function.BiFunction;

/**
 * This class is the core of the framework. An instance of it can be used to register commands
 * and run commands by providing the raw input string.
 *
 * @param <C> the type of the context.
 */
public class Commandry<C> {

    private final Node root;
    private final ArgumentParser argumentParser;
    private final CommandDispatcher<C> commandDispatcher;

    /**
     * Creates a new Commandry instance with a supplier for the dispatcher module.
     *
     * @param dispatcherSupplier the supplier for the dispatcher module.
     */
    public Commandry(BiFunction<ParserManager, Node, CommandDispatcher<C>> dispatcherSupplier) {
        this.root = Node.create();
        this.argumentParser = new ArgumentParser();
        this.commandDispatcher = dispatcherSupplier.apply(argumentParser, root);
    }

    /**
     * Creates a new Commandry instance with a simple command dispatcher implementation.
     */
    public Commandry() {
        this.root = Node.create();
        this.argumentParser = new ArgumentParser();
        this.commandDispatcher = new SimpleCommandDispatcher<>(argumentParser, root);
    }

    /**
     * Runs a command given by the input with the provided context. If the command
     * couldn't be handled correctly, a {@link CommandExecutionException} will be thrown.
     *
     * @param context the context to delegate to the command handler.
     * @param input   the raw input string.
     * @throws CommandExecutionException if the command couldn't be dispatched successfully.
     */
    public void dispatchCommand(C context, String input) throws CommandExecutionException {
        commandDispatcher.dispatch(input, context);
    }

    /**
     * Registers a new class as command handler. Each public instance method
     * which is annotated with {@link Command} will be loaded as command.
     * To register a class as command handler, a no-parameter constructor must be
     * accessible (means {@code public}. The value given by {@link Command#value()} has
     * to be a string containing non-whitespace characters only.
     * The value given by {@link Command#ascendants()} has to be a string which is either blank
     * or contains whitespace-separated commands that are already registered or defined in the same class.
     *
     * @param clazz the class to register as command handler.
     * @param <T>   the type of the class.
     */
    public final <T> void registerCommands(Class<T> clazz) {
        var commandHandler = ReflectionUtils.newInstance(clazz)
                .orElseThrow(() -> new CommandRegistrationException("Failed to register commands for class %s. "
                        + "No instance could be created. Is the default constructor public?"));
        var registration = new CommandRegistrationProcessor(clazz, commandHandler, argumentParser);
        registration.register(root);
    }

    /**
     * Returns the argument parser used by this commandry instance.
     * Every changes to the returned object are reflected to this instance,
     * so it can be used to register parsers.
     *
     * @return the argument parser used by this instance.
     */
    public ParserManager getArgumentParser() {
        return argumentParser;
    }
}
