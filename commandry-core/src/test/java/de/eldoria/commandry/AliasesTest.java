package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Alias;
import de.eldoria.commandry.annotation.Command;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class AliasesTest {
    private Commandry<CommandContext> commandry;

    @BeforeEach
    void setUp() {
        commandry = new Commandry<>();
        commandry.registerCommands(TestCommandClass.class);
    }

    @Test
    void testUsingLabel() {
        commandry.runCommand(context("abc"), "abc");
    }

    @Test
    void testUsingAlias() {
        commandry.runCommand(context("a"), "a");
    }

    @Test
    void testMultipleAliases() {
        commandry.runCommand(context("d"), "d");
        commandry.runCommand(context("e"), "e");
        commandry.runCommand(context("f"), "f");
    }

    private CommandContext context(String command) {
        return new CommandContext(command);
    }

    public static class TestCommandClass {

        public TestCommandClass() {

        }

        @Command("abc")
        @Alias("a")
        public void abc(CommandContext context) {
            if (!context.command.equals("a") && !context.command.equals("abc")) {
                fail();
            }
        }

        @Command("def")
        @Alias("d,e,f")
        public void def(CommandContext context) {
            if (!"def".contains(context.command)) {
                fail();
            }
        }
    }

    public static class CommandContext {

        private final String command;

        public CommandContext(String command) {
            this.command = command;
        }
    }
}
