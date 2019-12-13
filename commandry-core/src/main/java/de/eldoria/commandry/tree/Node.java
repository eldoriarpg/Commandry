package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.ParameterChain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Stack;

/**
 * This class is used to build a tree of commands.
 * A node thereby contains a set of children. Those can
 * identified by their name.
 * <b>Note:</b> The names are all lower-cased, so {@code HeLlO} will
 * identify the same child as {@code hello}.
 */
public abstract class Node {
    private static final String[] EMPTY_STRING_ARRAY = new String[0];
    private final Map<String, Node> children;
    private final Map<String, String> childrenAliases;
    private final Node parent;
    private final boolean executable = true; // TODO implement

    public Node(Node parent) {
        this.children = new HashMap<>();
        this.childrenAliases = new HashMap<>();
        this.parent = parent;
    }

    /**
     * Returns an Optional of the child node identified by the given name.
     * As all names are lowercase internally, the given String will be lower-cased
     * too.
     *
     * @param name the name identifying the child to find.
     * @return an Optional with either the child node or {@code null}.
     */
    public Optional<Node> find(String name) {
        String child = childrenAliases.get(name.toLowerCase());
        return Optional.ofNullable(children.get(child));
    }

    public Node getParent() {
        return parent;
    }

    /**
     * Executes the command represented by this node with the given arguments.
     * As there may be nodes which doesn't represent a command, invoking this method
     * may result in unintended behaviour.
     *
     * @param args the arguments to execute the command with.
     */
    public abstract void execute(Object[] args);

    /**
     * Returns the name of this node. The name may not be lowercase but as given when
     * calling {@link #addChild(String, Node)}.
     *
     * @return the name of this node.
     */
    public abstract String getName();

    /**
     * Returns the parameter chain the command represented by this node requires.
     * As there may be nodes which doesn't represent a command, invoking this method
     * may result in unintended behaviour.
     *
     * @return the parameter chain of the represented command.
     */
    public abstract ParameterChain getParameterChain();

    /**
     * Adds a child node to this node. Note that the name will be lower-cased internally,
     * so calling {@code addChild("hello", nodeTwo)} after {@code addChild("HeLlO", nodeTwo)}
     * will replace the existing child. To check if a child with the given name is already added,
     * {@link #find(String)} can be used.
     *
     * @param name the name of the child.
     * @param node the child node.
     * @see #find(String)
     * @see #addChild(String, String[], Node)
     * @see #addChild(String, String, Node)
     */
    public void addChild(String name, Node node) {
        addChild(name, EMPTY_STRING_ARRAY, node);
    }

    /**
     * Adds a child node to this node. Note that the name will be lower-cased internally,
     * so calling {@code addChild("hello", nodeTwo)} after {@code addChild("HeLlO", nodeTwo)}
     * will replace the existing child. To check if a child with the given name is already added,
     * {@link #find(String)} can be used. The given aliases can be used as argument for
     * {@link #find(String)} and will all return the node.
     *
     * @param name the name of the child.
     * @param aliases the array of aliases.
     * @param node the child node.
     * @see #find(String)
     * @see #addChild(String, Node)
     * @see #addChild(String, String, Node)
     */
    public void addChild(String name, String[] aliases, Node node) {
        String lowerName = name.toLowerCase();
        children.put(lowerName, node);
        childrenAliases.put(lowerName, lowerName);
        for (String alias : aliases) {
            childrenAliases.put(alias.toLowerCase(), lowerName);
        }
    }

    /**
     * Adds a child node to this node. Note that the name will be lower-cased internally,
     * so calling {@code addChild("hello", nodeTwo)} after {@code addChild("HeLlO", nodeTwo)}
     * will replace the existing child. To check if a child with the given name is already added,
     * {@link #find(String)} can be used. The given aliases can be used as argument for
     * {@link #find(String)} and will all return the node.
     *
     * @param name the name of the child.
     * @param alias the alias for the child.
     * @param node the child node.
     * @see #find(String)
     * @see #addChild(String, Node)
     * @see #addChild(String, String[], Node)
     */
    public void addChild(String name, String alias, Node node) {
        addChild(name, new String[] {alias}, node);
    }

    public List<String> getAvailableCommands() {
        var list = new ArrayList<String>();
        var before = buildUntilNode(getRoot());
        var stack = new Stack<Node>();
        stack.push(this);
        while (!stack.empty()) {
            var node = stack.pop();
            list.add(before + " " + node.buildUntilNode(this));
            node.children.forEach((k, v) -> stack.push(v));
        }
        return list;
    }

    private Node getRoot() {
        if (parent == null) return this;
        return parent.getRoot();
    }

    private String buildUntilNode(Node tempRoot) {
        var before = new StringBuilder();
        var p = this;
        while (p.parent != tempRoot && p.parent != null) {
            before.insert(0, ' ').insert(0, p.getName());
            p = p.parent;
        }
        before.insert(0, ' ').insert(0, p.getName());
        return before.toString();
    }

    @Override
    public String toString() {
        return getName();
    }
}
