package de.eldoria.commandry.parser;

/**
 * This class initializes and provides parsers for simple data types like primitives
 * and Strings.
 */
public final class SharedParsers {
    private static final ParserManager SHARED_PARSERS_MANAGER = new DefaultParserManager();

    static {
        SHARED_PARSERS_MANAGER.registerParser(input -> input, String.class);

        SHARED_PARSERS_MANAGER.registerParser(Boolean::parseBoolean, boolean.class);
        SHARED_PARSERS_MANAGER.registerParser(Boolean::parseBoolean, Boolean.class);

        SHARED_PARSERS_MANAGER.registerParser(Integer::parseInt, int.class);
        SHARED_PARSERS_MANAGER.registerParser(Integer::parseInt, Integer.class);

        SHARED_PARSERS_MANAGER.registerParser(Long::parseLong, long.class);
        SHARED_PARSERS_MANAGER.registerParser(Long::parseLong, Long.class);

        SHARED_PARSERS_MANAGER.registerParser(Byte::parseByte, byte.class);
        SHARED_PARSERS_MANAGER.registerParser(Byte::parseByte, Byte.class);

        SHARED_PARSERS_MANAGER.registerParser(Float::parseFloat, float.class);
        SHARED_PARSERS_MANAGER.registerParser(Float::parseFloat, Float.class);

        SHARED_PARSERS_MANAGER.registerParser(Double::parseDouble, double.class);
        SHARED_PARSERS_MANAGER.registerParser(Double::parseDouble, Double.class);
    }

    /**
     * Returns a parser manager which provides default and often used parsers.
     *
     * @return a parser manager.
     */
    public static ParserManager getManager() {
        // TODO return protected object?
        return SHARED_PARSERS_MANAGER;
    }
}
