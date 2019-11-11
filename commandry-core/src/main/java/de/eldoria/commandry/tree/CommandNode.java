package de.eldoria.commandry.tree;

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
     * @param name           the name of the command.
     * @param method         the method of the command.
     * @param commandHandler the object in which the method can be called.
     */
    public CommandNode(String name, Method method, Object commandHandler) {
        this.name = name;
        this.method = CheckedInstanceMethod.of(method, commandHandler);
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
