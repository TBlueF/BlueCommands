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

import java.util.Collections;
import java.util.List;

public class ParseFailure<C, T> {

    private final int position;
    private final String reason;
    private final List<ParseSegment<C, T>> commandStack;
    private final List<Suggestion> suggestions;

    public ParseFailure(int position, String reason, List<ParseSegment<C, T>> commandStack) {
        this(position, reason, commandStack, Collections.emptyList());
    }

    public ParseFailure(int position, String reason, List<ParseSegment<C, T>> commandStack, List<Suggestion> suggestions) {
        this.position = position;
        this.reason = reason;
        this.commandStack = commandStack;
        this.suggestions = suggestions;
    }

    public int getPosition() {
        return position;
    }

    public String getReason() {
        return reason;
    }

    public List<ParseSegment<C, T>> getCommandStack() {
        return commandStack;
    }

    public List<Suggestion> getSuggestions() {
        return suggestions;
    }

}
