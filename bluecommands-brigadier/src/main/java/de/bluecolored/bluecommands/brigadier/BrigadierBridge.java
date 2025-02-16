/*
 * This file is part of BlueCommands, licensed under the MIT License (MIT).
 *
 * Copyright (c) Blue (Lukas Rieger) <https://bluecolored.de>
 * Copyright (c) contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package de.bluecolored.bluecommands.brigadier;

import com.mojang.brigadier.arguments.*;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.CommandNode;
import de.bluecolored.bluecommands.ArgumentCommand;
import de.bluecolored.bluecommands.Command;
import de.bluecolored.bluecommands.LiteralCommand;
import de.bluecolored.bluecommands.parsers.NumberArgumentParser;
import de.bluecolored.bluecommands.parsers.SimpleArgumentParser;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public final class BrigadierBridge {

    private BrigadierBridge() {}

    public static <C, T> Collection<CommandNode<C>> createCommandNodes(Command<C, T> command) {
        return createCommandNodes(command, new DefaultExecutionHandler<>());
    }

    public static <C, T> Collection<CommandNode<C>> createCommandNodes(
            Command<C, T> command,
            CommandExecutionHandler<C, T> executionHandler
    ) {
        return createCommandNodes(command, executionHandler, c -> c);
    }

    public static <C, D, T> Collection<CommandNode<D>> createCommandNodes(
            Command<C, T> command,
            CommandExecutionHandler<C, T> executionHandler,
            Function<D, C> contextConverter
    ) {
        return createCommandNodes(
                List.of(command),
                new CommandSuggestionProvider<>(command, contextConverter),
                new CommandCommand<>(command, executionHandler, contextConverter),
                contextConverter
        );
    }

    private static <C, D, T> Collection<CommandNode<D>> createCommandNodes(
            Collection<Command<C, T>> commands,
            com.mojang.brigadier.suggestion.SuggestionProvider<D> suggestionProvider,
            com.mojang.brigadier.Command<D> executor,
            Function<D, C> contextConverter
    ) {
        Set<Command<C, T>> nodes = new HashSet<>();
        for (var command : commands)
            collectNodes(command, nodes);

        Collection<CommandNode<D>> commandNodes = new ArrayList<>(1);

        // literals
        for (Command<C, T> node : nodes) {
            if (!(node instanceof LiteralCommand)) continue;

            LiteralArgumentBuilder<D> builder = LiteralArgumentBuilder.literal(((LiteralCommand<C, T>) node).getLiteral());
            builder.requires(d -> node.isValid(contextConverter.apply(d)));
            if (node.getExecutable() != null) builder.executes(executor);
            createCommandNodes(node.getSubCommands(), suggestionProvider, executor, contextConverter).forEach(builder::then);

            commandNodes.add(builder.build());
        }

        // group arguments by brigadier-type
        // using an enum-map here sorts the arguments by their type as well (enum-ordinal) -> this is important
        EnumMap<CommonNodeType, Set<ArgumentCommand<C, T>>> typedArguments = nodes.stream()
                .filter(c -> c instanceof ArgumentCommand)
                .map(c -> (ArgumentCommand<C, T>) c)
                .collect(Collectors.groupingBy(
                        CommonNodeType::getFor,
                        () -> new EnumMap<>(CommonNodeType.class),
                        Collectors.toSet()
                ));

        // group string-type arguments together further
        if (typedArguments.containsKey(CommonNodeType.WORD) && typedArguments.containsKey(CommonNodeType.STRING))
            typedArguments.get(CommonNodeType.STRING).addAll(typedArguments.remove(CommonNodeType.WORD));
        if (typedArguments.containsKey(CommonNodeType.STRING) && typedArguments.containsKey(CommonNodeType.GREEDY))
            typedArguments.get(CommonNodeType.GREEDY).addAll(typedArguments.remove(CommonNodeType.STRING));

        typedArguments.forEach((type, arguments) -> {

                    RequiredArgumentBuilder<D, ?> builder = RequiredArgumentBuilder.argument(
                            getCommonArgumentId(type, arguments),
                            type.getArgumentType()
                    );

                    // only allow suggestions for string-types
                    if (type == CommonNodeType.GREEDY || type == CommonNodeType.STRING || type == CommonNodeType.WORD)
                        builder.suggests(suggestionProvider);

                    builder.requires(d -> {
                        C context = contextConverter.apply(d);
                        return arguments.stream().anyMatch(arg -> arg.isValid(context));
                    });

                    if (arguments.stream().map(Command::getExecutable).anyMatch(Objects::nonNull))
                        builder.executes(executor);

                    if (type != CommonNodeType.GREEDY) {
                        Collection<Command<C, T>> subCommands = arguments.stream()
                                .flatMap(c -> c.getSubCommands().stream())
                                .collect(Collectors.toSet());
                        createCommandNodes(subCommands, suggestionProvider, executor, contextConverter).forEach(builder::then);
                    }

                    commandNodes.add(builder.build());

                });

        return commandNodes;
    }

    private static <C, T> void collectNodes(Command<C, T> command, Set<Command<C, T>> nodes) {

        // skip if not literal or argument
        if (!(command instanceof LiteralCommand || command instanceof ArgumentCommand)) {
            for (var subCommand : command.getSubCommands()) {
                collectNodes(subCommand, nodes);
            }
            return;
        }

        // add this command
        nodes.add(command);

        // handle optional commands
        if (command.isOptional()) {
            for (var subCommand : command.getSubCommands()) {
                collectNodes(subCommand, nodes);
            }
        }

    }

    private static <C, T> String getCommonArgumentId(CommonNodeType type, Collection<ArgumentCommand<C, T>> arguments) {
        String commonNodeName;
        if (arguments.size() <= 3) {
            commonNodeName = type == CommonNodeType.GREEDY ? "..." : arguments.stream()
                    .map(ArgumentCommand::getArgumentId)
                    .distinct()
                    .sorted()
                    .collect(Collectors.joining("|"));
        } else {
            commonNodeName = "arg-" + UUID.randomUUID().toString().substring(0, 8);
        }
        return commonNodeName;
    }

    private enum CommonNodeType {

        INTEGER (LongArgumentType.longArg()),
        NUMBER (DoubleArgumentType.doubleArg()),
        WORD (StringArgumentType.word()),
        STRING (StringArgumentType.string()),
        GREEDY (StringArgumentType.greedyString());

        private final ArgumentType<?> argumentType;

        CommonNodeType (ArgumentType<?> argumentType) {
            this.argumentType = argumentType;
        }

        public ArgumentType<?> getArgumentType() {
            return argumentType;
        }

        private static CommonNodeType getFor(ArgumentCommand<?, ?> argumentCommand) {
            var parser = argumentCommand.getArgumentParser();

            if (parser instanceof NumberArgumentParser) {
                Class<?> numberType = ((NumberArgumentParser<?, ?>) parser).getType();
                if (numberType == Float.class || numberType == Double.class) return NUMBER;
                return INTEGER;
            }

            if (parser instanceof SimpleArgumentParser) {
                var sParser = (SimpleArgumentParser<?, ?>) parser;
                if (sParser.isGreedy()) return GREEDY;
                if (sParser.isAllowQuoted()) return STRING;
                return WORD;
            }

            return GREEDY;
        }
    }

}
