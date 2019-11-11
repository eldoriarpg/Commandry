package de.eldoria.commandry.util;

import java.util.Objects;

/**
 * This utility class is used as (sorted) 2-tuple.
 * All types are accepted by a pair as parameters.
 * {@link #equals(Object)} is implemented by simply delegating to both values.
 * That means, a Pair p = (firstP, secondP) is equal to a pair q = (firstQ, secondQ) if and
 * only if {@code firstP.equals(firstQ)} and {@code secondP.equals(secondQ)} are both true.
 * If the given object isn't a Pair, false is returned.
 *
 * @param <A> the first type.
 * @param <B> the second type.
 */
public final class Pair<A, B> {
    private final A first;
    private final B second;

    /**
     * Creates a new pair with the given values.
     * Both values must not be null.
     *
     * @param first  the first element of the pair.
     * @param second the second element of the pair.
     */
    public Pair(A first, B second) {
        Objects.requireNonNull(first);
        Objects.requireNonNull(second);
        this.first = first;
        this.second = second;
    }

    /**
     * Returns the first element of the pair. The returned value is guaranteed to be
     * non-null as only non-null values are accepted.
     *
     * @return the first element.
     */
    public A getFirst() {
        return first;
    }

    /**
     * Returns the second element of the pair. The returned value is guaranteed to be
     * non-null as only non-null values are accepted.
     *
     * @return the second element.
     */
    public B getSecond() {
        return second;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Pair)) return false;

        Pair<?, ?> pair = (Pair<?, ?>) o;

        if (!first.equals(pair.first)) return false;
        return second.equals(pair.second);
    }

    @Override
    public int hashCode() {
        int result = first.hashCode();
        result = 31 * result + second.hashCode();
        return result;
    }
}
