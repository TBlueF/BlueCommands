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

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.Reader;
import java.util.Objects;
import java.util.regex.MatchResult;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * An improved {@link java.io.StringReader} (more lenient, fewer exceptions and more options,
 * e.g. to get and change the reading position).
 */
public class InputReader extends Reader {

    private final String input;
    private final int length;
    private int position = 0;
    private int mark = 0;

    public InputReader(String input) {
        this.input = input;
        this.length = input.length();
    }

    @Override
    public int read() {
        if (position >= length) return -1;
        return input.charAt(position++);
    }

    public int peek() {
        if (position >= length) return -1;
        return input.charAt(position);
    }

    @Override
    public int read(char @NotNull [] charBuffer, int offset, int length) {
        Objects.checkFromIndexSize(offset, length, charBuffer.length);
        if (length == 0) return 0;
        if (position >= this.length) return -1;
        int count = Math.min(this.length - position, length);
        input.getChars(position, position + count, charBuffer, offset);
        position += count;
        return count;
    }

    @Override
    public int read(char @NotNull [] charBuffer) {
        return read(charBuffer, 0, charBuffer.length);
    }

    /**
     * Reads and consumes the next pattern-match,
     * but only if the pattern is matching and the match is starting from the current reading position.
     */
    public @Nullable MatchResult read(@NotNull Pattern pattern) {
        MatchResult result = peek(pattern);
        if (result != null)
            position = result.end();
        return result;
    }

    /**
     * Peeks the next pattern-match,
     * but only if the pattern is matching and the match is starting from the current reading position.
     */
    public @Nullable MatchResult peek(@NotNull Pattern pattern) {
        Matcher matcher = pattern.matcher(input);
        matcher.region(position, length);
        if (matcher.find() && matcher.start() == position)
            return matcher;
        return null;
    }

    @Override
    public boolean ready() {
        return true;
    }

    @Override
    public long skip(long n) {
        if (position >= length) return 0;
        // Bound skip by beginning and end of the source
        long r = Math.min(length - position, n);
        r = Math.max(-position, r);
        position += r;
        return r;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public void mark(int readAheadLimit) {
        mark();
    }

    public void mark() {
        this.mark = position;
    }

    @Override
    public void reset() {
        this.position = mark;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getRemaining() {
        return length - position;
    }

    public String peekRemaining() {
        return this.input.substring(position);
    }

    public String readRemaining() {
        String remaining = peekRemaining();
        position = length;
        return remaining;
    }

    public String getInput() {
        return input;
    }

    @Override
    public void close() {}

}
