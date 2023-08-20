package de.bluecolored.bluecommands;

import java.util.*;

public class ParseResult<C, T> {

    private final Collection<PreparedCommandExecutable<C, T>> matches;
    private final Collection<ParseFailure<C, T>> failures;

    public ParseResult() {
        this.matches = new LinkedList<>();
        this.failures = new ArrayList<>();
    }

    public Collection<PreparedCommandExecutable<C, T>> getMatches() {
        return matches;
    }

    public Collection<ParseFailure<C, T>> getFailures() {
        return failures;
    }

    public static class ParseFailure<C, T> {

        private final int position;
        private final String reason;
        private final LinkedList<Command<C, T>> commandStack;
        private final List<Suggestion> suggestions;

        public ParseFailure(int position, String reason, LinkedList<Command<C, T>> commandStack) {
            this(position, reason, commandStack, Collections.emptyList());
        }

        public ParseFailure(int position, String reason, LinkedList<Command<C, T>> commandStack, List<Suggestion> suggestions) {
            this.position = position;
            this.reason = reason;
            this.commandStack = commandStack;
            this.suggestions = suggestions;
        }

        public int getPosition() {
            return position;
        }

        public String getReason() {
            return reason;
        }

        public LinkedList<Command<C, T>> getCommandStack() {
            return commandStack;
        }

        public List<Suggestion> getSuggestions() {
            return suggestions;
        }

    }

}
