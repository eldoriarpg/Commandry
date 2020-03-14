package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.CheckedInstanceMethod;
import de.eldoria.commandry.util.reflection.ParameterChain;


/**
 * This class represents a command dispatcher as a node in a node tree.
 * This is the mainly used node type in a node tree.
 */
class CommandDispatcherNode extends Node {
    private final String name;
    private final CheckedInstanceMethod<?> method;

    /**
     * Creates a new instance. The {@code commandHandler} object
     * must match the type of the type in which {@code method} is declared, otherwise
     * an exception may be thrown.
     *
     * @param parent the parent node.
     * @param name   the name of the command.
     * @param method the method of the command.
     */
    CommandDispatcherNode(Node parent, String name, CheckedInstanceMethod<?> method) {
        super(parent);
        this.name = name;
        this.method = method;
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
