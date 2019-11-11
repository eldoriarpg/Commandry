package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.lang.reflect.Parameter;

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

        public NullParameterChain() {
            super(new Parameter[0]);
        }

    }
}
