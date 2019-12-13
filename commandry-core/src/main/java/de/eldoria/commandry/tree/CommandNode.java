package de.eldoria.commandry.tree;

import de.eldoria.commandry.ArgumentParser;
import de.eldoria.commandry.util.reflection.CheckedInstanceMethod;
import de.eldoria.commandry.util.reflection.ParameterChain;

import java.lang.reflect.Method;

/**
 * This class represents a command as a node in a node tree.
 * This is the mainly used node type in a node tree.
 */
public class CommandNode extends Node {
    private final String name;
    private final CheckedInstanceMethod method;

    /**
     * Creates a new instance. The {@code commandHandler} object
     * must match the type of the type in which {@code method} is declared, otherwise
     * an exception may be thrown.
     *
     * @param parent         the parent node.
     * @param name           the name of the command.
     * @param method         the method of the command.
     * @param commandHandler the object in which the method can be called.
     * @param argumentParser the argument parser for optional parameters.
     */
    public CommandNode(Node parent, String name, Method method, Object commandHandler, ArgumentParser argumentParser) {
        super(parent);
        this.name = name;
        this.method = CheckedInstanceMethod.of(method, commandHandler, argumentParser);
    }

    @Override
    public void execute(Object[] args) {
        method.invoke(args);
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public ParameterChain getParameterChain() {
        return method.getParameterChain();
    }
}
