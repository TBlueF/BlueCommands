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
package de.bluecolored.bluecommands;

import de.bluecolored.bluecommands.annotations.*;
import de.bluecolored.bluecommands.parsers.*;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Annotation;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiPredicate;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.regex.Matcher;

public class BlueCommands<C> {
    private static final java.util.regex.Pattern ARGUMENT_PATTERN = java.util.regex.Pattern.compile("^<(.*)>$");
    private static final java.util.regex.Pattern OPTIONAL_ARGUMENT_PATTERN = java.util.regex.Pattern.compile("^\\[(.*)]$");

    private final Map<String, ArgumentParser<C, ?>> argumentParsersById;
    private final Map<Class<? extends ArgumentParser<C, ?>>, ArgumentParser<C, ?>> argumentParsersByType;
    private final Map<Class<?>, ArgumentParser<C, ?>> argumentParsersByArgumentType;
    private final Map<Class<?>, Function<C, ?>> contextResolvers;
    private final Map<Class<? extends Annotation>, BiPredicate<? extends Annotation, C>> annotationContextPredicate;

    public BlueCommands() {
        this.argumentParsersById = new ConcurrentHashMap<>();
        this.argumentParsersByType = new ConcurrentHashMap<>();
        this.argumentParsersByArgumentType = new ConcurrentHashMap<>();
        this.contextResolvers = new ConcurrentHashMap<>();
        this.annotationContextPredicate = new ConcurrentHashMap<>();

        setArgumentParserForArgumentType(String.class, StringArgumentParser.string());
        setArgumentParserForArgumentType(byte.class, NumberArgumentParser.forBytes());
        setArgumentParserForArgumentType(Byte.class, NumberArgumentParser.forBytes());
        setArgumentParserForArgumentType(short.class, NumberArgumentParser.forShorts());
        setArgumentParserForArgumentType(Short.class, NumberArgumentParser.forShorts());
        setArgumentParserForArgumentType(int.class, NumberArgumentParser.forIntegers());
        setArgumentParserForArgumentType(Integer.class, NumberArgumentParser.forIntegers());
        setArgumentParserForArgumentType(long.class, NumberArgumentParser.forLongs());
        setArgumentParserForArgumentType(Long.class, NumberArgumentParser.forLongs());
        setArgumentParserForArgumentType(float.class, NumberArgumentParser.forFloats());
        setArgumentParserForArgumentType(Float.class, NumberArgumentParser.forFloats());
        setArgumentParserForArgumentType(double.class, NumberArgumentParser.forDoubles());
        setArgumentParserForArgumentType(Double.class, NumberArgumentParser.forDoubles());
        setArgumentParserForArgumentType(boolean.class, BooleanArgumentParser.create());
        setArgumentParserForArgumentType(Boolean.class, BooleanArgumentParser.create());
    }

    public Command<C, Object> createCommand(Object holder) {
        Command<C, Object> root = new Command<>();
        Class<?> holderClass = holder.getClass();

        String[] descriptionPrefixes = new String[]{""};
        var holderCommand = holderClass.getAnnotation(de.bluecolored.bluecommands.annotations.Command.class);
        if (holderCommand != null) {
            descriptionPrefixes = holderCommand.value();
        }

        for (String descriptionPrefix : descriptionPrefixes) {
            for (Method method : holderClass.getDeclaredMethods()){
                var command = method.getAnnotation(de.bluecolored.bluecommands.annotations.Command.class);
                if (command == null) continue;

                for (String description : command.value()) {
                    root.addSubCommand(createCommand(holder, method, descriptionPrefix + " " + description));
                }
            }
        }

        return root;
    }

    private Command<C, Object> createCommand(Object holder, Method method, String description) {
        String[] tokens = description.isBlank() ? new String[0] : description.trim().split(" +");
        try {
            return createCommand(holder, method, tokens, 0);
        } catch (CommandSetupException ex) {
            throw new CommandSetupException(ex.getMessage() + "\nMethod: " + method, ex);
        }
    }

    private Command<C, Object> createCommand(Object holder, Method method, String[] tokens, int nextToken) {
        if (nextToken >= tokens.length) {
            Command<C, Object> command = new Command<>();
            command.setExecutable(new MethodCommandExecutable<>(method, holder, this));
            return command;
        }

        String token = tokens[nextToken];
        boolean optional = false;
        Matcher argumentMatcher = ARGUMENT_PATTERN.matcher(token);
        Command<C, Object> command;

        if (!argumentMatcher.matches()) {
            argumentMatcher = OPTIONAL_ARGUMENT_PATTERN.matcher(token);
            optional = true;
        }

        if (!argumentMatcher.matches()) {
            command = new LiteralCommand<>(token);
            //optional = false;
        } else {
            String argumentId = argumentMatcher.group(1);

            Parameter parameter = null;
            for (Parameter param : method.getParameters()) {
                Argument argument = param.getAnnotation(Argument.class);
                if (argument != null && argument.value().equals(argumentId)) {
                    parameter = param;
                    break;
                }
            }
            if (parameter == null) throw new CommandSetupException(String.format(
                    "Argument '%s' is missing in method-signature.",
                    argumentId
            ));

            Parser parser = parameter.getAnnotation(Parser.class);
            ParserType parserType = parameter.getAnnotation(ParserType.class);
            ArgumentParser<C, ?> argumentParser;
            if (parser != null) {
                argumentParser = getArgumentParser(parser.value());
            } else if (parserType != null) {
                //noinspection unchecked,rawtypes
                argumentParser = getArgumentParser((Class) parserType.value());
            } else {
                argumentParser = getParserByArgumentType(parameter.getType());
            }

            if (argumentParser == null) throw new CommandSetupException(String.format(
                    "No Argument-Parser found for Argument '%s'.",
                    argumentId
            ));

            // add pattern to string arguments
            if (argumentParser.getClass() == StringArgumentParser.class) {
                Pattern pattern = parameter.getAnnotation(Pattern.class);
                if (pattern != null) {
                    //noinspection unchecked
                    StringArgumentParser<C> stringArgumentParser = (StringArgumentParser<C>) argumentParser;
                    argumentParser = stringArgumentParser.withPattern(pattern.value());
                }
            }

            // add range to number arguments
            if (argumentParser.getClass() == NumberArgumentParser.class) {
                Range range = parameter.getAnnotation(Range.class);
                if (range != null) {
                    //noinspection unchecked
                    NumberArgumentParser<C, ?> numberArgumentParser = (NumberArgumentParser<C, ?>) argumentParser;
                    argumentParser = numberArgumentParser.withBounds(range.min(), range.max());
                }
            }

            command = new ArgumentCommand<>(argumentId, argumentParser, optional);
        }

        command.addSubCommand(createCommand(holder, method, tokens, nextToken + 1));

        return command;
    }

    public void setArgumentParserForId(String id, ArgumentParser<C, ?> argumentParser) {
        argumentParsersById.put(id, argumentParser);
    }

    public <T> void setArgumentParserForArgumentType(Class<T> type, ArgumentParser<C, ? extends T> argumentParser) {
        argumentParsersByArgumentType.put(type, argumentParser);
    }

    public <T> void setContextResolverForType(Class<T> type, Function<C, T> contextResolver) {
        contextResolvers.put(type, contextResolver);
    }

    public void setContextPredicate(Predicate<C> contextPredicate) {
        this.setAnnotationContextPredicate(
                null,
                (command, context) -> contextPredicate.test(context)
        );
    }

    public <A extends Annotation> void setAnnotationContextPredicate(Class<A> annotationType, BiPredicate<@Nullable A, C> contextPredicate) {
        annotationContextPredicate.put(annotationType, contextPredicate);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable ArgumentParser<C, ? extends T> getParserByArgumentType(Class<T> argumentType) {
        return (ArgumentParser<C, ? extends T>) argumentParsersByArgumentType.get(argumentType);
    }

    @SuppressWarnings("unchecked")
    public <T> ArgumentParser<C, T> getArgumentParser(Class<? extends ArgumentParser<C, T>> parserType) {
        return (ArgumentParser<C, T>) argumentParsersByType.computeIfAbsent(parserType, type -> {
            try {
                Constructor<? extends ArgumentParser<C, ?>> constructor = type.getConstructor();
                constructor.setAccessible(true);
                return constructor.newInstance();
            } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | InvocationTargetException e) {
                throw new CommandSetupException("Failed to create an instance of: " + parserType, e);
            }
        });
    }

    public @Nullable ArgumentParser<C, ?> getArgumentParser(String id) {
        return argumentParsersById.get(id);
    }

    @SuppressWarnings("unchecked")
    public <T> @Nullable Function<C, T> getContextResolver(Class<T> argumentType) {
        return (Function<C, T>) contextResolvers.get(argumentType);
    }

    @SuppressWarnings("unchecked")
    public <A extends Annotation> @Nullable BiPredicate<@Nullable A, C> getAnnotationContextPredicate(Class<A> annotationType) {
        return (BiPredicate<A, C>) annotationContextPredicate.get(annotationType);
    }

    public boolean checkContext(C context) {
        for (var predicate : annotationContextPredicate.values()) {
            if (!predicate.test(null, context)) return false;
        }
        return true;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public boolean checkContext(C context, Method method) {
        for (var entry : annotationContextPredicate.entrySet()) {
            Class<? extends Annotation> annotationType = entry.getKey();
            Annotation annotation = annotationType != null ? method.getAnnotation(entry.getKey()) : null;
            BiPredicate predicate = entry.getValue();
            if (!predicate.test(annotation, context)) return false;
        }
        return true;
    }

}
