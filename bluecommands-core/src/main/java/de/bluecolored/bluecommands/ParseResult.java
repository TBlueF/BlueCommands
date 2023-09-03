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

public class ParseResult<C, T> {

    private final C context;
    private final String input;
    private final Collection<ParseMatch<C, T>> matches;
    private final Collection<ParseFailure<C, T>> failures;

    public ParseResult(C context, String input) {
        this.context = context;
        this.input = input;
        this.matches = new ArrayList<>();
        this.failures = new ArrayList<>();
    }

    public C getContext() {
        return context;
    }

    public String getInput() {
        return input;
    }

    public Collection<ParseMatch<C, T>> getMatches() {
        return Collections.unmodifiableCollection(matches);
    }

    void addMatch(ParseMatch<C, T> match) {
        matches.add(match);
    }

    public Collection<ParseFailure<C, T>> getFailures() {
        return Collections.unmodifiableCollection(failures);
    }

    void addFailure(ParseFailure<C, T> failure) {
        failures.add(failure);
    }

}
