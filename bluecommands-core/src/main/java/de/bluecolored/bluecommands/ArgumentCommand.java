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

import de.bluecolored.bluecommands.parsers.ArgumentParser;

import java.util.Collection;
import java.util.List;

public class ArgumentCommand<C, T> extends Command<C, T> {

    private final String argumentId;
    private final ArgumentParser<C, ?> argumentParser;
    private final boolean optional;

    public ArgumentCommand(String argumentId, ArgumentParser<C, ?> argumentParser, boolean optional) {
        this.argumentId = argumentId;
        this.argumentParser = argumentParser;
        this.optional = optional;
    }

    public String getArgumentId() {
        return argumentId;
    }

    public ArgumentParser<C, ?> getArgumentParser() {
        return argumentParser;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    @Override
    void parse(ParseData<C, T> data) {
        C context = data.getContext();
        if (!isValid(context)) return;

        InputReader input = data.getInput();
        int position = input.getPosition();

        int matchCount = data.getResult().getMatches().size();

        try {
            Object argument = argumentParser.parse(context, input);

            // sanity check position
            if (input.getPosition() < position) {
                throw new CommandSetupException("The ArgumentParser '" + argumentParser + "' altered the InputReader in an illegal way. (position changed backwards)");
            }

            // check if full token was consumed
            int next = input.peek();
            if (next != -1 && next != ' ') {
                throw new CommandSetupException("The ArgumentParser '" + argumentParser + "' did not consume the full token. (expected next char to be a space or end of string)");
            }

            // if we reached the end of input, check if there is any additional suggestions and add an extra failure if there are any
            if (next == -1) {
                input.setPosition(position); // reset position for suggestions
                List<Suggestion> extraSuggesions = argumentParser.suggest(context, input);
                if (!extraSuggesions.isEmpty()) {
                    data.getResult().addFailure(new ParseFailure<>(
                            position,
                            "Alternative Usages",
                            data.getCommandStack(),
                            extraSuggesions
                    ));
                }
                input.readRemaining(); // reset position
            }

            data.getCurrentSegment().setValue(argument);
            super.parse(data);
        } catch (CommandParseException ex) {
            input.setPosition(position); // reset position for suggestions
            data.getResult().addFailure(new ParseFailure<>(
                position,
                ex.getMessage(),
                data.getCommandStack(),
                argumentParser.suggest(context, input)
            ));
        }

        // skip argument if the argument is optional and no match has been added
        if (optional && matchCount == data.getResult().getMatches().size()) {
            data.getCurrentSegment().setValue(null);
            input.setPosition(Math.max(0, position - 1));
            super.parse(data);
        }
    }

    @Override
    public boolean isEqual(Command<C, T> other) {
        if (getClass() != other.getClass()) return false;

        ArgumentCommand<C, T> ac = (ArgumentCommand<C, T>) other;
        return
                ac.optional == optional &&
                ac.argumentId.equals(argumentId) &&
                ac.argumentParser.equals(argumentParser);
    }

}
