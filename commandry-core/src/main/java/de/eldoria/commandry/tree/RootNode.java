package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.lang.reflect.Parameter;

/**
 * This class is used to represent a special node which isn't a command node.
 * It's the root of the node tree which cannot be executed. A top level command should be
 * added as child of this one.
 */
public class RootNode extends Node {
    private static final ParameterChain NULL_PARAMETER_CHAIN = new NullParameterChain();

    @Override
    public void execute(Object[] args) {
        throw new UnsupportedOperationException("Root node cannot be executed.");
    }

    @Override
    public String getName() {
        throw new UnsupportedOperationException("Root node has no name.");
    }

    @Override
    public ParameterChain getParameterChain() {
        return NULL_PARAMETER_CHAIN;
    }

    private static class NullParameterChain extends ParameterChain {

        NullParameterChain() {
            super(new Parameter[0], null);
        }

    }
}
