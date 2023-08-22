package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.Suggestion;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class StringArgumentParser<C, T> implements ArgumentParser<C, T> {

    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("\"(.*?)\"");
    private static final Pattern STRING_PATTERN = Pattern.compile("(\\S+)");

    private final StringParser<T> stringParser;
    private final boolean allowQuoted;
    private final boolean greedy;
    private final @Nullable Pattern pattern;

    public StringArgumentParser(StringParser<T> stringParser, boolean allowQuoted, boolean greedy) {
        this.stringParser = stringParser;
        this.allowQuoted = allowQuoted;
        this.greedy = greedy;
        this.pattern = null;
    }

    public StringArgumentParser(StringParser<T> stringParser, boolean allowQuoted, boolean greedy, @Language("RegExp") String pattern) {
        this.stringParser = stringParser;
        this.allowQuoted = allowQuoted;
        this.greedy = greedy;
        this.pattern = Pattern.compile(pattern);
    }

    @Override
    public T parse(C context, InputReader input) throws CommandParseException {
        String string = parseString(input);

        if (pattern != null && !pattern.matcher(string).matches())
            throw new CommandParseException("'" + string + "' does not match the required pattern!");

        return stringParser.parse(string);
    }

    private String parseString(InputReader input) throws CommandParseException {
        MatchResult result = null;
        if (allowQuoted) result = input.read(QUOTED_STRING_PATTERN);
        if (greedy && result == null) return input.readRemaining();
        if (result == null) result = input.read(STRING_PATTERN);
        if (result == null) throw new CommandParseException("Could not find any String.");
        return result.group(1);
    }

    @Override
    public List<Suggestion> suggest(C context, Map<String, Object> arguments, InputReader input) {
        return Collections.emptyList();
    }

    public StringArgumentParser<C, T> withPattern(@Language("RegExp") String pattern) {
        return new StringArgumentParser<>(stringParser, allowQuoted, greedy, pattern);
    }

    @FunctionalInterface
    public interface StringParser<T> {

        T parse(String input) throws CommandParseException;

    }

    public static <C> StringArgumentParser<C, String> string() {
        return new StringArgumentParser<>(s -> s, true, false);
    }

    public static <C> StringArgumentParser<C, String> word() {
        return new StringArgumentParser<>(s -> s, false, false);
    }

    public static <C> StringArgumentParser<C, String> greedyString() {
        return new StringArgumentParser<>(s -> s, false, true);
    }

}
