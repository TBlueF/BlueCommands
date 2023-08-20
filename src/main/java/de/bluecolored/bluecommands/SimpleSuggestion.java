package de.bluecolored.bluecommands;

public class SimpleSuggestion implements Suggestion {

    private final String string;

    public SimpleSuggestion(String string) {
        this.string = string;
    }

    @Override
    public String getString() {
        return string;
    }

}
