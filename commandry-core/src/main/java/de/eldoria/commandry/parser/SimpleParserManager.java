package de.eldoria.commandry.parser;

import de.eldoria.commandry.exception.ArgumentParseException;

import java.util.HashMap;
import java.util.Map;

/**
 * This class manages multiple parser instances.
 */
public class SimpleParserManager implements ParserManager {
    private final Map<Class<?>, Parser<?>> parsers = new HashMap<>();

    @Override
    public <T> void registerParser(Parser<T> parser, Class<T> clazz) {
        parsers.put(clazz, parser);
    }

    @Override
    public boolean hasParserFor(Class<?> clazz) {
        return parsers.containsKey(clazz);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T parse(String input, Class<T> target) throws ArgumentParseException {
        Parser<?> parser = parsers.get(target);
        if (parser == null) {
            // TODO check all parameters on registration
            throw new ArgumentParseException("No parser for the argument type found.", input);
        }
        return (T) parser.parse(input);
    }
}
