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
import java.util.regex.Pattern;

public class StringArgumentParser<C> extends SimpleArgumentParser<C, String> {

    private final @Nullable Pattern pattern;

    private StringArgumentParser(boolean allowQuoted, boolean greedy) {
        this(allowQuoted, greedy, null);
    }

    private StringArgumentParser(boolean allowQuoted, boolean greedy, @Language("RegExp") String pattern) {
        super(allowQuoted, greedy);
        this.pattern = pattern != null ? Pattern.compile(pattern) : null;
    }

    @Override
    public String parse(C context, String string) throws CommandParseException {
        if (pattern != null && !pattern.matcher(string).matches())
            throw new CommandParseException("'" + string + "' does not match the required pattern!");

        return string;
    }

    @Override
    public List<Suggestion> suggest(C context, InputReader input) {
        return Collections.emptyList();
    }

    public StringArgumentParser<C> withPattern(@Language("RegExp") String pattern) {
        return new StringArgumentParser<>(isAllowQuoted(), isGreedy(), pattern);
    }

    public static <C> SimpleArgumentParser<C, String> string() {
        return new StringArgumentParser<>(true, false);
    }

    public static <C> SimpleArgumentParser<C, String> word() {
        return new StringArgumentParser<>(false, false);
    }

    public static <C> SimpleArgumentParser<C, String> greedyString() {
        return new StringArgumentParser<>(false, true);
    }

}
