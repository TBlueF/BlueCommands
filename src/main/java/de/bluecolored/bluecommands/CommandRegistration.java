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

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Collection;

import de.bluecolored.bluecommands.ArgumentParser.ParseResult;
import de.bluecolored.bluecommands.exceptions.CommandArgumentException;
import de.bluecolored.bluecommands.exceptions.InsufficientContextException;
import de.bluecolored.bluecommands.exceptions.InsufficientPermissionException;
import de.bluecolored.bluecommands.exceptions.UnknownParserException;

public class CommandRegistration<C extends CommandContext> {

	private Object holder;
	private Method method;
	private Command command;
	
	private CommandRegistration(Object holder, Method method, Command command) { 
		this.holder = holder;
		this.method = method;
		this.command = command;
	}
	
	public Object execute(ArgumentParserLibrary<? super C> parserLib, C context, String argumentsString) throws CommandArgumentException, InvocationTargetException, InsufficientContextException, InsufficientPermissionException {
		if (!context.hasPermission(command.permission())) throw new InsufficientPermissionException(context, command.permission());
		
		Parameter[] parameters = method.getParameters();
		Object[] resolvedParameters = new Object[parameters.length];
		
		String remainingArgumentsString = argumentsString;
		for (int i = 0; i < parameters.length; i++) {
			Argument argument = parameters[i].getAnnotation(Argument.class);
			if (argument != null) {
				ArgumentParser<? super C> parser = parserLib.getArgumentParser(argument.type());
				if (parser == null) throw new UnknownParserException(argument.type());
				
				ParseResult result = parser.parse(context, parameters[i].getType(), remainingArgumentsString);
				resolvedParameters[i] = result.getResult();
				remainingArgumentsString = remainingArgumentsString.substring(result.getConsumedChars());
			} else {
				resolvedParameters[i] = context.resolveFromContext(parameters[i].getType());
			}
		}
		
		if (!remainingArgumentsString.isEmpty()) throw new CommandArgumentException("Too many arguments!", remainingArgumentsString, null, null);
		
		try {
			return method.invoke(holder, resolvedParameters);
		} catch (IllegalAccessException | IllegalArgumentException e) {
			throw new RuntimeException(e); //should never happen
		} 
	}
	
	public Collection<String> suggest(ArgumentParserLibrary<? super C> parserLib, C context, String argumentsString) {
		Collection<String> suggestions = new ArrayList<>();

		Parameter[] parameters = method.getParameters();
		String remainingArgumentsString = argumentsString;
		for (int i = 0; i < parameters.length; i++) {
			Argument argument = parameters[i].getAnnotation(Argument.class);
			if (argument != null) {
				Class<?> argumentType = parameters[i].getType();
				if (!argument.type().equals(Object.class)) argumentType = argument.type();
				
				ArgumentParser<? super C> parser = parserLib.getArgumentParser(argumentType);
				if (parser == null) throw new UnknownParserException(argumentType);

				try {
					ParseResult result = parser.parse(context, parameters[i].getType(), remainingArgumentsString);
					remainingArgumentsString = remainingArgumentsString.substring(result.getConsumedChars());
				} catch (CommandArgumentException e) {
					suggestions.addAll(parser.suggest(context, parameters[i].getType(), remainingArgumentsString));
					break;
				}
			} else {
				try {
					context.resolveFromContext(parameters[i].getType());
				} catch (InsufficientContextException e) {
					break;
				}
			}
		}
		
		return suggestions;
	}
	
	public String getDescription() {
		return command.description();
	}
	
	public String getPermission() {
		return command.permission();
	}
	
	public String getLabel() {
		return command.label()[0];
	}
	
	public String[] getLabels() {
		return command.label();
	}
	
	public static <C extends CommandContext> Collection<CommandRegistration<C>> create(Object holder){
		Collection<CommandRegistration<C>> commands = new ArrayList<>();
		
		for (Method method : holder.getClass().getDeclaredMethods()) {
			Command command = method.getAnnotation(Command.class);
			if (command == null) continue;
			if (!Modifier.isPublic(method.getModifiers())) continue;
			
			commands.add(new CommandRegistration<C>(holder, method, command));
		}
		
		return commands;
	}
	
}
