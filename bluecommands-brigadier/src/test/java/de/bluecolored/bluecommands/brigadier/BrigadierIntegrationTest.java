package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.tree.RootCommandNode;
import de.bluecolored.bluecommands.BlueCommands;
import de.bluecolored.bluecommands.InputReader;
import de.bluecolored.bluecommands.Suggestion;
import de.bluecolored.bluecommands.annotations.*;
import de.bluecolored.bluecommands.parsers.StringArgumentParser;
import org.junit.jupiter.api.Test;
import org.junit.platform.commons.util.ToStringBuilder;

import java.util.List;
import java.util.Map;

public class BrigadierIntegrationTest {

    @Test
    public void test() throws CommandSyntaxException {
        BlueCommands<Object> blueCommands = new BlueCommands<>();
        blueCommands.setArgumentParserForId("word", StringArgumentParser.word());
        var command = blueCommands.createCommand(this);

        CommandDispatcher<Object> dispatcher = new CommandDispatcher<>();
        RootCommandNode<Object> rootNode = dispatcher.getRoot();
        BrigadierIntegration.createCommandNodes(command).forEach(rootNode::addChild);

        var suggestions = dispatcher.getCompletionSuggestions(dispatcher.parse("test Blue ", null));
        int result = dispatcher.execute("test Blue abcd 1233", null);
    }

    @Command("test <name> [number] [id] <ranged-number>")
    public int command(
            @Argument("name") @Parser("word") String name,
            @Argument("number") Double number,
            @Argument("id") @ParserType(TestArgument.class) String id,
            @Argument("ranged-number") @Range(min = 0) long rangedNumber
    ) {
        System.out.println(new ToStringBuilder("Result")
                .append("name", name)
                .append("number", number)
                .append("id", id)
                .append("rangedNumber", rangedNumber)
                .toString());

        return 23;
    }

    static class TestArgument extends StringArgumentParser<Object, String> {

        public TestArgument() {
            super(s -> s, false, false);
        }

        @Override
        public List<Suggestion> suggest(Object context, Map<String, Object> arguments, InputReader input) {
            return List.of(() -> "test-suggestion");
        }

    }

}
