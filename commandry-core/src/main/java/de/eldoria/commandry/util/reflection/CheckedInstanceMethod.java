package de.eldoria.commandry.util.reflection;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;

public final class CheckedInstanceMethod {
    private final Method method;
    private final ParameterChain parameterChain;
    private final Object instance;

    private CheckedInstanceMethod(Method method, Object instance) {
        this.method = method;
        this.parameterChain = new ParameterChain(method.getParameters());
        this.instance = instance;
    }

    public static CheckedInstanceMethod of(Method method, Object instance) {
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
        return new CheckedInstanceMethod(method, instance);
    }

    public void invoke(Object... args) {
        try {
            method.invoke(instance, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }

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

    public ParameterChain getParameterChain() {
        return parameterChain;
    }
}
