package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.Suggestion;

import java.util.List;
import java.util.Map;

public interface ArgumentParser<C, T> {

    T parse(C context, InputReader input) throws CommandParseException;

    List<Suggestion> suggest(C context, Map<String, Object> arguments, InputReader input);

    default List<Suggestion> suggest(C context, Map<String, Object> arguments) {
        return suggest(context, arguments, new InputReader(""));
    }

}
