package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.annotation.Optional;
import de.eldoria.commandry.context.CommandContext;
import de.eldoria.commandry.exception.CommandExecutionException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CommandryTest {
    private Commandry<SimpleCommandContext> commandry;

    @BeforeEach
    void setUp() {
        commandry = new Commandry<>();
        commandry.registerCommands(TestCommandClass.class);
    }

    @Test
    void testEmptyInput() {
        assertThrows(CommandExecutionException.class, () -> commandry.runCommand(context("anything"), ""));
    }

    @Test
    void testInvalidCommand() {
        assertThrows(CommandExecutionException.class, () -> commandry.runCommand(context("anything"), "invalid"));
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

    @Test
    void testSingleSubCommandOfSingleCommandWithoutParameters() {
        commandry.runCommand(context("..."), "cmd1 cmd9");
    }

    @Test
    void testSubCommandOfSingleCommandWithParameters() {
        commandry.runCommand(context(""), "cmd1 cmd10 ccc");
    }

    @Test
    void testSubCommandWithParametersOfSingleCommandWithParameters() {
        commandry.runCommand(context(""), "cmd2 required otherRequired cmd11 thirdRequired");
    }

    @Test
    void testInputParsing() {
        commandry.runCommand(context(""), "cmd12 12");
    }

    @Test
    void testOptionalParsing() {
        commandry.runCommand(context(""), "cmd13");
    }

    @Test
    void testPrimitiveParsing() {
        commandry.runCommand(context(""), "cmd14 true 1337 -1337 13.37 -13.37");
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

        @Command(value = "cmd9", parents = "cmd1")
        public void nine() {
            assertTrue(true);
        }

        @Command(value = "cmd10", parents = "cmd1")
        public void ten(String ccc, @Optional("hello world") String text) {
            assertEquals("ccc", ccc);
            assertEquals("hello world", text);
        }

        @Command(value = "cmd11", parents = "cmd2")
        public void eleven(String required, String otherRequired, String thirdRequired) {
            assertEquals("required", required);
            assertEquals("otherRequired", otherRequired);
            assertEquals("thirdRequired", thirdRequired);
        }

        @Command(value = "cmd12")
        public void twelve(Integer i){
            assertEquals(12, i);
        }

        @Command(value = "cmd13")
        public void thirteen(@Optional("13") Integer i){
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

    public static class SimpleCommandContext extends CommandContext<SimpleCommandContext> {

        public SimpleCommandContext(String command) {
            super(command);
        }
    }
}
