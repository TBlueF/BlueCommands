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

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import de.bluecolored.bluecommands.Command;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.ParseMatch;
import de.bluecolored.bluecommands.ParseResult;

import java.util.Comparator;
import java.util.function.Function;

class CommandCommand<C, D, T> implements com.mojang.brigadier.Command<D> {

    private final Command<C, T> command;
    private final CommandExecutionHandler<C, T> executionHandler;
    private final Function<D, C> contextConverter;

    public CommandCommand(Command<C,T> command, CommandExecutionHandler<C,T> executionHandler, Function<D, C> contextConverter) {
        this.command = command;
        this.executionHandler = executionHandler;
        this.contextConverter = contextConverter;
    }

    @Override
    public int run(CommandContext<D> context) throws CommandSyntaxException {
        InputReader inputReader = new InputReader(context.getInput());
        inputReader.setPosition(context.getRange().getStart());

        ParseResult<C, T> result = command.parse(contextConverter.apply(context.getSource()), inputReader);
        if (result.getMatches().isEmpty())
            return executionHandler.handleParseFailure(result);

        ParseMatch<C, T> executable = result.getMatches().stream()
                .max(Comparator.comparing(ParseMatch::getPriority))
                .orElseThrow(IllegalStateException::new);

        return executionHandler.handleExecution(executable.execute());
    }

}
