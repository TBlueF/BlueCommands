package de.bluecolored.bluecommands;

import java.util.Collections;
import java.util.List;

public class ParseFailure<C, T> {

    private final int position;
    private final String reason;
    private final List<ParseSegment<C, T>> commandStack;
    private final List<Suggestion> suggestions;

    public ParseFailure(int position, String reason, List<ParseSegment<C, T>> commandStack) {
        this(position, reason, commandStack, Collections.emptyList());
    }

    public ParseFailure(int position, String reason, List<ParseSegment<C, T>> commandStack, List<Suggestion> suggestions) {
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

    public List<ParseSegment<C, T>> getCommandStack() {
        return commandStack;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

}
