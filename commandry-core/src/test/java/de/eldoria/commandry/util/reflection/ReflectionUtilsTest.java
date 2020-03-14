package de.eldoria.commandry.util.reflection;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ReflectionUtilsTest {

    @Test
    void testGetClassAnnotation() {
        Optional<Deprecated> annotation = ReflectionUtils.getAnnotation(Deprecated.class, AnnotatedClass.class);
        assertTrue(annotation.isPresent());
        assertEquals("id1", annotation.get().since());
    }

    @Test
    void testGetClassAnnotationEmpty() {
        Optional<Deprecated> annotation = ReflectionUtils.getAnnotation(Deprecated.class, NotAnnotatedClass.class);
        assertTrue(annotation.isEmpty());
    }

    @Test
    void testGetMethodAnnotation() throws NoSuchMethodException {
        var method = AnnotatedClass.class.getMethod("annotatedMethod");
        Optional<Deprecated> annotation = ReflectionUtils.getAnnotation(Deprecated.class, method);
        assertTrue(annotation.isPresent());
        assertEquals("id1.1", annotation.get().since());
    }

    @Test
    void testGetMethodAnnotationEmpty() throws NoSuchMethodException {
        var method = AnnotatedClass.class.getMethod("notAnnotatedMethod");
        Optional<Deprecated> annotation = ReflectionUtils.getAnnotation(Deprecated.class, method);
        assertTrue(annotation.isEmpty());
    }

    @Test
    void testIsStaticMethodTrue() throws NoSuchMethodException {
        var method = NotAnnotatedClass.class.getMethod("staticMethod");
        assertTrue(ReflectionUtils.isStatic(method));
    }

    @Test
    void testIsStaticFieldTrue() throws NoSuchFieldException {
        var field = NotAnnotatedClass.class.getField("staticField");
        assertTrue(ReflectionUtils.isStatic(field));
    }

    @Test
    void testIsStaticMethodFalse() throws NoSuchMethodException {
        var method = NotAnnotatedClass.class.getMethod("nonStaticMethod");
        assertFalse(ReflectionUtils.isStatic(method));
    }

    @Test
    void testIsStaticFieldFalse() throws NoSuchFieldException {
        var field = NotAnnotatedClass.class.getField("nonStaticField");
        assertFalse(ReflectionUtils.isStatic(field));
    }

    @Test
    void testNewInstanceSucceed() {
        var optInstance = ReflectionUtils.newInstance(AnnotatedClass.class);
        assertTrue(optInstance.isPresent());
        assertEquals(optInstance.get().getClass(), AnnotatedClass.class);
    }

    @Test
    void testNewInstanceFail() {
        var optInstance = ReflectionUtils.newInstance(NotAnnotatedClass.class);
        assertTrue(optInstance.isEmpty());
    }

    @Deprecated(since = "id1")
    static class AnnotatedClass {

        public AnnotatedClass() {

        }

        @Deprecated(since = "id1.1")
        public void annotatedMethod() {

        }

        public void notAnnotatedMethod() {

        }
    }

    static class NotAnnotatedClass {

        public static final int staticField = 0;
        public final int nonStaticField = 1;
        public NotAnnotatedClass(int u) {

        }

        public static void staticMethod() {

        }

        public void nonStaticMethod() {

        }
    }
}
