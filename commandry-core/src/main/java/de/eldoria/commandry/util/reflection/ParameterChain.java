package de.eldoria.commandry.util.reflection;

import de.eldoria.commandry.annotation.Optional;

import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * The {@code ParameterChain} is used to fill up all required (and also optional)
 * parameters a command requires. It also validates types and can set missing optional
 * arguments.
 *
 * @see Parameter
 */
public class ParameterChain {
    private final Parameter[] parameters;
    private final Object[] arguments;
    private final int optionalIndex;
    private final CheckedInstanceMethod method;
    private int position;

    /**
     * Creates a new {@code ParameterChain} for the given parameters.
     *
     * @param parameters the parameters which should be represented by this parameter chain.
     * @param method     the method the parameters belong to.
     */
    public ParameterChain(Parameter[] parameters, CheckedInstanceMethod method) {
        this.method = method;
        int paramsLength = parameters.length;
        int optIndex = paramsLength;
        for (int i = optIndex - 1; i >= 0; i--) {
            if (ReflectionUtils.getAnnotation(Optional.class, parameters[i]).isPresent()) {
                optIndex = i;
            } else {
                break;
            }
        }

        this.parameters = parameters;
        this.arguments = new Object[paramsLength];
        this.optionalIndex = optIndex;
        this.position = 0;
    }

    /**
     * Gets whether more arguments are required to satisfy all required parameters.
     * This method doesn't have any significance regarding optional arguments.
     *
     * @return {@code true} if further argument(s) are required to satisfy all required parameters,
     * {@code false} otherwise.
     * @see #offerArgument(Object)
     */
    public boolean requiresFurtherArgument() {
        return position < optionalIndex;
    }

    /**
     * Gets whether more arguments can be offered. If {@link #requiresFurtherArgument()}
     * returns true, this method will return true too. If all required parameters are
     * satisfied, this method will still return true as long as there are unsatisfied optional
     * parameters.
     *
     * @return {@code true} if further argument(s) are available to satisfy, {@code false} otherwise.
     * @see #offerArgument(Object)
     */
    public boolean acceptsFurtherArgument() {
        return position < parameters.length;
    }

    /**
     * Gets the type of the next parameter. This is especially the type the next offered argument must match.
     *
     * @return the type of the next parameter.
     */
    public Class<?> getNextType() {
        checkAcceptsFurtherArgument();
        return parameters[position].getType();
    }

    /**
     * Accepts an object if and only if {@link #acceptsFurtherArgument()} returns true, and the type
     * of the object can be assigned to the type returned by {@link #getNextType()}.
     *
     * @param offer the object to offer to the chain.
     */
    public void offerArgument(Object offer) {
        checkAcceptsFurtherArgument();
        checkType(offer);
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts a boolean value if {@link #acceptsFurtherArgument()} returns true and
     * {@link #getNextType()} equals {@code boolean.class}. Is is used as boxing does
     * not work when calling {@link #offerArgument(Object)}.
     *
     * @param offer the boolean to offer to the chain.
     */
    public void offerArgument(boolean offer) {
        checkAcceptsFurtherArgument();
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts an int value if {@link #acceptsFurtherArgument()} returns true and
     * {@link #getNextType()} equals {@code int.class}. Is is used as boxing does
     * not work when calling {@link #offerArgument(Object)}.
     *
     * @param offer the int to offer to the chain.
     */
    public void offerArgument(int offer) {
        checkAcceptsFurtherArgument();
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts a long value if {@link #acceptsFurtherArgument()} returns true and
     * {@link #getNextType()} equals {@code long.class}. Is is used as boxing does
     * not work when calling {@link #offerArgument(Object)}.
     *
     * @param offer the long to offer to the chain.
     */
    public void offerArgument(long offer) {
        checkAcceptsFurtherArgument();
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts a float value if {@link #acceptsFurtherArgument()} returns true and
     * {@link #getNextType()} equals {@code float.class}. Is is used as boxing does
     * not work when calling {@link #offerArgument(Object)}.
     *
     * @param offer the float to offer to the chain.
     */
    public void offerArgument(float offer) {
        checkAcceptsFurtherArgument();
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts a double value if {@link #acceptsFurtherArgument()} returns true and
     * {@link #getNextType()} equals {@code double.class}. Is is used as boxing does
     * not work when calling {@link #offerArgument(Object)}.
     *
     * @param offer the double to offer to the chain.
     */
    public void offerArgument(double offer) {
        checkAcceptsFurtherArgument();
        arguments[position] = offer;
        position++;
    }

    /**
     * Accepts a collection of objects to offer. This method works as
     * using {@link #offerArgument(Object)} for each element of a collection.
     *
     * @param offers the objects to offer to the chain.
     */
    public void offerAll(Collection<Object> offers) {
        offers.forEach(this::offerArgument);
    }

    /**
     * Gets a copy of the current argument array.
     * As long as {@link #acceptsFurtherArgument()} returns true, the array will
     * contain null values. Changes to the returned array are not reflected to the chain.
     *
     * @return a copy of the current argument array.
     */
    public Object[] getArgumentArray() {
        return Arrays.copyOf(arguments, arguments.length);
    }

    /**
     * Gets a copy of the current argument list.
     * The returned list is unmodifiable.
     *
     * @return a copy of the current argument list.
     */
    public List<Object> getArgumentList() {
        return List.of(arguments);
    }

    /**
     * Replaces missing optional arguments with their default values.
     * If not all required parameters are already satisfied, means {@link #requiresFurtherArgument()}
     * returns true, an exception will be thrown.
     *
     * @throws IllegalStateException if {@link #requiresFurtherArgument()} returns true.
     */
    public void completeArguments() {
        if (requiresFurtherArgument()) {
            throw new IllegalStateException("Not all required parameters are satisfied.");
        }
        var parsedOptionals = method.getParsedOptionals();
        for (int i = position; i < parameters.length; i++) {
            arguments[i] = parsedOptionals.get(parameters[i].getName());
        }
    }

    private void checkType(Object o) {
        if (!parameters[position].getType().isAssignableFrom(o.getClass())) {
            throw new IllegalArgumentException(String.format("Invalid type. Was %s but expected %s",
                    o.getClass(), parameters[position].getType()));
        }
    }

    private void checkAcceptsFurtherArgument() {
        if (!acceptsFurtherArgument()) {
            throw new IndexOutOfBoundsException("No more parameters allowed.");
        }
    }

    private Object parseOptional(Parameter parameter) {
        var optional = ReflectionUtils.getAnnotation(Optional.class, parameter)
                .orElseThrow(() -> new IllegalStateException("Optional annotation not found."));
        return optional.value(); // TODO parse but somewhere else
    }
}
