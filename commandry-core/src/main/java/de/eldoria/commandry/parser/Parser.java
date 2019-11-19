package de.eldoria.commandry.parser;

/**
 * This interface is used for all parser instances. It also can be used as
 * functional interface and therefor as lambda expression.
 *
 * @param <T> the type to parse input to.
 */
@FunctionalInterface
public interface Parser<T> {

    /**
     * Parses a given input to an instance of the given type.
     *
     * @param input the input to parse.
     * @return an object of the given type
     */
    T parse(String input);

}
