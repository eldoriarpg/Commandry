package de.eldoria.commandry;

import de.eldoria.commandry.annotation.DefaultsTo;
import de.eldoria.commandry.exception.ArgumentParseException;
import de.eldoria.commandry.parser.Parser;
import de.eldoria.commandry.parser.ParserManager;
import de.eldoria.commandry.parser.SharedParsers;
import de.eldoria.commandry.parser.SimpleParserManager;
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
 * @see SimpleParserManager
 */
public class ArgumentParser implements ParserManager {
    private final ParserManager sharedParsers = SharedParsers.getManager();
    private final ParserManager customParserManager = new SimpleParserManager();

    /**
     * Parses all parameter values annotated with {@link DefaultsTo} from a method and returns them in a map.
     * That way, they don't need to be parsed multiple times. If an argument cannot be parsed, an error may
     * be thrown. The returned map is immutable and therefore cannot be modified.
     *
     * @param method the method to parse the parameters annotated with {@link DefaultsTo} for.
     * @return a map view of the parsed optional arguments.
     * @throws ArgumentParseException if an optional value couldn't be parsed.
     */
    public Map<String, Object> parseDefaults(Method method) throws ArgumentParseException {
        var map = new HashMap<String, Object>();
        for (Parameter parameter : method.getParameters()) {
            var annotation = ReflectionUtils.getAnnotation(DefaultsTo.class, parameter);
            var name = parameter.getName();
            var type = parameter.getType();
            if (annotation.isPresent()) {
                var value = annotation.get().value();
                if (value.equals("null")) {
                    map.put(name, null);
                } else {
                    map.put(name, parse(annotation.get().value(), type));
                }
            }
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
    public <T> T parse(String input, Class<T> type) throws ArgumentParseException {
        if (sharedParsers.hasParserFor(type)) {
            return sharedParsers.parse(input, type);
        } else if (customParserManager.hasParserFor(type)) {
            return customParserManager.parse(input, type);
        } else {
            throw new ArgumentParseException("No parser for this type parameter found.", input); // TODO
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
