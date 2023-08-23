package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.suggestion.SuggestionProvider;
import com.mojang.brigadier.tree.CommandNode;
import de.bluecolored.bluecommands.ArgumentCommand;
import de.bluecolored.bluecommands.Command;
import de.bluecolored.bluecommands.LiteralCommand;
import de.bluecolored.bluecommands.parsers.NumberArgumentParser;
import de.bluecolored.bluecommands.parsers.StringArgumentParser;

import java.util.ArrayList;
import java.util.Collection;

public class BrigadierIntegration {

    public static <C> Collection<CommandNode<C>> createCommandNodes(Command<C, ?> command) {
        return createCommandNodes(command, new CommandSuggestionProvider<>(command), new CommandCommand<>(command));
    }

    @SuppressWarnings("unchecked")
    private static <C> Collection<CommandNode<C>> createCommandNodes(Command<C, ?> command, SuggestionProvider<C> suggestionProvider, com.mojang.brigadier.Command<C> executor) {
        Collection<CommandNode<C>> commandNodes = new ArrayList<>(1);

        ArgumentBuilder<C, ?> argumentBuilder = null;
        boolean fallback = false;

        if (command instanceof LiteralCommand) {

            var literalCommand = (LiteralCommand<C, ?>) command;
            argumentBuilder = LiteralArgumentBuilder.literal(literalCommand.getLiteral());

        } else if (command instanceof ArgumentCommand) {
            RequiredArgumentBuilder<C, ?> requiredArgumentBuilder;

            var argumentCommand = (ArgumentCommand<C, ?>) command;
            var argumentParser = argumentCommand.getArgumentParser();
            if (argumentParser instanceof NumberArgumentParser) {
                requiredArgumentBuilder = createArgumentBuilder(argumentCommand.getArgumentId(), (NumberArgumentParser<C, ?>) argumentParser);
            } else if (argumentParser instanceof StringArgumentParser) {
                var stringArgumentParser = (StringArgumentParser<C, ?>) argumentParser;
                if (stringArgumentParser.isAllowQuoted()) {
                    requiredArgumentBuilder = RequiredArgumentBuilder.argument(argumentCommand.getArgumentId(), StringArgumentType.string());
                } else if (stringArgumentParser.isGreedy()) {
                    requiredArgumentBuilder = RequiredArgumentBuilder.argument(argumentCommand.getArgumentId(), StringArgumentType.greedyString());
                } else {
                    requiredArgumentBuilder = RequiredArgumentBuilder.argument(argumentCommand.getArgumentId(), StringArgumentType.word());
                }
            } else {
                // not supported by brigadier, fall back
                fallback = true;
                requiredArgumentBuilder = RequiredArgumentBuilder.argument("...", StringArgumentType.greedyString());
            }

            if (!fallback && argumentCommand.isOptional()) {
                for (Command<C, ?> subCommand : command.getSubCommands()) {
                    commandNodes.addAll(createCommandNodes(subCommand, suggestionProvider, executor));
                }
            }

            requiredArgumentBuilder.suggests(suggestionProvider);
            argumentBuilder = requiredArgumentBuilder;
        }

        if (argumentBuilder != null) {
            argumentBuilder.requires(command::isValid);

            if (command.getExecutable() != null)
                argumentBuilder.executes(executor);

            if (!fallback) {
                for (Command<C, ?> subCommand : command.getSubCommands()) {
                    createCommandNodes(subCommand, suggestionProvider, executor).forEach(argumentBuilder::then);
                }
            }

            commandNodes.add(argumentBuilder.build());
        } else {
            for (Command<C, ?> subCommand : command.getSubCommands()) {
                commandNodes.addAll(createCommandNodes(subCommand, suggestionProvider, executor));
            }
        }

        return commandNodes;
    }

    private static <C> RequiredArgumentBuilder<C, ?> createArgumentBuilder(String argumentId, NumberArgumentParser<C, ?> numberArgumentParser) {
        var type = numberArgumentParser.getType();

        if (type == Byte.class || type == Short.class || type == Integer.class) {
            return RequiredArgumentBuilder.argument(argumentId, IntegerArgumentType.integer(
                    (int) numberArgumentParser.getMin(),
                    (int) numberArgumentParser.getMax()
            ));
        }

        if (type == Long.class) {
            return RequiredArgumentBuilder.argument(argumentId, LongArgumentType.longArg(
                    (long) numberArgumentParser.getMin(),
                    (long) numberArgumentParser.getMax()
            ));
        }

        if (type == Float.class) {
            return RequiredArgumentBuilder.argument(argumentId, FloatArgumentType.floatArg(
                    (float) numberArgumentParser.getMin(),
                    (float) numberArgumentParser.getMax()
            ));
        }

        if (type == Double.class) {
            return RequiredArgumentBuilder.argument(argumentId, DoubleArgumentType.doubleArg(
                    numberArgumentParser.getMin(),
                    numberArgumentParser.getMax()
            ));
        }

        throw new UnsupportedOperationException("Unknown number-argument type: " + type);
    }

}
