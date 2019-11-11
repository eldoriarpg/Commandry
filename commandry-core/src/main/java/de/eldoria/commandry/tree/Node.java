package de.eldoria.commandry.tree;

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
    private final Map<String, Node> children = new HashMap<>();

    /**
     * Returns an Optional of the child node identified by the given name.
     * As all names are lowercase internally, the given String will be lower-cased
     * too.
     *
     * @param name the name identifying the child to find.
     * @return an Optional with either the child node or {@code null}.
     */
    public Optional<Node> find(String name) {
        return Optional.ofNullable(children.get(name.toLowerCase()));
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
     */
    public void addChild(String name, Node node) {
        children.put(name.toLowerCase(), node);
    }
}
