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
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class LiteralCommand<C, T> extends Command<C, T> {
    private static final Pattern PATTERN = Pattern.compile("\\S*");

    private final String literal;

    public LiteralCommand(String literal) {
        this.literal = literal;
    }

    public String getLiteral() {
        return literal;
    }

    @Override
    void parse(ParseData<C, T> data) {
        C context = data.getContext();
        if (!isValid(context)) return;

        InputReader input = data.getInput();
        MatchResult match = input.read(PATTERN);
        if (match == null || !match.group().equals(literal)) {
            data.getResult().addFailure(new ParseFailure<>(
                    data.getCurrentSegment().getPosition(),
                    match == null ?
                            String.format("Expected '%s', but got something else.", literal) :
                            String.format("Expected '%s', but got '%s'.", literal, match.group()),
                    data.getCommandStack(),
                    Collections.singletonList(new SimpleSuggestion(literal))
            ));
        } else {
            super.parse(data);
        }
    }

    @Override
    public boolean isEqual(Command<C, T> other) {
        if (getClass() != other.getClass()) return false;
        return ((LiteralCommand<C, T>) other).literal.equals(literal);
    }

}
