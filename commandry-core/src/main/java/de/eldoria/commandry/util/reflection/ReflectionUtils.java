package de.eldoria.commandry.util.reflection;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Member;
import java.lang.reflect.Modifier;
import java.util.Optional;

/**
 * This class provides functionality to simplify usage reflections.
 */
public final class ReflectionUtils {

    private ReflectionUtils() {

    }

    /**
     * Returns an optional annotation instance of the given type.
     *
     * @param annotationClass  the class of the annotation type to get.
     * @param annotatedElement the element which the annotation should get from.
     * @param <A>              the type of the annotation.
     * @return an Optional containing the annotation if it exists, {@link Optional#empty()} otherwise.
     */
    public static <A extends Annotation> Optional<A> getAnnotation(Class<A> annotationClass,
                                                                   AnnotatedElement annotatedElement) {
        return Optional.ofNullable(annotatedElement.getAnnotation(annotationClass));
    }

    /**
     * Returns whether the given member is static or not.
     *
     * @param member the member to check if it's static.
     * @return {@code true} if the member is static, {@code false} otherwise.
     */
    public static boolean isStatic(Member member) {
        return hasModifiers(member, Modifier.STATIC);
    }

    /**
     * Returns an optional of the type to create an instance for. Exceptions are caught,
     * so even giving a null class will return {@link Optional#empty()}. Also, if there is no default
     * constructor or it's not visible to this class, an empty Optional will be returned.
     *
     * @param clazz the class type to create a new instance for.
     * @param <O>   the type of the instance to create.
     * @return an optional containing the created instance or {@link Optional#empty()}.
     */
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
