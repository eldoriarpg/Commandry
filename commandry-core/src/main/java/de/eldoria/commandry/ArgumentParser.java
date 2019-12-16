package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Optional;
import de.eldoria.commandry.parser.DefaultParserManager;
import de.eldoria.commandry.parser.Parser;
import de.eldoria.commandry.parser.ParserManager;
import de.eldoria.commandry.parser.SharedParsers;
import de.eldoria.commandry.util.reflection.ReflectionUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * An ArgumentParser provides parsing of arguments with both shared parsers and
 * custom parsers.
 *
 * @see SharedParsers
 * @see DefaultParserManager
 */
public class ArgumentParser implements ParserManager {
    private final ParserManager sharedParsers = SharedParsers.getManager();
    private final ParserManager customParserManager = new DefaultParserManager();

    /**
     * Parses all optional parameter values from a method and returns them in a map. That way,
     * they don't need to be parsed multiple times. If an argument cannot be parsed, an error may
     * be thrown. The returned map is immutable and therefore cannot be modified.
     *
     * @param method the method to parse the optional parameters for.
     * @return a map view of the parsed optional arguments.
     */
    public Map<String, Object> parseOptionals(Method method) {
        var map = new HashMap<String, Object>();
        for (Parameter parameter : method.getParameters()) {
            var annotation = ReflectionUtils.getAnnotation(Optional.class, parameter);
            var name = parameter.getName();
            var type = parameter.getType();
            annotation.ifPresent(optional -> map.put(name, parse(optional.value(), type)));
        }
        return Collections.unmodifiableMap(map);
    }

    /**
     * Parses an input string to an object of the given type.
     *
     * @param input the input string to parse.
     * @param type  the type class the string should be parsed to.
     * @param <T>   the type.
     * @return an object of the requested type.
     */
    @Override
    public <T> T parse(String input, Class<T> type) {
        if (sharedParsers.hasParserFor(type)) {
            return sharedParsers.parse(input, type);
        } else if (customParserManager.hasParserFor(type)) {
            return customParserManager.parse(input, type);
        } else {
            throw new IllegalStateException("No parser for this type parameter found."); // TODO
        }
    }

    /**
     * Registers a parser for a specific class.
     *
     * @param parser the parser instance to register.
     * @param clazz  the class the parser should parse.
     * @param <T>    the type the parser should parse.
     */
    @Override
    public <T> void registerParser(Parser<T> parser, Class<T> clazz) {
        customParserManager.registerParser(parser, clazz);
    }

    @Override
    public boolean hasParserFor(Class<?> clazz) {
        return sharedParsers.hasParserFor(clazz) || customParserManager.hasParserFor(clazz);
    }
}
