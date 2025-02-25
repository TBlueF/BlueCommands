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

import java.util.*;

public class ParseData<C, T> {

    private final C context;
    private final InputReader input;
    private final ParseResult<C, T> result;
    private final LinkedList<ParseSegment<C, T>> segments;

    public ParseData(C context, InputReader input, Command<C, T> initialSegment) {
        this.context = context;
        this.input = input;
        this.result = new ParseResult<>(context, input.getInput());
        this.segments = new LinkedList<>();
        pushSegment(initialSegment);
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
