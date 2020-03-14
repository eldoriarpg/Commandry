package de.eldoria.commandry.annotation.processing;

import de.eldoria.commandry.annotation.Alias;
import de.eldoria.commandry.annotation.Command;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Messager;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;
import java.util.Set;
import java.util.function.Predicate;
import java.util.regex.Pattern;

@SupportedAnnotationTypes({"de.eldoria.commandry.annotation.Command", "de.eldoria.commandry.annotation.Alias"})
public class CommandProcessor extends AbstractProcessor {
    private static final Pattern VALID_COMMAND_PATTERN = Pattern.compile("[^\\s,.<>\\[\\]{}]+");
    private static final String MULTI_SPLITERATOR = ",";

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        var messager = processingEnv.getMessager();
        var predicate = VALID_COMMAND_PATTERN.asMatchPredicate();
        for (var typeElement : annotations) {
            for (var element : roundEnv.getElementsAnnotatedWith(typeElement)) {
                Command command = element.getAnnotation(Command.class);
                if (command == null) {
                    messager.printMessage(Diagnostic.Kind.ERROR, "No @Command annotation found.");
                    continue;
                }
                if (typeElement.getQualifiedName().contentEquals(Command.class.getTypeName())) {
                    single(command.value(), predicate, messager);

                    if (!command.ascendants().isEmpty()) { // not default
                        multi(command.ascendants(), predicate, messager);
                    }
                } else {
                    Alias alias = element.getAnnotation(Alias.class);
                    if (alias != null) {
                        multi(alias.value(), predicate, messager);
                    }
                }
            }
        }
        return false;
    }

    private void single(String arg, Predicate<String> predicate, Messager messager) {
        if (arg.isBlank()) {
            messager.printMessage(Diagnostic.Kind.ERROR, "Command name must not be empty.");
        } else if (!predicate.test(arg)) {
            messager.printMessage(Diagnostic.Kind.ERROR,
                    "Invalid command name, must match " + VALID_COMMAND_PATTERN.toString() + " but was " + arg);
        }
    }

    private void multi(String arg, Predicate<String> singlePredicate, Messager messager) {
        if (!arg.contains(MULTI_SPLITERATOR)) {
            single(arg, singlePredicate, messager);
        } else {
            String[] split = arg.split(MULTI_SPLITERATOR, -1);
            for (String a : split) {
                single(a, singlePredicate, messager);
            }
        }
    }
}
