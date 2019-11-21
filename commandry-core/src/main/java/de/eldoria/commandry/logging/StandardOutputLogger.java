package de.eldoria.commandry.logging;

import java.time.LocalTime;
import java.time.temporal.ChronoUnit;

public class StandardOutputLogger implements Logger {

    @Override
    public void log(LogLevel logLevel, String message) {
        System.out.printf("[%s][%s] %s %s",
                LocalTime.now().truncatedTo(ChronoUnit.SECONDS).toString(),
                logLevel.name(),
                message,
                System.lineSeparator());
    }
}
