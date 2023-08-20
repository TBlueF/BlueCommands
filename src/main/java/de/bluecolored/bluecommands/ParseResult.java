package de.bluecolored.bluecommands;

import java.util.*;

public class ParseResult<C, T> {

    private final Collection<PreparedCommandExecutable<C, T>> matches;
    private final Collection<ParseFailure> failures;

    public ParseResult() {
        this.matches = new LinkedList<>();
        this.failures = new ArrayList<>();
    }

    public Collection<PreparedCommandExecutable<C, T>> getMatches() {
        return matches;
    }

    public Collection<ParseFailure> getFailures() {
        return failures;
    }

    public static class ParseFailure {

        private final int position;
        private final String reason;
        private final List<Suggestion> suggestions;

        public ParseFailure(int position, String reason) {
            this(position, reason, Collections.emptyList());
        }

        public ParseFailure(int position, String reason, List<Suggestion> suggestions) {
            this.position = position;
            this.reason = reason;
            this.suggestions = suggestions;
        }

        public int getPosition() {
            return position;
        }

        public String getReason() {
            return reason;
        }

        public List<Suggestion> getSuggestions() {
            return suggestions;
        }

    }

}
