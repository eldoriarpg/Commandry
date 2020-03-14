package de.eldoria.commandry.registration;

import de.eldoria.commandry.ArgumentParser;
import de.eldoria.commandry.annotation.Alias;
import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.exception.ArgumentParseException;
import de.eldoria.commandry.exception.CommandRegistrationException;
import de.eldoria.commandry.tree.Node;
import de.eldoria.commandry.util.Pair;
import de.eldoria.commandry.util.StringUtils;
import de.eldoria.commandry.util.reflection.CheckedInstanceMethod;
import de.eldoria.commandry.util.reflection.ReflectionUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Queue;
import java.util.function.Consumer;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * This class handles the registration of commands defined in one single class.
 * It can be extended to modify the registration behaviour.
 *
 * @param <T> the type of the command handler instance.
 */
public class CommandRegistrationProcessor<T> {
    private static final Pattern VALID_COMMAND_PATTERN = Pattern.compile("[^\\s,.<>\\[\\]{}]+");
    private static final Comparator<Pair<Method, Command>> METHOD_COMPARATOR;
    private final Class<T> commandHandlerClass;
    private final T commandHandlerInstance;
    private final ArgumentParser parser;

    static {
        METHOD_COMPARATOR = Comparator
                .comparing((Pair<Method, Command> pair) -> pair.getSecond().ascendants())
                .thenComparing(pair -> pair.getSecond().value());
    }

    /**
     * Creates a new registration processor.
     *
     * @param commandHandlerClass    the instance of the command handler.
     * @param commandHandlerInstance the class of the command handler.
     * @param parser                 the argument parser for default values.
     */
    public CommandRegistrationProcessor(Class<T> commandHandlerClass, T commandHandlerInstance,
                                        ArgumentParser parser) {
        this.commandHandlerClass = commandHandlerClass;
        this.commandHandlerInstance = commandHandlerInstance;
        this.parser = parser;
    }

    /**
     * Registers the command to a specific node. The node is seen as the root node.
     *
     * @param root the node seen as the root for the registration process.
     */
    public void register(Node root) {
        var commandMethods = getCommandMethods(commandHandlerClass);
        for (var pair : commandMethods) {
            LinkedList<String> parents;
            if (pair.getSecond().ascendants().isBlank()) {
                parents = new LinkedList<>();
            } else {
                // as commandMethods is sorted, all parents are already registered
                parents = StringUtils.splitString(pair.getSecond().ascendants(), " ", LinkedList::new);
            }
            try {
                addCommand(pair.getFirst(), pair.getSecond(), parents, root);
            } catch (ArgumentParseException e) {
                throw new CommandRegistrationException("Couldn't register command.", e);
            }
        }
    }


    /**
     * Adds a command implemented by the given method of the given command handler to this commandry instance.
     * If it is a top level command, means it has no ascendants, it will be added to the root node. Otherwise,
     * it will be added its parent node. This is achieved by traversing all ascendants from the root
     * node down to the parent. If an ascendant node is expected but not found, a
     * {@link CommandRegistrationException} will be thrown.
     *
     * @param method    the method defining the command.
     * @param command   the annotation of the method which declared it as a command.
     * @param parents   the queue representation of all ascendant commands. Can be empty.
     * @param ascendant the ascendant command node.
     */
    private void addCommand(Method method, Command command,
                            Queue<String> parents, Node ascendant) throws ArgumentParseException {
        if (parents.isEmpty()) {
            addChild(method, command, ascendant);
        } else {
            var next = parents.poll();
            var node = ascendant.find(next)
                    .orElseThrow(() -> new CommandRegistrationException("Missing parent node '" + next + "'", command));
            addCommand(method, command, parents, node);
        }
    }

    /**
     * Adds the given method as node child to the given parent. To build the node,
     * aliases of the command are read from the annotation, if available. If the annotation is
     * available but the aliases can't be parsed, a {@link CommandRegistrationException} will be thrown.
     *
     * @param method  the method defining the command.
     * @param command the annotation of the method which declared it as a command.
     * @param parent  the direct parent of the command.
     */
    private void addChild(Method method, Command command, Node parent)
            throws ArgumentParseException {
        var commandName = command.value();
        var aliases = ReflectionUtils.getAnnotation(Alias.class, method);
        var checkedMethod = CheckedInstanceMethod.of(method, commandHandlerInstance, parser.parseDefaults(method));
        if (aliases.isPresent()) {
            String aliasesString = aliases.get().value();
            if (aliasesString.contains(",")) {
                String[] aliasArray = aliasesString.split("( )*,( )*");
                Arrays.stream(aliasArray).forEach(this::checkCommandName);
                parent.addChild(commandName, aliasArray, checkedMethod);
            } else {
                checkCommandName(aliasesString);
                parent.addChild(commandName, aliasesString, checkedMethod);
            }
        } else {
            parent.addChild(commandName, checkedMethod);
        }
    }

    /**
     * Returns a list of methods paired together with their command annotation. It is guaranteed that
     * each pair has a valid method and a valid command annotation. There are no methods in this list
     * which have no command annotation.
     * All methods declared in the given command handler are checked and only filtered out if they don't
     * have a {@link Command} annotation.
     *
     * @param clazz the command handler class.
     * @return a list of all command methods in the given command handler.
     */
    private List<Pair<Method, Command>> getCommandMethods(Class<?> clazz) {
        return Arrays.stream(clazz.getDeclaredMethods())
                .map(method -> methodToPair(method, Command.class, command -> checkCommandName(command.value())))
                .filter(Objects::nonNull)
                .sorted(METHOD_COMPARATOR)
                .collect(Collectors.toList());
    }

    /**
     * Pairs a method to itself and its specified annotation. If the annotation isn't available
     * on that method, {@code null} is returned.
     *
     * @param method the method to pair.
     * @return the pair of the method with its annotation.
     */
    private <A extends Annotation> Pair<Method, A> methodToPair(Method method, Class<A> annotationClass,
                                                                Consumer<A> annotationConsumer) {
        var a = ReflectionUtils.getAnnotation(annotationClass, method);
        if (a.isEmpty()) return null; // no annotation found, ignore method
        var annotation = a.get();
        annotationConsumer.accept(annotation);
        return new Pair<>(method, annotation);
    }


    private void checkCommandName(String name) {
        if (!VALID_COMMAND_PATTERN.matcher(name).matches()) {
            throw new CommandRegistrationException("Invalid command name: " + name);
        }
    }
}
