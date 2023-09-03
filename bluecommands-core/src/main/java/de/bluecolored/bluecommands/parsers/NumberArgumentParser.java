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

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

public class NumberArgumentParser<C, T extends Number> extends SimpleArgumentParser<C, T> {

    private final Class<T> type;
    private final StringParser<T> numberParser;
    private final double min, max;

    private NumberArgumentParser(Class<T> type, StringParser<T> numberParser) {
        super(false, false);
        this.type = type;
        this.numberParser = numberParser;
        this.min = Double.NEGATIVE_INFINITY;
        this.max = Double.POSITIVE_INFINITY;
    }

    private NumberArgumentParser(Class<T> type, StringParser<T> numberParser, double min, double max) {
        super(false, false);
        this.type = type;
        this.numberParser = numberParser;
        this.min = min;
        this.max = max;
    }

    @Override
    public T parse(C context, String string) throws CommandParseException {
        T result = numberParser.parse(string);
        if (result.doubleValue() < min) throw new CommandParseException(result + " is too small. It has to be greater or equal to " + min);
        if (result.doubleValue() > max) throw new CommandParseException(result + " is too big. It has to be smaller or equal to " + max);
        return result;
    }

    @Override
    public List<Suggestion> suggest(C context, InputReader input) {
        return Collections.emptyList();
    }

    public Class<T> getType() {
        return type;
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public NumberArgumentParser<C, T> withBounds(double min, double max) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public NumberArgumentParser<C, T> withMin(double min) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public NumberArgumentParser<C, T> withMax(double max) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public static <C> NumberArgumentParser<C, Byte> forBytes() {
        return createWrapped(Byte.class, Byte::parseByte);
    }

    public static <C> NumberArgumentParser<C, Short> forShorts() {
        return createWrapped(Short.class, Short::parseShort);
    }

    public static <C> NumberArgumentParser<C, Integer> forIntegers() {
        return createWrapped(Integer.class, Integer::parseInt);
    }

    public static <C> NumberArgumentParser<C, Long> forLongs() {
        return createWrapped(Long.class, Long::parseLong);
    }

    public static <C> NumberArgumentParser<C, Float> forFloats() {
        return createWrapped(Float.class, Float::parseFloat);
    }

    public static <C> NumberArgumentParser<C, Double> forDoubles() {
        return createWrapped(Double.class, Double::parseDouble);
    }

    private static <C, T extends Number> NumberArgumentParser<C, T> createWrapped(Class<T> type, Function<String, T> numberParser) {
        return new NumberArgumentParser<>(type, wrap(numberParser, type));
    }

    private static <T> StringParser<T> wrap(Function<String, T> numberParser, Class<T> type) {
        return s -> {
            try {
                return numberParser.apply(s);
            } catch (RuntimeException ex) {
                throw new CommandParseException("'" + s +"' is not a vaild " + type.getSimpleName());
            }
        };
    }

    private interface StringParser<T> {
        T parse(String string) throws CommandParseException;
    }

}
