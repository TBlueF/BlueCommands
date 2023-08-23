package de.bluecolored.bluecommands;

import de.bluecolored.bluecommands.annotations.Command;
import de.bluecolored.bluecommands.annotations.*;
import de.bluecolored.bluecommands.parsers.StringArgumentParser;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ToStringBuilder;

import java.util.Comparator;

public class CommandTest {

    @Test
    public void testCommand() {
        BlueCommands<Object> blueCommands = new BlueCommands<>();
        blueCommands.setArgumentParserForId("word", StringArgumentParser.word());
        var command = blueCommands.createCommand(this);

        ParseResult<Object, Object> result = command.parse(null, "test Blue abcd 1233");

        ParseMatch<Object, Object> executable = result.getMatches().stream()
                .max(Comparator.comparing(ParseMatch::getPriority))
                .orElseThrow(IllegalStateException::new);

        Object executionResult = executable.execute();
        System.out.println(executionResult);
    }

    @Command("test <name> [number] [id] <ranged-number>")
    public String command(
            @Argument("name") @Parser("word") String name,
            @Argument("number") Double number,
            @Argument("id") @Pattern("\\w{3,24}") String id,
            @Argument("ranged-number") @Range(min = 0) long rangedNumber
    ) {
        System.out.println(new ToStringBuilder("Result")
                .append("name", name)
                .append("number", number)
                .append("id", id)
                .append("rangedNumber", rangedNumber)
                .toString());

        return "YAY!";
    }

}
