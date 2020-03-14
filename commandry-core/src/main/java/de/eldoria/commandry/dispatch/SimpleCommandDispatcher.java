package de.eldoria.commandry.dispatch;

import de.eldoria.commandry.exception.CommandExecutionException;
import de.eldoria.commandry.parser.ParserManager;
import de.eldoria.commandry.tree.Node;
import de.eldoria.commandry.util.StringReader;
import de.eldoria.commandry.util.reflection.ParameterChain;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

/**
 * This default implementation of the command dispatcher is used by default.
 *
 * @param <C> the context type.
 */
public class SimpleCommandDispatcher<C> implements CommandDispatcher<C> {

    private final ParserManager parserManager;
    private final Node root;

    /**
     * Creates a new instance of the simple command dispatcher.
     *
     * @param parserManager the parser manager to handle parsing.
     * @param root          the root node for exploration to find the command to execute.
     */
    public SimpleCommandDispatcher(ParserManager parserManager, Node root) {
        this.parserManager = parserManager;
        this.root = root;
    }

    @Override
    public void dispatch(StringReader reader, C context) throws CommandExecutionException {
        checkEmptyInput(reader);
        Node currentCommand = null;
        ParameterChain parameterChain = null;
        while (reader.canRead()) {
            var word = reader.readWord();
            if (currentCommand == null) {
                // only executed at the beginning, when looking for the top level command
                currentCommand = root.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, List.of(), context);
            } else if (parameterChain.acceptsFurtherArgument()) {
                // Defaults are prioritized before subcommands
                parameterChain.offerArgument(parserManager.parse(word, parameterChain.getNextType()));
            } else {
                // No default argument required anymore, try to find a subcommand
                currentCommand = currentCommand.find(word)
                        .orElseThrow(noMatchingCommandFound(word));
                var argumentList = parameterChain.getArgumentList();
                parameterChain = currentCommand.getParameterChain();
                offerAll(parameterChain, argumentList, context);
            }
        }
        Objects.requireNonNull(parameterChain, "parameterChain must not be null"); // shouldn't happen
        if (parameterChain.requiresFurtherArgument()) {
            // Can't read anything else from the reader, but more arguments are required.
            reader.reset();
            throw new CommandExecutionException("Too few arguments.", reader.readRemaining());
        }
        parameterChain.completeArguments();
        currentCommand.execute(parameterChain.getArgumentArray());
    }

    /**
     * Offers a list of arguments to a parameter chain. This can be used if a new chain has to be
     * satisfied, but arguments were already parsed. This method automatically detects if
     * the context is required or not.
     *
     * @param targetChain   the parameter chain to fill with parsed arguments.
     * @param currentOffers the available parsed arguments to offer.
     * @param context       the context to offer, if required.
     */
    private void offerAll(ParameterChain targetChain, List<Object> currentOffers, C context) {
        if (!targetChain.acceptsFurtherArgument() && currentOffers.isEmpty()) {
            return;
        }
        if (targetChain.getNextType().isInstance(context)) { // chain requires context
            if (currentOffers.isEmpty() || !currentOffers.get(0).equals(context)) {
                targetChain.offerArgument(context);
            }
            targetChain.offerAll(currentOffers);
        } else {
            List<Object> offers;
            if (!currentOffers.isEmpty() && currentOffers.get(0).equals(context)) {
                // ignore context
                offers = currentOffers.subList(1, currentOffers.size());
            } else {
                offers = currentOffers;
            }
            targetChain.offerAll(offers);
        }
    }


    private void checkEmptyInput(StringReader reader) throws CommandExecutionException {
        if (!reader.canRead()) {
            throw new CommandExecutionException("No empty input allowed.", "");
        }
    }

    private Supplier<CommandExecutionException> noMatchingCommandFound(String word) {
        return () -> new CommandExecutionException("No matching command found.", word);
    }
}
