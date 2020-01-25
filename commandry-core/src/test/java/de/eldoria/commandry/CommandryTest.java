package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.annotation.Optional;
import de.eldoria.commandry.exception.CommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandryTest {
    private Commandry<CommandContext> commandry;

    @BeforeEach
    void setUp() {
        commandry = new Commandry<>();
        commandry.registerCommands(TestCommandClass.class);
    }

    @Test
    void testEmptyInput() {
        assertThrows(CommandExecutionException.class, () -> commandry.dispatchCommand(context("anything"), ""));
    }

    @Test
    void testInvalidCommand() {
        assertThrows(CommandExecutionException.class, () -> commandry.dispatchCommand(context("anything"), "invalid"));
    }

    @Test
    void testSingleCommandWithoutParameters() {
        commandry.dispatchCommand(context("cmd1"), "cmd1");
    }

    @Test
    void testSingleCommandWithParameters() {
        commandry.dispatchCommand(context("cmd2"), "cmd2 required otherRequired");
    }

    @Test
    void testSingleCommandWithoutUsingOptionalParameter() {
        commandry.dispatchCommand(context("cmd3"), "cmd3");
    }

    @Test
    void testSingleCommandWithUsingOptionalParameter() {
        commandry.dispatchCommand(context("cmd4"), "cmd4 notOpt");
    }

    @Test
    void testCommandWithContextParameterOnly() {
        commandry.dispatchCommand(context("cmd5"), "cmd5");
    }

    @Test
    void testCommandWithContextAndRequiredParameter() {
        commandry.dispatchCommand(context("cmd6"), "cmd6 required");
    }

    @Test
    void testCommandWithContextAndUsingOptionalParameter() {
        commandry.dispatchCommand(context("cmd7"), "cmd7 required");
    }

    @Test
    void testCommandWithContextAndNotUsingOptionalParameter() {
        commandry.dispatchCommand(context("cmd8"), "cmd8 required notOpt");
    }

    @Test
    void testSingleSubCommandOfSingleCommandWithoutParameters() {
        commandry.dispatchCommand(context("..."), "cmd1 cmd9");
    }

    @Test
    void testSubCommandOfSingleCommandWithParameters() {
        commandry.dispatchCommand(context(""), "cmd1 cmd10 ccc");
    }

    @Test
    void testSubCommandWithParametersOfSingleCommandWithParameters() {
        commandry.dispatchCommand(context(""), "cmd2 required otherRequired cmd11 thirdRequired");
    }

    @Test
    void testInputParsing() {
        commandry.dispatchCommand(context(""), "cmd12 12");
    }

    @Test
    void testOptionalParsing() {
        commandry.dispatchCommand(context(""), "cmd13");
    }

    @Test
    void testPrimitiveParsing() {
        commandry.dispatchCommand(context(""), "cmd14 true 1337 -1337 13.37 -13.37");
    }

    private CommandContext context(String command) {
        return new CommandContext(command);
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
        public void five(CommandContext context) {
            assertEquals("cmd5", context.command);
        }

        @Command("cmd6")
        public void six(CommandContext context, String required) {
            assertEquals("cmd6", context.command);
            assertEquals("required", required);
        }

        @Command("cmd7")
        public void seven(CommandContext context, String required, @Optional("opt") String optString) {
            assertEquals("cmd7", context.command);
            assertEquals("required", required);
            assertEquals("opt", optString);
        }

        @Command("cmd8")
        public void eight(CommandContext context, String required, @Optional("opt") String optString) {
            assertEquals("cmd8", context.command);
            assertEquals("required", required);
            assertEquals("notOpt", optString);
        }

        @Command(value = "cmd9", ascendants = "cmd1")
        public void nine() {
            assertTrue(true);
        }

        @Command(value = "cmd10", ascendants = "cmd1")
        public void ten(String ccc, @Optional("hello world") String text) {
            assertEquals("ccc", ccc);
            assertEquals("hello world", text);
        }

        @Command(value = "cmd11", ascendants = "cmd2")
        public void eleven(String required, String otherRequired, String thirdRequired) {
            assertEquals("required", required);
            assertEquals("otherRequired", otherRequired);
            assertEquals("thirdRequired", thirdRequired);
        }

        @Command(value = "cmd12")
        public void twelve(Integer i) {
            assertEquals(12, i);
        }

        @Command(value = "cmd13")
        public void thirteen(@Optional("13") Integer i) {
            assertEquals(13, i);
        }

        @Command("cmd14")
        public void fourteen(boolean b, int i, long l, float f, double d) {
            assertTrue(b);
            assertEquals(1337, i);
            assertEquals(-1337, l);
            assertEquals(13.37f, f);
            assertEquals(-13.37, d);
        }
    }

    public static class CommandContext {

        private final String command;

        public CommandContext(String command) {
            this.command = command;
        }
    }
}
