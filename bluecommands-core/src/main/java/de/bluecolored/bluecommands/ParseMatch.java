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
