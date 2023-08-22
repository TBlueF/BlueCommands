package de.bluecolored.bluecommands;

import java.util.List;
import java.util.Map;

public class ParseMatch<C, T> {

    private final CommandExecutable<C, T> executable;
    private final C context;
    private final Map<String, Object> arguments;
    private final List<ParseSegment<C, T>> commandStack;

    public ParseMatch(CommandExecutable<C, T> executable, C context, Map<String, Object> arguments, List<ParseSegment<C, T>> commandStack) {
        this.executable = executable;
        this.context = context;
        this.arguments = arguments;
        this.commandStack = commandStack;
    }

    public T execute() {
        return executable.execute(context, arguments);
    }

    public CommandExecutable<C, T> getExecutable() {
        return executable;
    }

    public C getContext() {
        return context;
    }

    public Map<String, Object> getArguments() {
        return arguments;
    }

    public int getPriority() {
        return executable.getPriority();
    }

    public List<ParseSegment<C, T>> getCommandStack() {
        return commandStack;
    }

}
