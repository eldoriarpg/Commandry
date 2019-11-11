package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.annotation.Optional;
import de.eldoria.commandry.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandryTest {
    private Commandry<SimpleCommandContext> commandry;

    @BeforeEach
    void setUp() {
        commandry = new Commandry<>();
        commandry.registerCommands(TestCommandClass.class);
    }

    @Test
    void testSingleCommandWithoutParameters() {
        commandry.runCommand(context("cmd1"), "cmd1");
    }

    @Test
    void testSingleCommandWithParameters() {
        commandry.runCommand(context("cmd2"), "cmd2 required otherRequired");
    }

    @Test
    void testSingleCommandWithoutUsingOptionalParameter() {
        commandry.runCommand(context("cmd3"), "cmd3");
    }

    @Test
    void testSingleCommandWithUsingOptionalParameter() {
        commandry.runCommand(context("cmd4"), "cmd4 notOpt");
    }

    @Test
    void testCommandWithContextParameterOnly() {
        commandry.runCommand(context("cmd5"), "cmd5");
    }

    @Test
    void testCommandWithContextAndRequiredParameter() {
        commandry.runCommand(context("cmd6"), "cmd6 required");
    }

    @Test
    void testCommandWithContextAndUsingOptionalParameter() {
        commandry.runCommand(context("cmd7"), "cmd7 required");
    }

    @Test
    void testCommandWithContextAndNotUsingOptionalParameter() {
        commandry.runCommand(context("cmd8"), "cmd8 required notOpt");
    }

    private SimpleCommandContext context(String command) {
        return new SimpleCommandContext(command);
    }

    public static class TestCommandClass {

        public TestCommandClass() {

        }

        @Command("cmd1")
        public void one() {
            assertTrue(true);
        }

        @Command("cmd2")
        public void two(String required, String otherRequired) {
            assertEquals("required", required);
            assertEquals("otherRequired", otherRequired);
        }

        // run without using the parameter
        @Command("cmd3")
        public void three(@Optional("opt") String optString) {
            assertEquals("opt", optString);
        }

        // run with using the parameter
        @Command("cmd4")
        public void four(@Optional("opt") String optString) {
            assertEquals("notOpt", optString);
        }

        @Command("cmd5")
        public void five(SimpleCommandContext context) {
            assertEquals("cmd5", context.getCommand());
        }

        @Command("cmd6")
        public void six(SimpleCommandContext context, String required) {
            assertEquals("cmd6", context.getCommand());
            assertEquals("required", required);
        }

        @Command("cmd7")
        public void seven(SimpleCommandContext context, String required, @Optional("opt") String optString) {
            assertEquals("cmd7", context.getCommand());
            assertEquals("required", required);
            assertEquals("opt", optString);
        }

        @Command("cmd8")
        public void eight(SimpleCommandContext context, String required, @Optional("opt") String optString) {
            assertEquals("cmd8", context.getCommand());
            assertEquals("required", required);
            assertEquals("notOpt", optString);
        }
    }

    public static class SimpleCommandContext extends CommandContext<SimpleCommandContext> {

        public SimpleCommandContext(String command) {
            super(command);
        }
    }
}
