package de.bluecolored.bluecommands;

import java.util.LinkedList;
import java.util.Map;

public class PreparedCommandExecutable<C, T> {

    private final CommandExecutable<C, T> executable;
    private final C context;
    private final Map<String, Object> arguments;
    private final LinkedList<Command<C, T>> commandStack;

    public PreparedCommandExecutable(CommandExecutable<C, T> executable, C context, Map<String, Object> arguments, LinkedList<Command<C, T>> commandStack) {
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

    public LinkedList<Command<C, T>> getCommandStack() {
        return commandStack;
    }

}
