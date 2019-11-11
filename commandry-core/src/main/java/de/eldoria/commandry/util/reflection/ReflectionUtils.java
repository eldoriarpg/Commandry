package de.eldoria.commandry.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Optional;

public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    public static <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass,
                                                                   AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(annotationClass));
    }

    public static boolean isStatic(Member member) {
        return hasModifiers(member, Modifier.STATIC);
    }

    public static <O> Optional<O> newInstance(Class<O> clazz) {
        try {
            return Optional.of(clazz.getConstructor().newInstance());
        } catch (Exception ignore) {
            return Optional.empty();
        }
    }

    private static boolean hasModifiers(Member member, int modifiers) {
        return (member.getModifiers() & modifiers) != 0;
    }
}
