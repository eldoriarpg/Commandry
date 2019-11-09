package de.eldoria.commandry.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

public final class CollectionUtils {

    private CollectionUtils() {

    }

    public static <S, C extends Collection<S>, T, D extends Collection<T>> D convert(C input,
                                                                                     Function<S, T> convertFunction,
                                                                                     Supplier<D> collectionSupplier) {
        D target = collectionSupplier.get();
        input.forEach(s -> target.add(convertFunction.apply(s)));
        return target;
    }

    public static <S, T, D extends Collection<T>> D convert(
            S[] input, Function<S, T> convertFunction, Supplier<D> collectionSupplier) {
        D target = collectionSupplier.get();
        for (S s : input) {
            target.add(convertFunction.apply(s));
        }
        return target;
    }
}
