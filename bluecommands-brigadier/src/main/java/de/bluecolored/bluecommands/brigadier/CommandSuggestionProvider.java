package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.context.StringRange;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.suggestion.Suggestion;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.suggestion.Suggestions;
import com.mojang.brigadier.suggestion.SuggestionsBuilder;
import de.bluecolored.bluecommands.Command;
import de.bluecolored.bluecommands.ParseFailure;
import de.bluecolored.bluecommands.ParseResult;

import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.CompletableFuture;

public class CommandSuggestionProvider<C> implements SuggestionProvider<C> {

    private final Command<C, ?> command;

    public CommandSuggestionProvider(Command<C, ?> command) {
        this.command = command;
    }

    @Override
    public CompletableFuture<Suggestions> getSuggestions(CommandContext<C> context, SuggestionsBuilder builder) throws CommandSyntaxException {
        return CompletableFuture.supplyAsync(() -> {
            Collection<Suggestion> suggestions = new ArrayList<>();
            ParseResult<C, ?> result = command.parse(context.getSource(), builder.getInput());
            for (ParseFailure<C, ?> failure : result.getFailures()) {
                failure.getSuggestions().stream()
                        .map(de.bluecolored.bluecommands.Suggestion::getString)
                        .map(text -> new Suggestion(StringRange.between(failure.getPosition(), builder.getInput().length()), text))
                        .forEach(suggestions::add);
            }
            return Suggestions.create(builder.getInput(), suggestions);
        });
    }

}
