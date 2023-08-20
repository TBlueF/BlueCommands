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
        this.subCommands = new ArrayList<>();
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
        ParseResult<C, T> result = new ParseResult<>();
        parse(context, new InputReader(input), new ParseStack<>(), result);
        return result;
    }

    void parse(C context, InputReader input, ParseStack<C, T> stack, ParseResult<C, T> result) {
        if (!isValid(context)) return;

        int inputPosition = input.getPosition();
        if (executable != null) {
            if (input.peek() == -1)
                result.getMatches().add(new PreparedCommandExecutable<>(executable, context, stack.getArguments(), stack.getCommandStack()));
            else
                result.getFailures().add(new ParseResult.ParseFailure<>(inputPosition, "Too many arguments.", stack.getCommandStack()));
        }

        if (getClass() == Command.class || input.getPosition() == 0 || input.read() == ' ') {
            inputPosition = input.getPosition();
            for (Command<C, T> subCommand : subCommands) {
                try (var ignored = stack.push(subCommand)) {
                    input.setPosition(inputPosition);
                    subCommand.parse(context, input, stack, result);
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
