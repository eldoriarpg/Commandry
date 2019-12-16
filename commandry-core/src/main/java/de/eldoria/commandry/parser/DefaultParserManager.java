package de.eldoria.commandry.parser;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages multiple parser instances.
 */
public class DefaultParserManager implements ParserManager {
    private final Map<Class<?>, Parser<?>> parsers = new HashMap<>();

    @Override
    public <T> void registerParser(Parser<T> parser, Class<T> clazz) {
        parsers.put(clazz, parser);
    }

    @Override
    public boolean hasParserFor(Class<?> clazz) {
        return parsers.containsKey(clazz);
    }

    @Override
    public <T> T parse(String input, Class<T> target) {
        Parser<?> parser = parsers.get(target);
        if (parser == null) {
            // TODO
            return null;
        }
        return (T) parser.parse(input);
    }
}
