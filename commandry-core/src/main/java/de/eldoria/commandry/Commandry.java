package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.context.CommandContext;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

public class Commandry<C extends CommandContext<C>, R> {
    private static final Comparator<Pair<Method, Command>> METHOD_COMPARATOR;
    private final RootNode root = new RootNode();

    static {
        METHOD_COMPARATOR = Comparator
                .comparing((Pair<Method, Command> pair) -> pair.getSecond().parents())
                .thenComparing(pair -> pair.getSecond().value());
    }

    public R runCommand(C context, String input) {
        // TODO context integration
        StringReader reader = new StringReader(input);
        if (!reader.canRead()) {
            throw new IllegalArgumentException("No input given.");
        }
        Node first = root.find(reader.readWord());
        if (first == null) {
            throw new IllegalArgumentException("Command not found.");
        }
        runCommand(reader, first, new LinkedList<>());
        return null;
    }

    public void registerCommands(Class<?> clazz) {
        var commandHandler = ReflectionUtils.newInstance(clazz);
        if (commandHandler == null) {
            // TODO
            return;
        }
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

    private void runCommand(StringReader reader, Node current, List<Object> args) {
        var parameterChain = current.getParameterChain();
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
                // TODO throw exception, too few arguments
                return;
            }
            next = reader.readWord();
        }
        var possibleSubCommand = next;
        AtomicBoolean b = new AtomicBoolean();
        current.accept(node -> {
            if (node.getName().equals(possibleSubCommand)) {
                b.set(true);
                runCommand(reader, node, parameterChain.getArgumentList());
            }
        });
        if (!b.get()) {
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
            String next = parents.poll();
            parent.accept(node -> {
                if (node.getName().equals(next)) {
                    addCommand(method, commandHandler, command, parents, node);
                }
            });
        }
    }

    private List<Pair<Method, Command>> getCommandMethods(Object commandHandler) {
        var clazz = commandHandler.getClass();
        // TODO method must have context argument
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
