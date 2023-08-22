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

    public String getLiteral() {
        return literal;
    }

    @Override
    void parse(ParseData<C, T> data) {
        C context = data.getContext();
        if (!isValid(context)) return;

        InputReader input = data.getInput();
        MatchResult match = input.read(PATTERN);
        if (match == null || !match.group().equals(literal)) {
            data.getResult().addFailure(new ParseFailure<>(
                    data.getCurrentSegment().getPosition(),
                    match == null ?
                            String.format("Expected '%s', but got something else.", literal) :
                            String.format("Expected '%s', but got '%s'.", literal, match.group()),
                    data.getCommandStack(),
                    Collections.singletonList(new SimpleSuggestion(literal))
            ));
        } else {
            super.parse(data);
        }
    }

    @Override
    public boolean isEqual(Command<C, T> other) {
        if (getClass() != other.getClass()) return false;
        return ((LiteralCommand<C, T>) other).literal.equals(literal);
    }

}
