package de.eldoria.commandry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The command annotation is used to mark methods which should be registered as
 * commands. The required {@code value} is the name of the command. The optional {@code parents}
 * defines all parent commands, comma-separated.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Command {

    String value();

    String parents() default "";
}
