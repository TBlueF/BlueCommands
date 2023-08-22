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
    void parse(ParseData<C, T> data) {
        C context = data.getContext();
        if (!isValid(context)) return;

        InputReader input = data.getInput();
        int position = input.getPosition();

        try {
            Object argument = argumentParser.parse(context, input);

            // sanity check position
            if (input.getPosition() < position) {
                throw new CommandParseException("The ArgumentParser '" + argumentParser + "' altered the InputReader in an illegal way. (position changed backwards)");
            }

            // check if full token was consumed
            int next = input.peek();
            if (next != -1 && next != ' ') {
                throw new CommandParseException("The ArgumentParser '" + argumentParser + "' did not consume the full token. (expected next char to be a space or end of string)");
            }

            data.getCurrentSegment().setValue(argument);
            super.parse(data);
        } catch (CommandParseException ex) {
            input.setPosition(position); // reset position for suggestions
            data.getResult().addFailure(new ParseFailure<>(
                position,
                ex.getMessage(),
                data.getCommandStack(),
                argumentParser.suggest(context, data.getArguments(), input)
            ));

            if (optional) {
                input.setPosition(Math.max(0, position - 1));
                super.parse(data);
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
