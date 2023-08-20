package de.bluecolored.bluecommands;

import de.bluecolored.bluecommands.parsers.ArgumentParser;

import java.util.Map;

public class ArgumentCommand<C, T> extends Command<C, T> {

    private final String argumentId;
    private final ArgumentParser<C, ?> argumentParser;

    public ArgumentCommand(String argumentId, ArgumentParser<C, ?> argumentParser) {
        this.argumentId = argumentId;
        this.argumentParser = argumentParser;
    }

    @Override
    public void parse(C context, Map<String, Object> arguments, InputReader input, ParseResult<C, T> result) {
        if (!isValid(context)) return;

        int position = input.getPosition();

        try {
            Object argument = argumentParser.parse(context, input);

            // check if full token was consumed
            int next = input.peek();
            if (next != -1 && next != ' ') {
                throw new CommandParseException("ArgumentParser did not consume the full token.");
            }

            arguments.put(argumentId, argument);
            super.parse(context, arguments, input, result);
        } catch (CommandParseException ex) {
            input.setPosition(position);
            result.getFailures().add(new ParseResult.ParseFailure(
                position,
                ex.getMessage(),
                argumentParser.suggest(context, arguments, input)
            ));
        }
    }

    @Override
    public boolean isEqual(Command<C, T> other) {
        if (getClass() != other.getClass()) return false;

        ArgumentCommand<C, T> ac = (ArgumentCommand<C, T>) other;
        return ac.argumentId.equals(argumentId) && ac.argumentParser.equals(argumentParser);
    }

}
