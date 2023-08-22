package de.bluecolored.bluecommands;

import org.jetbrains.annotations.Nullable;

import java.util.*;

public class Command<C, T> {

    private final Collection<Command<C, T>> subCommands;
    private @Nullable CommandExecutable<C, T> executable;

    private transient C lastValidationContext;
    private transient boolean lastValidationResult;
    private transient long lastValidationTime;

    public Command() {
        this.subCommands = new ArrayList<>(1);
        this.executable = null;
    }

    public @Nullable CommandExecutable<C, T> getExecutable() {
        return executable;
    }

    public void setExecutable(@Nullable CommandExecutable<C, T> executable) {
        this.executable = executable;
    }

    public Collection<Command<C, T>> getSubCommands() {
        return subCommands;
    }

    public ParseResult<C, T> parse(C context, String input) {
        ParseData<C, T> stack = new ParseData<>(context, input);
        parse(stack);
        return stack.getResult();
    }

    void parse(ParseData<C, T> data) {
        if (!isValid(data.getContext())) return;

        InputReader input = data.getInput();
        int inputPosition = input.getPosition();
        if (executable != null) {
            if (input.peek() == -1)
                data.getResult().addMatch(new ParseMatch<>(executable, data.getContext(), data.getArguments(), data.getCommandStack()));
            else
                data.getResult().addFailure(new ParseFailure<>(inputPosition, "Too many arguments.", data.getCommandStack()));
        }

        if (getClass() == Command.class || inputPosition == 0 || input.read() == ' ') {
            for (Command<C, T> subCommand : subCommands) {
                try {
                    data.pushSegment(subCommand);
                    subCommand.parse(data);
                } finally {
                    data.popSegment();
                }
            }
        }
    }

    public synchronized boolean isValid(C context) {
        long now = System.currentTimeMillis();
        if (lastValidationTime < now - 1000 || lastValidationContext == null || !lastValidationContext.equals(context)) {
            lastValidationResult = checkValid(context);
            lastValidationContext = context;
            lastValidationTime = now;
        }
        return lastValidationResult;
    }

    private boolean checkValid(C context) {
        if (executable != null && executable.isValid(context))
            return true;

        for (Command<C, T> subCommand : subCommands) {
            if (subCommand.isValid(context))
                return true;
        }

        return false;
    }

    /**
     * Returns true if this command is equal to the provided command, ignoring any subcommands.
     */
    public boolean isEqual(Command<C, T> other) {
        return getClass() == other.getClass();
    }

    public boolean tryMerge(Command<C, T> other) {
        if (!isEqual(other)) return false;
        merge(other);
        return true;
    }

    private void merge(Command<C, T> other) {
        if (other.executable != null) {
            if (this.executable != null) throw new CommandSetupException("Ambiguous command executable!");
            this.executable = other.executable;
        }

        for (Command<C, T> otherSubCommand : other.subCommands) {
            addSubCommand(otherSubCommand);
        }
    }

    public void addSubCommand(Command<C, T> subCommand) {
        if (subCommand.getClass() == Command.class) {
            merge(subCommand);
            return;
        }

        boolean merged = false;
        for (Command<C, T> thisSubCommand : this.subCommands) {
            if (thisSubCommand.tryMerge(subCommand)) {
                merged = true;
                break;
            }
        }
        if (!merged) this.subCommands.add(subCommand);
    }

}
