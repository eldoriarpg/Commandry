package de.eldoria.commandry.parser;

public interface ParserManager {

    /**
     * Registers a parser for the given type. Calling this method may replace
     * other parsers for the same type.
     *
     * @param parser the parser to register.
     * @param clazz  the type class to register the parser for.
     * @param <T>    the type which can be parsed by the parser.
     */
    <T> void registerParser(Parser<T> parser, Class<T> clazz);

    /**
     * Returns whether there is an existing parser for the given type.
     *
     * @param clazz the type class to look up.
     * @return {@code true} if a parser for the given type exists, {@code false} otherwise.
     */
    boolean hasParserFor(Class<?> clazz);

    /**
     * Parses an input string to the requested type. If no parser is found,
     * null is returned. The parser may fail to parse and throw an exception.
     *
     * @param input  the input to parse.
     * @param target the type class to get the parser for.
     * @param <T>    the type to return.
     * @return the parsed object of the requested type.
     */
    <T> T parse(String input, Class<T> target);
}
