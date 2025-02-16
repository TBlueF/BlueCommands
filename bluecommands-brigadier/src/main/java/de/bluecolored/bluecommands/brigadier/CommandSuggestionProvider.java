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
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.bluecolored.bluecommands.*;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;

class CommandSuggestionProvider<D> implements SuggestionProvider<D> {

    private final ParseFunction<D> command;

    public <C> CommandSuggestionProvider(Command<C, ?> command, Function<D, C> conversion) {
        this.command = (context, input) -> command.parse(conversion.apply(context), input);
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<D> context, SuggestionsBuilder suggestionsBuilder) {
        return CompletableFuture.supplyAsync(() -> {
            InputReader inputReader = new InputReader(context.getInput());
            inputReader.setPosition(context.getRange().getStart());
            ParseResult<?, ?> result = command.parse(context.getSource(), inputReader);

            // only get suggestions for last word
            int start = suggestionsBuilder.getInput().lastIndexOf(' ') + 1;
            SuggestionsBuilder builder = suggestionsBuilder.createOffset(start);

            Map<String, Suggestion> suggestionMap = new HashMap<>();
            for (ParseFailure<?, ?> failure : result.getFailures()) {
                if (failure.getPosition() != builder.getStart()) continue;
                for (var suggestion : failure.getSuggestions()) {
                    suggestionMap.put(suggestion.getString(), suggestion);
                }
            }

            for (var suggestion : suggestionMap.values()) {
                if (suggestion instanceof TooltipSuggestion) {
                    builder.suggest(suggestion.getString(), ((TooltipSuggestion) suggestion).getTooltip());
                } else {
                    builder.suggest(suggestion.getString());
                }
            }

            return builder.build();
        });
    }

    interface ParseFunction<C> {
        ParseResult<?, ?> parse(C context, InputReader input);
    }

}
