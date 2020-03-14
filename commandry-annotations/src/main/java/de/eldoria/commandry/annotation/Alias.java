package de.eldoria.commandry.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to add aliases to commands.
 * The given value is either a single word or multiple words, comma-separated.
 *
 * @see Command
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Alias {

    String value();
}
