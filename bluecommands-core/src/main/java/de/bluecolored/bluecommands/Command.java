/*
 * This file is part of BlueCommands, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluecommands;

import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.function.Consumer;

public class Command<C, T> {

    private final Collection<Command<C, T>> subCommands;
    private @Nullable CommandExecutable<C, T> executable;

    private transient C lastValidationContext;
    private transient boolean lastValidationResult;
    private transient long lastValidationTime;

    private transient boolean lastTreeOptionalResult;
    private transient long lastTreeOptionalTime;

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
        return parse(context, new InputReader(input));
    }

    public ParseResult<C, T> parse(C context, InputReader input) {
        ParseData<C, T> stack = new ParseData<>(context, input);
        parse(stack);
        return stack.getResult();
    }

    void parse(ParseData<C, T> data) {
        if (!isValid(data.getContext())) return;

        InputReader input = data.getInput();
        int inputPosition = input.getPosition();
        if (executable != null && executable.isValid(data.getContext())) {
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
        } else if (!subCommands.isEmpty()) {
            if (isSubTreeOptional()) {
                gatherAllExecutables(data.getContext(), executable -> {
                    data.getResult().addMatch(new ParseMatch<>(executable, data.getContext(), data.getArguments(), data.getCommandStack()));
                });
            } else {
                data.getResult().addFailure(new ParseFailure<>(inputPosition, "Not enough arguments!", data.getCommandStack()));
            }
        }
    }

    public boolean isOptional() {
        return false;
    }

    private void gatherAllExecutables(C context, Consumer<CommandExecutable<C, T>> consumer) {
        if (this.executable != null && this.isValid(context))
            consumer.accept(executable);

        for (Command<C, T> subCommand : subCommands)
            subCommand.gatherAllExecutables(context, consumer);
    }

    private boolean isSubTreeOptional() {
        for (Command<C, T> subCommand : subCommands)
            if (!subCommand.isTreeOptional()) return false;
        return true;
    }

    @SuppressWarnings("BooleanMethodIsAlwaysInverted")
    private boolean isTreeOptional() {
        long now = System.currentTimeMillis();
        if (lastTreeOptionalTime < now - 1000) {
            lastTreeOptionalResult = checkTreeOptional();
            lastTreeOptionalTime = now;
        }
        return lastTreeOptionalResult;
    }

    private boolean checkTreeOptional() {
        if (!isOptional()) return false;
        for (Command<C, T> subCommand : subCommands)
            if (!subCommand.isTreeOptional()) return false;
        return true;
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
