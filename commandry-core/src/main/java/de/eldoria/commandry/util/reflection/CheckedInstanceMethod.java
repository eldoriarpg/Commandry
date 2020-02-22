package de.eldoria.commandry.util.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is used as wrapper to adapt required functionality of {@link Method}.
 * It's limited to instance methods and does not throw checked exceptions. The instance
 * to call the method with is directly bound to this object.
 * Also, parameters of the method are provided as chain, so they only can accessed one after another
 * in the order they're declared.
 */
public final class CheckedInstanceMethod {
    private final Method method;
    private final Object instance;
    private final Map<String, Object> parsedDefaults;

    private CheckedInstanceMethod(Method method, Object instance, Map<String, Object> parsedDefaults) {
        this.method = method;
        this.instance = instance;
        this.parsedDefaults = parsedDefaults;
    }

    /**
     * Creates the wrapped method object for the given method and an associated object.
     * The declaring class of the method must be assignable from the object class.
     *
     * @param method          the method to wrap.
     * @param instance        the object instance to call the method with.
     * @param parsedOptionals a map of already parsed optional parameters
     * @return the wrapped method.
     */
    public static CheckedInstanceMethod of(Method method, Object instance, Map<String, Object> parsedOptionals) {
        if (!method.getDeclaringClass().isAssignableFrom(instance.getClass())) {
            throw new IllegalArgumentException("instance isn't of the type " + method.getDeclaringClass());
        }
        if (ReflectionUtils.isStatic(method)) {
            throw new IllegalArgumentException(
                    String.format("method is static but should be an instance method: %s", method.getName()));
        }
        if (!method.canAccess(instance)) {
            throw new IllegalArgumentException("method cannot be accessed. Is it public? " + method.getName());
        }

        return new CheckedInstanceMethod(method, instance, new HashMap<>(parsedOptionals));
    }

    /**
     * Invokes the method with the given arguments. If the method cannot be invoked
     * with the given arguments, a {@link RuntimeException} will be thrown. To check if the arguments
     * are valid, {@link #canInvoke(Object...)} can be used.
     *
     * @param args the arguments to invoke the method with.
     * @see #canInvoke(Object...)
     */
    public void invoke(Object... args) {
        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns whether the given arguments can be used to invoke the method successfully.
     * This means, that each argument is compared to the type the method requires at the parameter
     * with the same index. If the length of the arguments is not equal to the length of the parameters,
     * {@code false} will be returned.
     *
     * @param args the arguments to check.
     * @return {@code true} if the method can be invoked with the given arguments, {@code false} otherwise.
     */
    public boolean canInvoke(Object... args) {
        Parameter[] params = method.getParameters();
        if (params.length != args.length) return false;
        for (int i = 0; i < params.length; i++) {
            if (!params[i].getType().isAssignableFrom(args[i].getClass())) {
                return false;
            }
        }
        return true;
    }

    /**
     * Returns a new {@link ParameterChain} object for this method.
     * Multiple calls will result in different objects, since ParameterChain
     * is mutable.
     *
     * @return a new ParameterChain for this method.
     */
    public ParameterChain getParameterChain() {
        return new ParameterChain(method.getParameters(), this);
    }

    /**
     * Returns a map of all arguments parsed by their optional parameters. The returned map
     * may be unmodifiable.
     *
     * @return a map of optional arguments.
     */
    public Map<String, Object> getParsedDefaults() {
        return parsedDefaults;
    }
}
