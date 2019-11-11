package de.eldoria.commandry.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    public static <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass, Method method) {
        return Optional.ofNullable(method.getAnnotation(annotationClass));
    }

    public static <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass, Parameter parameter) {
        return Optional.ofNullable(parameter.getAnnotation(annotationClass));
    }

    public static boolean isStatic(Method method) {
        return hasModifiers(method, Modifier.STATIC);
    }

    private static boolean hasModifiers(Method method, int modifiers) {
        return (method.getModifiers() & modifiers) != 0;
    }

    public static <O> Optional<O> newInstance(Class<O> clazz) {
        try {
            return Optional.of(clazz.getConstructor().newInstance());
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }
}
