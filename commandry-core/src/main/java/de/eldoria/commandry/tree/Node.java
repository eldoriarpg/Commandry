package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public abstract class Node {
    protected final Map<String, Node> children = new HashMap<>();

    public Optional<Node> find(String name) {
        return Optional.ofNullable(children.get(name.toLowerCase()));
    }

    public abstract void execute(Object[] args);

    public abstract String getName();

    public abstract ParameterChain getParameterChain();

    public void addChild(String name, Node node) {
        children.put(name.toLowerCase(), node);
    }
}
