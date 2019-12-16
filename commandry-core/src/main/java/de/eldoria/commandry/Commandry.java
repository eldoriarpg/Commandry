package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Alias;
import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.exception.CommandException;
import de.eldoria.commandry.exception.CommandExecutionException;
import de.eldoria.commandry.exception.CommandRegistrationException;
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
public class Commandry<C> {
    private static final Comparator<Pair<Method, Command>> METHOD_COMPARATOR;
    private static final String NO_MATCHING_COMMAND_FOUND = "No matching command found.";
    private final RootNode root = new RootNode();
    private final ArgumentParser argumentParser = new ArgumentParser();

    static {
        METHOD_COMPARATOR = Comparator
                .comparing((Pair<Method, Command> pair) -> pair.getSecond().ascendants())
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
     * To register a class as command handler, a no-parameter constructor must be
     * accessible (means {@code public}. The value given by {@link Command#value()} has
     * to be a string containing non-whitespace characters only.
     * The value given by {@link Command#ascendants()} has to be a string which is either blank
     * or contains whitespace-separated commands that are already registered or defined in the same class.
     * Throws a {@link CommandRegistrationException} if the command couldn't be registered.
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
            if (pair.getSecond().ascendants().isBlank()) {
                parents = new LinkedList<>();
            } else {
                parents = StringUtils.splitString(pair.getSecond().ascendants(), " ", LinkedList::new);
            }
            addCommand(pair.getFirst(), commandHandler, pair.getSecond(), parents, root);
        }
    }

    /**
     * Returns the argument parser used by this commandry instance.
     * Every changes to the returned object are reflected to this instance,
     * so it can be used to register parsers.
     *
     * @return the argument parser used by this instance.
     */
    public ArgumentParser getArgumentParser() {
        return argumentParser;
    }

    /**
     * Executes a command given as a string reader with a context.
     * The method will read from the raw input to determine which command is called and
     * which arguments are provided.
     *
     * @param reader  the reader holding the raw command input.
     * @param context the context of the command.
     */
    private void execute(StringReader reader, C context) {
        checkEmptyInput(reader);
        Node currentCommand = null;
        ParameterChain parameterChain = null;
        while (reader.canRead()) {
            var word = reader.readWord();
            if (currentCommand == null) {
                // only executed at the beginning, when looking for the top level command
                currentCommand = root.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, List.of(), context);
            } else if (parameterChain.acceptsFurtherArgument()) {
                // Optionals are prioritized before subcommands
                parameterChain.offerArgument(argumentParser.parse(word, parameterChain.getNextType()));
            } else {
                // No Optional argument required anymore, try to find a subcommand
                currentCommand = currentCommand.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                var argumentList = parameterChain.getArgumentList();
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, argumentList, context);
            }
        }
        if (parameterChain.requiresFurtherArgument()) {
            // Can't read anything else from the reader, but more arguments are required.
            reader.reset();
            throw new CommandExecutionException("Too few arguments.", reader.readRemaining());
        }
        parameterChain.completeArguments();
        currentCommand.execute(parameterChain.getArgumentArray());
    }

    /**
     * Adds a command implemented by the given method of the given command handler to this commandry instance.
     * If it is a top level command, means it has no ascendants, it will be added to the root node. Otherwise,
     * it will be added its parent node. This is achieved by traversing all ascendants from the root
     * node down to the parent. If an ascendant node is expected but not found, a
     * {@link CommandRegistrationException} will be thrown.
     *
     * @param method         the method defining the command.
     * @param commandHandler the command handler holding the method.
     * @param command        the annotation of the method which declared it as a command.
     * @param parents        the queue representation of all ascendant commands. Can be empty.
     * @param ascendant      the ascendant command node.
     */
    private void addCommand(Method method, Object commandHandler, Command command,
                            Queue<String> parents, Node ascendant) {
        if (parents.isEmpty()) {
            addChild(method, commandHandler, command, ascendant);
        } else {
            var next = parents.poll();
            var node = ascendant.find(next)
                    .orElseThrow(() -> new CommandRegistrationException("Missing parent node '" + next + "'", command));
            addCommand(method, commandHandler, command, parents, node);
        }
    }

    /**
     * Adds the given method as node child to the given parent. To build the node,
     * aliases of the command are read from the annotation, if available. If the annotation is
     * available but the aliases can't be parsed, a {@link CommandRegistrationException} will be thrown.
     *
     * @param method         the method defining the command.
     * @param commandHandler the command handler holding the method.
     * @param command        the annotation of the method which declared it as a command.
     * @param parent         the direct parent of the command.
     */
    private void addChild(Method method, Object commandHandler, Command command, Node parent) {
        var commandName = command.value();
        var node = new CommandNode(parent, commandName, method, commandHandler, argumentParser);
        var aliases = ReflectionUtils.getAnnotation(Alias.class, method);
        if (aliases.isPresent()) {
            // TODO validate Alias
            String aliasesString = aliases.get().value();
            if (aliasesString.contains(",")) {
                parent.addChild(commandName, aliasesString.split("( )*,( )*"), node);
            } else {
                parent.addChild(commandName, aliasesString, node);
            }
        } else {
            parent.addChild(commandName, node);
        }
    }

    /**
     * Returns a list of methods paired together with their command annotation. It is guaranteed that
     * each pair has a valid method and a valid command annotation. There are no methods in this list
     * which have no command annotation.
     * All methods declared in the given command handler are checked and only filtered out if they don't
     * have a {@link Command} annotation.
     *
     * @param commandHandler the command handler instance.
     * @return a list of all command methods in the given command handler.
     */
    private List<Pair<Method, Command>> getCommandMethods(Object commandHandler) {
        var clazz = commandHandler.getClass();
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(this::methodToPair)
                .filter(Objects::nonNull)
                .sorted(METHOD_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * Pairs a method to itself and its {@link Command} annotation. If the annotation isn't available,
     * {@code null} is returned.
     *
     * @param method the method to pair.
     * @return the pair of the method with its command annotation.
     */
    private Pair<Method, Command> methodToPair(Method method) {
        var a = ReflectionUtils.getAnnotation(Command.class, method);
        if (a.isEmpty()) return null;
        // TODO validate Command
        return new Pair<>(method, a.get());
    }

    /**
     * Offers a list of arguments to a parameter chain. This can be used if a new chain has to be
     * satisfied, but arguments were already parsed. This method automatically detects if
     * the context is required or not.
     *
     * @param targetChain   the parameter chain to fill with parsed arguments.
     * @param currentOffers the available parsed arguments to offer.
     * @param context       the context to offer, if required.
     */
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
