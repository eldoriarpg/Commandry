package de.eldoria.commandry.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public final class StringUtils {

    private StringUtils() {

    }

    public static <C extends Collection<String>> C splitString(String input,
                                                               String regex,
                                                               Supplier<C> collectionSupplier) {
        return CollectionUtils.convert(input.split(regex), Function.identity(), collectionSupplier);
    }
}
