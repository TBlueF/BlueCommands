package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;

import java.util.function.Function;

public class BooleanArgumentParser<C> extends StringArgumentParser<C, Boolean> {

    public BooleanArgumentParser() {
        super(BooleanArgumentParser::parse,false, false);
    }

    private static Boolean parse(String string) throws CommandParseException {
        if (string.equals("true")) return Boolean.TRUE;
        if (string.equals("false")) return Boolean.FALSE;
        throw new CommandParseException("'" + string + "' is not a valid boolean");
    }

}
