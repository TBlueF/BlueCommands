package de.bluecolored.bluecommands;

import java.util.*;

public class ParseData<C, T> {

    private final C context;
    private final InputReader input;
    private final ParseResult<C, T> result;
    private final LinkedList<ParseSegment<C, T>> segments;

    public ParseData(C context, String input) {
        this.context = context;
        this.input = new InputReader(input);
        this.result = new ParseResult<>(input);
        this.segments = new LinkedList<>();
    }

    public C getContext() {
        return context;
    }

    public InputReader getInput() {
        return input;
    }

    public Map<String, Object> getArguments() {
        synchronized (segments) {
            Map<String, Object> arguments = new HashMap<>();
            for (ParseSegment<C, T> segment : segments) {
                if (segment.getCommand() instanceof ArgumentCommand) {
                    ArgumentCommand<?, ?> argumentCommand = (ArgumentCommand<?, ?>) segment.getCommand();
                    arguments.put(argumentCommand.getArgumentId(), segment.getValue());
                }
            }
            return Collections.unmodifiableMap(arguments);
        }
    }

    public List<ParseSegment<C, T>> getCommandStack() {
        synchronized (segments) {
            return List.copyOf(segments);
        }
    }

    public ParseResult<C, T> getResult() {
        return result;
    }

    public ParseSegment<C, T> getCurrentSegment() {
        return segments.getLast();
    }

    public void pushSegment(Command<C, T> command) {
        synchronized (segments) {
            segments.addLast(new ParseSegment<>(command, input.getPosition()));
        }
    }

    public void popSegment() {
        synchronized (segments) {
            input.setPosition(segments.removeLast().getPosition());
        }
    }

}
