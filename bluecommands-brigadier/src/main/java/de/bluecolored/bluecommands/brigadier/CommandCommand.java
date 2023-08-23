package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandExceptionType;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import de.bluecolored.bluecommands.Command;
import de.bluecolored.bluecommands.ParseFailure;
import de.bluecolored.bluecommands.ParseMatch;
import de.bluecolored.bluecommands.ParseResult;

import java.util.Comparator;

public class CommandCommand<C> implements com.mojang.brigadier.Command<C> {

    private final Command<C, ?> command;

    public CommandCommand(Command<C, ?> command) {
        this.command = command;
    }

    @Override
    public int run(CommandContext<C> context) throws CommandSyntaxException {
        ParseResult<C, ?> result = command.parse(context.getSource(), context.getInput());
        if (result.getMatches().isEmpty()) {
            ParseFailure<C, ?> failure = result.getFailures().stream()
                    .max(Comparator.comparing(ParseFailure::getPosition))
                    .orElseThrow(IllegalAccessError::new);
            throw new CommandSyntaxException(
                    new SimpleCommandExceptionType(failure::getReason),
                    failure::getReason,
                    context.getInput(),
                    failure.getPosition()
            );
        }

        ParseMatch<C, ?> executable = result.getMatches().stream()
                .max(Comparator.comparing(ParseMatch::getPriority))
                .orElseThrow(IllegalStateException::new);

        Object executionResult = executable.execute();
        if (executionResult instanceof Number)
            return ((Number) executionResult).intValue();

        return 1;
    }

}
