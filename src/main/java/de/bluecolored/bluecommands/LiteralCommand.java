package de.bluecolored.bluecommands;

import java.util.Collections;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class LiteralCommand<C, T> extends Command<C, T> {
    private static final Pattern PATTERN = Pattern.compile("\\S*");

    private final String literal;

    public LiteralCommand(String literal) {
        this.literal = literal;
    }

    @Override
    void parse(C context, InputReader input, ParseStack<C, T> stack, ParseResult<C, T> result) {
        if (!isValid(context)) return;

        int position = input.getPosition();
        MatchResult match = input.read(PATTERN);
        if (match == null || !match.group().equals(literal)) {
            result.getFailures().add(new ParseResult.ParseFailure<>(
                    position,
                    match == null ?
                            String.format("Expected '%s', but got something else.", literal) :
                            String.format("Expected '%s', but got '%s'.", literal, match.group()),
                    stack.getCommandStack(),
                    Collections.singletonList(new SimpleSuggestion(literal))
            ));
        } else {
            super.parse(context, input, stack, result);
        }
    }

    @Override
    public boolean isEqual(Command<C, T> other) {
        if (getClass() != other.getClass()) return false;
        return ((LiteralCommand<C, T>) other).literal.equals(literal);
    }

}
