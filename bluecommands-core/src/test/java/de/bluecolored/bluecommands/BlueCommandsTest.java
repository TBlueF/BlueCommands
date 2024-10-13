package de.bluecolored.bluecommands;

import de.bluecolored.bluecommands.annotations.Argument;
import de.bluecolored.bluecommands.annotations.Command;
import de.bluecolored.bluecommands.annotations.ParserType;
import de.bluecolored.bluecommands.parsers.ArgumentParser;
import de.bluecolored.bluecommands.parsers.SimpleArgumentParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;

public class BlueCommandsTest {

    private de.bluecolored.bluecommands.Command<?, ?> commands;

    @BeforeEach
    public void init() {
        BlueCommands<?> blueCommands = new BlueCommands<>();
        commands = blueCommands.createCommand(this);
    }

    @Test
    public void testBasicCommand() {
        var result = commands.parse(null, "test arg1 arg2");
        assertEquals(1, result.getMatches().size());

        var match = result.getMatches().iterator().next();
        assertNull(match.getContext());
        var expected = new HashMap<>();
        expected.put("with", "arg1");
        expected.put("some", null);
        expected.put("arguments", "arg2");
        assertEquals(expected, match.getArguments());
        assertEquals(4, match.getCommandStack().size());

        var stack1 = match.getCommandStack().get(0);
        assertEquals(0, stack1.getPosition());
        assertInstanceOf(LiteralCommand.class, stack1.getCommand());
        assertNull(stack1.getValue());

        var stack2 = match.getCommandStack().get(1);
        assertEquals(5, stack2.getPosition());
        assertInstanceOf(ArgumentCommand.class, stack2.getCommand());
        assertEquals("arg1", stack2.getValue());

        var stack3 = match.getCommandStack().get(2);
        assertEquals(10, stack3.getPosition());
        assertInstanceOf(ArgumentCommand.class, stack3.getCommand());
        assertNull(stack3.getValue());

        var stack4 = match.getCommandStack().get(3);
        assertEquals(10, stack4.getPosition());
        assertInstanceOf(ArgumentCommand.class, stack4.getCommand());
        assertEquals("arg2", stack4.getValue());
    }

    @Test
    public void testSuggestionWithTrailingSpaceStart() {
        var result = commands.parse(null, "test ");
        assertEquals(Set.of(
                "suggestion1_1", "suggestion1_2",
                "suggestion4_1", "suggestion4_2"
        ), allSuggestions(result));
    }

    @Test
    public void testSuggestionWithoutTrailingSpaceBeforeOptional() {
        var result = commands.parse(null, "test arg1");
        assertEquals(Set.of(
                "suggestion1_1", "suggestion1_2",
                "suggestion4_1", "suggestion4_2"
        ), allSuggestions(result));
    }

    @Test
    public void testSuggestionWithTrailingSpaceOptional() {
        var result = commands.parse(null, "test arg1 ");
        assertEquals(Set.of(
                "suggestion2_1", "suggestion2_2",
                "suggestion3_1", "suggestion3_2"
        ), allSuggestions(result));
    }

    @Test
    public void testSuggestionWithoutTrailingSpace() {
        var result = commands.parse(null, "test arg1 arg2");
        assertEquals(Set.of(
                "suggestion2_1", "suggestion2_2",
                "suggestion3_1", "suggestion3_2"
        ), allSuggestions(result));
    }

    @Test
    public void testSuggestionWithTrailingSpace() {
        var result = commands.parse(null, "test arg1 arg2 ");
        assertEquals(Set.of(
                "suggestion3_1", "suggestion3_2"
        ), allSuggestions(result));
    }

    private static Set<String> allSuggestions(ParseResult<?, ?> result) {
        return result.getFailures().stream()
                .map(ParseFailure::getSuggestions)
                .flatMap(List::stream)
                .map(Suggestion::getString)
                .collect(Collectors.toSet());
    }

    @Command("test <with> [some] <arguments>")
    public void testWithArgs(
            @ParserType(StringWithSuggestions.class) @Argument("with") String with,
            @ParserType(StringWithSuggestions2.class) @Argument("some") String some,
            @ParserType(StringWithSuggestions3.class) @Argument("arguments") String arguments
    ) {}

    @Command("test <single-argument>")
    public void testWithArgs(
            @ParserType(StringWithSuggestions4.class) @Argument("single-argument") String singleArgument
    ) {}

    public static class StringWithSuggestions<C> extends SimpleArgumentParser<C, String> implements ArgumentParser<C, String> {

        public StringWithSuggestions() {
            super(true, false);
        }

        @Override
        public String parse(C context, String string) throws CommandParseException {
            return string;
        }

        @Override
        public List<Suggestion> suggest(C context, InputReader input) {
            return List.of(
                    new SimpleSuggestion("suggestion1_1"),
                    new SimpleSuggestion("suggestion1_2")
            );
        }

    }

    public static class StringWithSuggestions2<C> extends SimpleArgumentParser<C, String> {

        public StringWithSuggestions2() {
            super(true, false);
        }

        @Override
        public String parse(C context, String string) throws CommandParseException {
            return string;
        }

        @Override
        public List<Suggestion> suggest(C context, InputReader input) {
            return List.of(
                    new SimpleSuggestion("suggestion2_1"),
                    new SimpleSuggestion("suggestion2_2")
            );
        }

    }

    public static class StringWithSuggestions3<C> extends SimpleArgumentParser<C, String> {

        public StringWithSuggestions3() {
            super(true, false);
        }

        @Override
        public String parse(C context, String string) throws CommandParseException {
            return string;
        }

        @Override
        public List<Suggestion> suggest(C context, InputReader input) {
            return List.of(
                    new SimpleSuggestion("suggestion3_1"),
                    new SimpleSuggestion("suggestion3_2")
            );
        }

    }

    public static class StringWithSuggestions4<C> extends SimpleArgumentParser<C, String> {

        public StringWithSuggestions4() {
            super(true, false);
        }

        @Override
        public String parse(C context, String string) throws CommandParseException {
            return string;
        }

        @Override
        public List<Suggestion> suggest(C context, InputReader input) {
            return List.of(
                    new SimpleSuggestion("suggestion4_1"),
                    new SimpleSuggestion("suggestion4_2")
            );
        }

    }

}