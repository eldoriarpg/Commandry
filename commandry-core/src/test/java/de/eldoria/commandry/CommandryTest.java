package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.annotation.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandryTest {
    private Commandry commandry;

    @BeforeEach
    void setUp() {
        commandry = new Commandry();
        commandry.registerCommands(TestCommandClass.class);
    }

    @Test
    void testSingleCommandWithoutParameters() {
        commandry.runCommand(null, "cmd1");
    }

    @Test
    void testSingleCommandWithParameters() {
        commandry.runCommand(null, "cmd2 required otherRequired");
    }

    @Test
    void testSingleCommandWithoutUsingOptionalParameter() {
        commandry.runCommand(null, "cmd3");
    }

    @Test
    void testSingleCommandWithUsingOptionalParameter() {
        commandry.runCommand(null, "cmd4 notOpt");
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
    }
}
