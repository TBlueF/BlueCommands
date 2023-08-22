package de.bluecolored.bluecommands;

import java.util.*;

public class ParseResult<C, T> {

    private final String input;
    private final Collection<ParseMatch<C, T>> matches;
    private final Collection<ParseFailure<C, T>> failures;

    public ParseResult(String input) {
        this.input = input;
        this.matches = new ArrayList<>();
        this.failures = new ArrayList<>();
    }

    public String getInput() {
        return input;
    }

    public Collection<ParseMatch<C, T>> getMatches() {
        return Collections.unmodifiableCollection(matches);
    }

    void addMatch(ParseMatch<C, T> match) {
        matches.add(match);
    }

    public Collection<ParseFailure<C, T>> getFailures() {
        return Collections.unmodifiableCollection(failures);
    }

    void addFailure(ParseFailure<C, T> failure) {
        failures.add(failure);
    }

}
