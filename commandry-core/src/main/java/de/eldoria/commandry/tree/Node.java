package de.eldoria.commandry.tree;

import de.eldoria.commandry.util.reflection.CheckedInstanceMethod;
import de.eldoria.commandry.util.reflection.ParameterChain;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

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

    /**
     * Creates a new node with a given parent node.
     *
     * @param parent the parent of this node.
     */
    Node(Node parent) {
        this.children = new HashMap<>();
        this.childrenAliases = new HashMap<>();
        this.parent = parent;
    }

    /**
     * Creates a node that can be used as root.
     *
     * @return a new node that can be used as root.
     */
    public static Node create() {
        return new RootNode();
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
     * calling {@link #addChild(String, CheckedInstanceMethod)}.
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
     * @param name   the name of the child.
     * @param method the underlying command method of the child.
     * @see #find(String)
     * @see #addChild(String, String[], CheckedInstanceMethod)
     * @see #addChild(String, String, CheckedInstanceMethod)
     */
    public void addChild(String name, CheckedInstanceMethod method) {
        addChild(name, EMPTY_STRING_ARRAY, method);
    }

    /**
     * Adds a child node to this node. Note that the name will be lower-cased internally,
     * so calling {@code addChild("hello", nodeTwo)} after {@code addChild("HeLlO", nodeTwo)}
     * will replace the existing child. To check if a child with the given name is already added,
     * {@link #find(String)} can be used. The given aliases can be used as argument for
     * {@link #find(String)} and will all return the node.
     *
     * @param name    the name of the child.
     * @param aliases the array of aliases.
     * @param method  the underlying command method of the child.
     * @see #find(String)
     * @see #addChild(String, CheckedInstanceMethod)
     * @see #addChild(String, String, CheckedInstanceMethod)
     */
    public void addChild(String name, String[] aliases, CheckedInstanceMethod method) {
        String lowerName = name.toLowerCase();
        var node = new CommandDispatcherNode(this, name, method);
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
     * @param name   the name of the child.
     * @param alias  the alias for the child.
     * @param method the underlying command method of the child.
     * @see #find(String)
     * @see #addChild(String, CheckedInstanceMethod)
     * @see #addChild(String, String[], CheckedInstanceMethod)
     */
    public void addChild(String name, String alias, CheckedInstanceMethod method) {
        addChild(name, new String[] {alias}, method);
    }

    @Override
    public String toString() {
        return getName();
    }
}
