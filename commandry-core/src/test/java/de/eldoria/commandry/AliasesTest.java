package de.eldoria.commandry;

import de.eldoria.commandry.annotation.Alias;
import de.eldoria.commandry.annotation.Command;
import de.eldoria.commandry.context.CommandContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.fail;

public class AliasesTest {
    private Commandry<SimpleCommandContext> commandry;

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

    private SimpleCommandContext context(String command) {
        return new SimpleCommandContext(command);
    }

    public static class TestCommandClass {

        public TestCommandClass() {

        }

        @Command("abc")
        @Alias("a")
        public void abc(SimpleCommandContext context) {
            if (!context.getCommand().equals("a") && !context.getCommand().equals("abc")) {
                fail();
            }
        }

        @Command("def")
        @Alias("d,e,f")
        public void def(SimpleCommandContext context) {
            if (!"def".contains(context.getCommand())) {
                fail();
            }
        }
    }

    public static class SimpleCommandContext extends CommandContext<SimpleCommandContext> {

        public SimpleCommandContext(String command) {
            super(command);
        }
    }
}
