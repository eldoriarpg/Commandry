package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.context.CommandContext;
import de.eldoria.commandry.exception.CommandException;
import de.eldoria.commandry.exception.CommandExecutionException;
import de.eldoria.commandry.tree.CommandNode;
import de.eldoria.commandry.tree.Node;
import de.eldoria.commandry.tree.RootNode;
import de.eldoria.commandry.util.Pair;
import de.eldoria.commandry.util.StringReader;
import de.eldoria.commandry.util.StringUtils;
import de.eldoria.commandry.util.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.stream.Collectors;

/**
 * This class is the core of the framework. An instance of it can be used to register commands
 * and run commands by providing the raw input string.
 *
 * @param <C> the type of the context.
 */
public class Commandry<C extends CommandContext<C>> {
    private static final Comparator<Pair<Method, Command>> METHOD_COMPARATOR;

    static {
        METHOD_COMPARATOR = Comparator
                .comparing((Pair<Method, Command> pair) -> pair.getSecond().parents())
                .thenComparing(pair -> pair.getSecond().value());
    }

    private final RootNode root = new RootNode();

    /**
     * Runs a command given by the input with the provided context. If the command
     * couldn't be handled correctly, a {@link CommandExecutionException} will be thrown.
     *
     * @param context the context to delegate to the command handler.
     * @param input   the raw input string.
     */
    public void runCommand(C context, String input) {
        var reader = new StringReader(input);
        if (!reader.canRead()) {
            throw new CommandExecutionException("No input given.", null);
        }
        var first = root.find(reader.readWord());
        if (first.isEmpty()) {
            reader.reset();
            throw new CommandExecutionException("Command not found.", reader.readRemaining());
        }
        execute(reader, first.get(), new LinkedList<>(), context);
    }

    /**
     * Registers a new class as command handler. Each public instance method
     * which is annotated with {@link Command} will be loaded as command.
     *
     * @param clazz the class to register as command handler.
     * @param <T>   the type of the class.
     */
    public <T> void registerCommands(Class<T> clazz) {
        var commandHandler = ReflectionUtils.newInstance(clazz)
                .orElseThrow(() -> new CommandException("Failed to register commands for class %s. "
                        + "No instance could be created. Is the default constructor public?"));
        var commandMethods = getCommandMethods(commandHandler);
        for (var pair : commandMethods) {
            LinkedList<String> parents;
            if (pair.getSecond().parents().isBlank()) {
                parents = new LinkedList<>();
            } else {
                parents = StringUtils.splitString(pair.getSecond().parents(), " ", LinkedList::new);
            }
            addCommand(pair.getFirst(), commandHandler, pair.getSecond().value(), parents, root);
        }
    }

    /*
        Explanation:
        Staring with the first word after the command name, we call this method with
            (1) reader being the reader for the whole command input
            (2) current is the top level command which was called
            (3) args is an empty list meant for parsed arguments
            (4) context is the context object to pass to the parameter chain if possible
        First, we try to offer the context to the parameter chain of (2). That will be the case
        if it's the first method parameter. Afterwards, we're check if (1) has remaining content.
        If not, we're trying to complete the parameter chain (optional parameters will be used if available).
        If yes, we're start reading the next words and offer them as long as the parameter chain requires more
        arguments.
        Then, when a further word is required, sub commands are prioritised over optional parameters.
     */
    private void execute(StringReader reader, Node current, List<Object> args, C context) {
        var parameterChain = current.getParameterChain();
        var argsWillOffer = !args.isEmpty() && args.get(0) == context;
        if (!argsWillOffer && parameterChain.acceptsFurtherArgument()
                && parameterChain.getNextType().isInstance(context)) {
            parameterChain.offerArgument(context);
        }
        if (!reader.canRead()) {
            parameterChain.completeArguments();
            current.execute(parameterChain.getArgumentArray());
            return;
        }
        var next = reader.readWord();
        parameterChain.offerAll(args);
        while (parameterChain.requiresFurtherArgument()) {
            parameterChain.offerArgument(next); // TODO parse
            if (!reader.canRead()) {
                reader.reset();
                throw new CommandExecutionException("Wrong arguments, cannot perform command.", reader.readRemaining());
            }
            next = reader.readWord();
        }
        var possibleSubCommand = next;
        var node = current.find(next);
        node.ifPresent(n -> execute(reader, n, parameterChain.getArgumentList(), context));
        if (node.isEmpty()) {
            next = possibleSubCommand;
            while (parameterChain.acceptsFurtherArgument()) {
                parameterChain.offerArgument(next); // TODO parse
                if (!reader.canRead()) {
                    break;
                }
                next = reader.readWord();
            }
            parameterChain.completeArguments();
            current.execute(parameterChain.getArgumentArray());
        }
    }

    private void addCommand(Method method, Object commandHandler, String command, Queue<String> parents, Node parent) {
        if (parents.isEmpty()) {
            parent.addChild(command, new CommandNode(command, method, commandHandler));
        } else {
            var next = parents.poll();
            var node = parent.find(next);
            node.ifPresent(n -> addCommand(method, commandHandler, command, parents, n));
        }
    }

    private List<Pair<Method, Command>> getCommandMethods(Object commandHandler) {
        var clazz = commandHandler.getClass();
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(this::methodToPair)
                .filter(Objects::nonNull)
                .sorted(METHOD_COMPARATOR)
                .collect(Collectors.toList());
    }

    private Pair<Method, Command> methodToPair(Method method) {
        var a = ReflectionUtils.getAnnotation(Command.class, method);
        if (a.isEmpty()) return null;
        return new Pair<>(method, a.get());
    }
}
