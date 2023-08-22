package de.bluecolored.bluecommands;

public class ParseSegment<C, T> {

    private final int position;
    private final Command<C, T> command;
    private Object value;

    public ParseSegment(Command<C, T> command, int position) {
        this.position = position;
        this.command = command;
        this.value = null;
    }

    public int getPosition() {
        return position;
    }

    public Command<C, T> getCommand() {
        return command;
    }

    public Object getValue() {
        return value;
    }

    void setValue(Object value) {
        this.value = value;
    }

}
