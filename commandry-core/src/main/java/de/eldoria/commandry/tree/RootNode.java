package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.lang.reflect.Parameter;

/**
 * This class is used to represent a special node which isn't a command node.
 * It's the root of the node tree which cannot be executed. A top level command should be
 * added as child of this one.
 */
public class RootNode extends Node {
    private static final ParameterChain NULL_PARAMETER_CHAIN = new ParameterChain(new Parameter[0], null);

    /**
     * Creates a root node instance with its parent being null.
     */
    public RootNode() {
        super(null);
    }

    @Override
    public void execute(Object[] args) {
        throw new UnsupportedOperationException("Root node cannot be executed.");
    }

    @Override
    public String getName() {
        return "";
    }

    @Override
    public ParameterChain getParameterChain() {
        return NULL_PARAMETER_CHAIN;
    }
}
