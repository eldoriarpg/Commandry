package de.eldoria.commandry.parser;

import de.eldoria.commandry.exception.ArgumentParseException;

import java.util.function.Function;

/**
 * This class initializes and provides parsers for simple data types like primitives
 * and Strings.
 */
public final class SharedParsers {
    private static final ParserManager SHARED_PARSERS_MANAGER = new SimpleParserManager();

    static {
        SHARED_PARSERS_MANAGER.registerParser(input -> input, String.class);

        var charParser = charParser();
        SHARED_PARSERS_MANAGER.registerParser(charParser, char.class);
        SHARED_PARSERS_MANAGER.registerParser(charParser, Character.class);

        var booleanParser = booleanParser();
        SHARED_PARSERS_MANAGER.registerParser(booleanParser, boolean.class);
        SHARED_PARSERS_MANAGER.registerParser(booleanParser, Boolean.class);

        var intParser = numericParser(Integer::parseInt);
        SHARED_PARSERS_MANAGER.registerParser(intParser, int.class);
        SHARED_PARSERS_MANAGER.registerParser(intParser, Integer.class);

        var longParser = numericParser(Long::parseLong);
        SHARED_PARSERS_MANAGER.registerParser(longParser, long.class);
        SHARED_PARSERS_MANAGER.registerParser(longParser, Long.class);

        var byteParser = numericParser(Byte::parseByte);
        SHARED_PARSERS_MANAGER.registerParser(byteParser, byte.class);
        SHARED_PARSERS_MANAGER.registerParser(byteParser, Byte.class);

        var floatParser = numericParser(Float::parseFloat);
        SHARED_PARSERS_MANAGER.registerParser(floatParser, float.class);
        SHARED_PARSERS_MANAGER.registerParser(floatParser, Float.class);

        var doubleParser = numericParser(Double::parseDouble);
        SHARED_PARSERS_MANAGER.registerParser(doubleParser, double.class);
        SHARED_PARSERS_MANAGER.registerParser(doubleParser, Double.class);
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

    private static <T extends Number> Parser<T> numericParser(Function<String, T> parser) {
        return s -> {
            try {
                return parser.apply(s);
            } catch (NumberFormatException e) {
                throw new ArgumentParseException("Not a number", s);
            }
        };
    }

    private static Parser<Boolean> booleanParser() {
        return s -> {
            switch (s.toLowerCase()) {
                case "true":
                    return true;
                case "false":
                    return false;
                default:
                    throw new ArgumentParseException("Not a boolean", s);
            }
        };
    }

    private static Parser<Character> charParser() {
        return s -> {
            if (s.length() != 1) {
                throw new ArgumentParseException("Not a char", s);
            }
            return s.charAt(0);
        };
    }
}
