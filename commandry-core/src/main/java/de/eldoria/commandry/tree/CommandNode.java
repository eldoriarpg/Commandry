package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.CheckedInstanceMethod;
import de.eldoria.commandry.util.reflection.ParameterChain;

import java.lang.reflect.Method;

public class CommandNode extends Node {
    private final String name;
    private final CheckedInstanceMethod method;

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
