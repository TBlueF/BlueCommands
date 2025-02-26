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
package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.Message;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.bluecolored.bluecommands.ParseFailure;
import de.bluecolored.bluecommands.ParseMatch;
import de.bluecolored.bluecommands.ParseResult;

import java.util.Comparator;

public class DefaultExecutionHandler<C, T> implements CommandExecutionHandler<C, T> {
    private static final Message DEFAULT_FAILURE_MESSAGE = () -> "Unknown or incomplete command!";

    @Override
    public int handle(ParseResult<C, T> parseResult) throws CommandSyntaxException {
        if (parseResult.getMatches().isEmpty())
            return handleParseFailure(parseResult);

        ParseMatch<C, T> executable = parseResult.getMatches().stream()
                .max(Comparator.comparing(ParseMatch::getPriority))
                .orElseThrow(IllegalStateException::new);

        T executionResult;
        try {
            executionResult = executable.execute();
        } catch (Exception exception) {
            return handleExecutionException(executable.getContext(), exception);
        }

        return handleExecution(executable.getContext(), executionResult);
    }

    public int handleParseFailure(ParseResult<C, T> result) throws CommandSyntaxException {
        ParseFailure<C, ?> failure = result.getFailures().stream()
                .max(Comparator.comparing(ParseFailure::getPosition))
                .orElseThrow(() -> new CommandSyntaxException(
                        new SimpleCommandExceptionType(DEFAULT_FAILURE_MESSAGE),
                        DEFAULT_FAILURE_MESSAGE
                ));
        throw new CommandSyntaxException(
                new SimpleCommandExceptionType(failure::getReason),
                failure::getReason,
                result.getInput(),
                failure.getPosition()
        );
    }

    public int handleExecution(C context, T result) {
        if (result instanceof Number)
            return ((Number) result).intValue();

        return 1;
    }

    public int handleExecutionException(C context, Throwable throwable) {
        throw new RuntimeException(throwable);
    }

}
