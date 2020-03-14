package de.eldoria.commandry.util;

import java.util.Collection;
import java.util.function.Function;
import java.util.function.Supplier;

/**
 * This class contains useful methods which are either called from different places
 * or are context-independent and decrease verbosity.
 * All methods are related to collections, either unspecific or a specified subclass.
 */
public final class CollectionUtils {

    private CollectionUtils() {

    }

    /**
     * Converts a collections from one type to another by a given convert function.
     * This is mainly thought to convert between different collection types rather
     * then different implementations of specific collection types.
     * If the function maps values to null and the target collection doesn't support null
     * elements, it may have unintended behaviour.
     * All parameters require non-null arguments, otherwise a {@link NullPointerException} will
     * be thrown.
     *
     * @param input              the input collection.
     * @param convertFunction    the convert function to convert single entries.
     * @param collectionSupplier the supplier for the target collection.
     * @param <S>                the source element type.
     * @param <C>                the source collection type.
     * @param <T>                the target element type.
     * @param <D>                thr target collection type.
     * @return the collection provided by the supplier containing the converted elements.
     */
    public static <S, C extends Collection<S>, T, D extends Collection<T>> D convert(C input,
                                                                                     Function<S, T> convertFunction,
                                                                                     Supplier<D> collectionSupplier) {
        D target = collectionSupplier.get();
        input.forEach(s -> target.add(convertFunction.apply(s)));
        return target;
    }

    /**
     * Converts an array of a type to a collection of another type using the given function.
     * If the function maps values to null and the target collection doesn't support null
     * elements, it may have unintended behaviour.
     * All parameters require non-null arguments, otherwise a {@link NullPointerException} will
     * be thrown.
     *
     * @param input              the input array.
     * @param convertFunction    the convert function.
     * @param collectionSupplier the supplier for the target collection.
     * @param <S>                the source element type.
     * @param <T>                the target element type.
     * @param <D>                the target collection type.
     * @return the supplied collection containing the converted elements.
     */
    public static <S, T, D extends Collection<T>> D convert(
            S[] input, Function<S, T> convertFunction, Supplier<D> collectionSupplier) {
        D target = collectionSupplier.get();
        for (S s : input) {
            target.add(convertFunction.apply(s));
        }
        return target;
    }
}
