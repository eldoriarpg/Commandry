package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public abstract class Node {
    protected final Map<String, Node> children = new HashMap<>();

    public void accept(Consumer<Node> visitor) {
        visitor.accept(this);
        children.forEach((s, c) -> c.accept(visitor));
    }

    public Node find(String name) {
        return children.get(name);
    }

    public abstract void execute(Object[] args);

    public abstract String getName();

    public abstract ParameterChain getParameterChain();

    public void addChild(String name, Node node) {
        children.put(name, node);
    }
}
