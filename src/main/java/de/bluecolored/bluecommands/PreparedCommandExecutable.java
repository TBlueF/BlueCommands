package de.bluecolored.bluecommands;

import java.util.Map;

public class PreparedCommandExecutable<C, T> {

    private final CommandExecutable<C, T> executable;
    private final C context;
    private final Map<String, Object> arguments;

    public PreparedCommandExecutable(CommandExecutable<C, T> executable, C context, Map<String, Object> arguments) {
        this.executable = executable;
        this.context = context;
        this.arguments = arguments;
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

}
