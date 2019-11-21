package de.eldoria.commandry.logging;

public interface Logger {

    void log(LogLevel logLevel, String message);

    default void trace(String message) {
        log(LogLevel.TRACE, message);
    }

    default void debug(String message) {
        log(LogLevel.DEBUG, message);
    }

    default void info(String message) {
        log(LogLevel.INFO, message);
    }

    default void warn(String message) {
        log(LogLevel.WARN, message);
    }

    default void error(String message) {
        log(LogLevel.ERROR, message);
    }
}
