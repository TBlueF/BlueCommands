package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.Suggestion;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.Pattern;

public class StringArgumentParser<C> extends SimpleArgumentParser<C, String> {

    private final @Nullable Pattern pattern;

    private StringArgumentParser(boolean allowQuoted, boolean greedy) {
        this(allowQuoted, greedy, null);
    }

    private StringArgumentParser(boolean allowQuoted, boolean greedy, @Language("RegExp") String pattern) {
        super(allowQuoted, greedy);
        this.pattern = pattern != null ? Pattern.compile(pattern) : null;
    }

    @Override
    public String parse(C context, String string) throws CommandParseException {
        if (pattern != null && !pattern.matcher(string).matches())
            throw new CommandParseException("'" + string + "' does not match the required pattern!");

        return string;
    }

    @Override
    public List<Suggestion> suggest(C context, InputReader input) {
        return Collections.emptyList();
    }

    public StringArgumentParser<C> withPattern(@Language("RegExp") String pattern) {
        return new StringArgumentParser<>(isAllowQuoted(), isGreedy(), pattern);
    }

    public static <C> SimpleArgumentParser<C, String> string() {
        return new StringArgumentParser<>(true, false);
    }

    public static <C> SimpleArgumentParser<C, String> word() {
        return new StringArgumentParser<>(false, false);
    }

    public static <C> SimpleArgumentParser<C, String> greedyString() {
        return new StringArgumentParser<>(false, true);
    }

}
