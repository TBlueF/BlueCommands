package de.bluecolored.bluecommands;

import de.bluecolored.bluecommands.parsers.ArgumentParser;

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

    public boolean isOptional() {
        return optional;
    }

    @Override
    void parse(C context, InputReader input, ParseStack<C, T> stack, ParseResult<C, T> result) {
        if (!isValid(context)) return;

        int position = input.getPosition();

        try {
            Object argument = argumentParser.parse(context, input);

            // check if full token was consumed
            int next = input.peek();
            if (next != -1 && next != ' ') {
                throw new CommandParseException("ArgumentParser did not consume the full token.");
            }

            stack.getArguments().put(argumentId, argument);
            super.parse(context, input, stack, result);
        } catch (CommandParseException ex) {
            input.setPosition(position);
            result.getFailures().add(new ParseResult.ParseFailure<>(
                position,
                ex.getMessage(),
                stack.getCommandStack(),
                argumentParser.suggest(context, stack.getArguments(), input)
            ));

            if (optional) {
                input.setPosition(Math.max(0, position - 1));
                super.parse(context, input, stack, result);
            }
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
