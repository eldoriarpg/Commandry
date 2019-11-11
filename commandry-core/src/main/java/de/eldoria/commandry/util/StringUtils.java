package de.eldoria.commandry.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class contains useful methods which are either called from different places
 * or are context-independent and decrease verbosity.
 * All methods are in some kind related to Strings or CharSequences and chars.
 */
public final class StringUtils {

    private StringUtils() {

    }

    /**
     * Splits an input string with the given regex and returns a collections containing the split elements.
     * In functionality, this method works like {@link String#split(String)} but returns the elements
     * in a collection instead of an array.
     * As a supplier is used to pass a collection instance, there's a great flexibility in functionality.
     * Nevertheless, as not all collections have the same behaviour, the specific implementation should be
     * chosen wisely.
     *
     * @param input              the input to split.
     * @param regex              the regex to use to split.
     * @param collectionSupplier a supplier to create the collection instance in which the elements
     *                           should be added.
     * @param <C>                the type of the returned collection.
     * @return the collection containing the split elements of the input strings.
     * @see String#split(String)
     */
    public static <C extends Collection<String>> C splitString(String input,
                                                               String regex,
                                                               Supplier<C> collectionSupplier) {
        return CollectionUtils.convert(input.split(regex), Function.identity(), collectionSupplier);
    }
}
