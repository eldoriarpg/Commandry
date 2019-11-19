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
import de.eldoria.commandry.util.reflection.ParameterChain;
import de.eldoria.commandry.util.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * This class is the core of the framework. An instance of it can be used to register commands
 * and run commands by providing the raw input string.
 *
 * @param <C> the type of the context.
 */
public class Commandry<C extends CommandContext<C>> {
    private static final Comparator<Pair<Method, Command>> METHOD_COMPARATOR;
    private static final String NO_MATCHING_COMMAND_FOUND = "No matching command found.";
    private final RootNode root = new RootNode();
    private final ArgumentParser argumentParser = new ArgumentParser();

    static {
        METHOD_COMPARATOR = Comparator
                .comparing((Pair<Method, Command> pair) -> pair.getSecond().parents())
                .thenComparing(pair -> pair.getSecond().value());
    }

    /**
     * Runs a command given by the input with the provided context. If the command
     * couldn't be handled correctly, a {@link CommandExecutionException} will be thrown.
     *
     * @param context the context to delegate to the command handler.
     * @param input   the raw input string.
     */
    public void runCommand(C context, String input) {
        var reader = new StringReader(input);
        execute(reader, context);
    }

    /**
     * Registers a new class as command handler. Each public instance method
     * which is annotated with {@link Command} will be loaded as command.
     *
     * @param clazz the class to register as command handler.
     * @param <T>   the type of the class.
     */
    public final <T> void registerCommands(Class<T> clazz) {
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

    /**
     * This method gets called internally when a command is registered. This can be used
     * for systems that require an additional registration. In the default implementation,
     * nothing will happen.
     *
     * @param command the command string that got registered.
     */
    protected void onCommandRegistration(String command) {

    }

    private void execute(StringReader reader, C context) {
        checkEmptyInput(reader);
        Node currentCommand = null;
        ParameterChain parameterChain = null;
        while (reader.canRead()) {
            var word = reader.readWord();
            if (currentCommand == null) {
                currentCommand = root.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, List.of(), context);
            } else if (parameterChain.acceptsFurtherArgument()) {
                offerWisely(parameterChain, word);
            } else {
                currentCommand = currentCommand.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                var argumentList = parameterChain.getArgumentList();
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, argumentList, context);
            }
        }
        if (parameterChain.requiresFurtherArgument()) {
            reader.reset();
            throw new CommandExecutionException("Too few arguments.", reader.readRemaining());
        }
        parameterChain.completeArguments();
        currentCommand.execute(parameterChain.getArgumentArray());
    }

    private void addCommand(Method method, Object commandHandler, String command, Queue<String> parents, Node parent) {
        if (parents.isEmpty()) {
            parent.addChild(command, new CommandNode(command, method, commandHandler, argumentParser));
            onCommandRegistration(command);
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

    private void offerWisely(ParameterChain chain, String input) {
        if (!chain.getNextType().isPrimitive()) {
            chain.offerArgument(argumentParser.parse(input, chain.getNextType()));
        } else if (chain.getNextType() == boolean.class) {
            chain.offerArgument(argumentParser.parse(input, boolean.class).booleanValue());
        } else if (chain.getNextType() == int.class) {
            chain.offerArgument(argumentParser.parse(input, int.class).intValue());
        } else if (chain.getNextType() == long.class) {
            chain.offerArgument(argumentParser.parse(input, long.class).longValue());
        } else if (chain.getNextType() == byte.class) {
            chain.offerArgument(argumentParser.parse(input, byte.class).byteValue());
        } else if (chain.getNextType() == float.class) {
            chain.offerArgument(argumentParser.parse(input, float.class).floatValue());
        } else if (chain.getNextType() == double.class) {
            chain.offerArgument(argumentParser.parse(input, double.class).doubleValue());
        }
    }

    private void offerAll(ParameterChain targetChain, List<Object> currentOffers, C context) {
        if (!targetChain.acceptsFurtherArgument() && currentOffers.isEmpty()) {
            return;
        }
        if (targetChain.getNextType().isInstance(context)) { // chain requires context
            if (currentOffers.isEmpty() || !currentOffers.get(0).equals(context)) {
                targetChain.offerArgument(context);
            }
            targetChain.offerAll(currentOffers);
        } else {
            List<Object> offers;
            if (!currentOffers.isEmpty() && currentOffers.get(0).equals(context)) {
                // ignore context
                offers = currentOffers.subList(1, currentOffers.size());
            } else {
                offers = currentOffers;
            }
            targetChain.offerAll(offers);
        }
    }

    private void checkEmptyInput(StringReader reader) {
        if (!reader.canRead()) {
            throw new CommandExecutionException("No empty input allowed.", "");
        }
    }

    private Supplier<CommandExecutionException> noMatchingCommandFound(String word) {
        return () -> new CommandExecutionException(NO_MATCHING_COMMAND_FOUND, word);
    }
}
