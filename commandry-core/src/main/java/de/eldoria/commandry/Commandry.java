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
    public final void runCommand(C context, String input) {
        var reader = new StringReader(input);
        checkEmptyInput(reader);
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
                parameterChain.offerArgument(word);
            } else {
                currentCommand = currentCommand.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                var argumentList = parameterChain.getArgumentList();
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, argumentList, context);
            }
        }
        if (parameterChain != null && parameterChain.requiresFurtherArgument()) {
            reader.reset();
            throw new CommandExecutionException("Too few arguments.", reader.readRemaining());
        }
        parameterChain.completeArguments();
        currentCommand.execute(parameterChain.getArgumentArray());
    }

    private void addCommand(Method method, Object commandHandler, String command, Queue<String> parents, Node parent) {
        if (parents.isEmpty()) {
            parent.addChild(command, new CommandNode(command, method, commandHandler));
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

    private void offerAll(ParameterChain targetChain, List<Object> currentOffers, C context) {
        /*if (targetChain.getNextType().isInstance(context)) {
            if (currentOffers.isEmpty() || currentOffers.get(0).equals(context)) {

            }
        }*/


        if (!currentOffers.isEmpty()) {
            if (targetChain.getNextType().isInstance(context) == currentOffers.get(0).equals(context)) {
                targetChain.offerAll(currentOffers);
                return;
            }
        }
        if (targetChain.acceptsFurtherArgument() && targetChain.getNextType().isInstance(context)) {
            targetChain.offerArgument(context);
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
