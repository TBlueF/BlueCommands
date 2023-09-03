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
package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.Suggestion;
import org.intellij.lang.annotations.Language;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public abstract class SimpleArgumentParser<C, T> implements ArgumentParser<C, T> {

    private static final Pattern QUOTED_STRING_PATTERN = Pattern.compile("\"(.*?)\"");
    private static final Pattern STRING_PATTERN = Pattern.compile("(\\S*)");

    private final boolean allowQuoted;
    private final boolean greedy;

    public SimpleArgumentParser(boolean allowQuoted, boolean greedy) {
        this.allowQuoted = allowQuoted;
        this.greedy = greedy;
    }

    @Override
    public final T parse(C context, InputReader input) throws CommandParseException {
        MatchResult result = null;
        if (allowQuoted) result = input.read(QUOTED_STRING_PATTERN);
        if (result == null && greedy) return parse(context, input.readRemaining());
        if (result == null) result = input.read(STRING_PATTERN);
        if (result == null) return parse(context, "");
        return parse(context, result.group(1));
    }

    public abstract T parse(C context, String string) throws CommandParseException;

    public boolean isAllowQuoted() {
        return allowQuoted;
    }

    public boolean isGreedy() {
        return greedy;
    }

}
