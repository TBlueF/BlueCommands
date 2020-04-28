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
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import de.bluecolored.bluecommands.exceptions.CommandArgumentException;
import de.bluecolored.bluecommands.exceptions.CommandFormatException;
import de.bluecolored.bluecommands.exceptions.CommandNotFoundException;
import de.bluecolored.bluecommands.exceptions.InsufficientContextException;
import de.bluecolored.bluecommands.exceptions.InsufficientPermissionException;

public class CommandHandler<C extends CommandContext> {

	private ArgumentParserLibrary<? super C> parserLib;
	private Map<String, Collection<CommandRegistration<C>>> commands;
	
	public CommandHandler() {
		this(new ArgumentParserLibrary<>());
	}
	
	public CommandHandler(ArgumentParserLibrary<? super C> parserLib) {
		this.parserLib = parserLib;
		this.commands = new HashMap<>();
	}
	
	/**
	 * 
	 * @param context
	 * @param commandString
	 * @return
	 * @throws InvocationTargetException If the command-method throws an exception
	 * @throws CommandArgumentException If there is an invalid argument that could not be parsed
	 * @throws InsufficientContextException If there was a method parameter that could not be resolved in the given context
	 * @throws InsufficientPermissionException If the context has not the permission to execute the command
	 * @throws CommandNotFoundException If there was no such command registered
	 */
	public synchronized Object execute(C context, String commandString) throws InvocationTargetException, CommandFormatException {
		int splitPoint = commandString.length();
		
		CommandFormatException commandException = null;
		while (splitPoint > 0) {
			String command = commandString.substring(0, splitPoint);
			String arguments = splitPoint < commandString.length() ? commandString.substring(splitPoint + 1) : "";

			for (CommandRegistration<C> reg : getCommands(command)) {
				try {
					return reg.execute(parserLib, context, arguments);
				} catch (CommandFormatException e) {
					if (commandException == null) {
						commandException = e;
					} else {
						commandException.addSuppressed(e);
					}
				}
			}
			
			splitPoint = commandString.lastIndexOf(' ', splitPoint - 1);
		}
		
		if (commandException == null) {
			commandException = new CommandNotFoundException();
		}
		
		throw commandException;
	}
	
	public synchronized Collection<String> suggest(C context, String commandString) {
		String suggestString;
		String startingWith;
		if (commandString.endsWith(" ")) {
			suggestString = commandString.substring(0, commandString.length() - 1);
			startingWith = "";
		} else {
			int splitPoint = commandString.lastIndexOf(' ');
			if (splitPoint >= 0) {
				suggestString = commandString.substring(0, splitPoint);
				startingWith = commandString.substring(splitPoint + 1);
			} else {
				suggestString = "";
				startingWith = commandString;
			}
		}
		
		Set<String> suggestions = new HashSet<>(); 
		
		int splitPoint = suggestString.length();
		boolean commandFound = false;
		while (splitPoint > 0 && !commandFound) {
			String command = suggestString.substring(0, splitPoint);
			String arguments = splitPoint < suggestString.length() ? suggestString.substring(splitPoint + 1) : "";

			for (CommandRegistration<C> reg : getCommands(command)) {
				suggestions.addAll(reg.suggest(parserLib, context, arguments));
				commandFound = true;
			}
			
			splitPoint = suggestString.lastIndexOf(' ', splitPoint - 1);
		}
		
		suggestions.addAll(getDirectSubcommands(suggestString));
		
		final String prefix = startingWith;
		suggestions.removeIf(sug -> !sug.startsWith(prefix));

		return suggestions;
	}
	
	public Collection<CommandRegistration<C>> getCommands(String label){
		Collection<CommandRegistration<C>> coll = commands.get(label);
		if (coll == null) return Collections.emptyList();
		return Collections.unmodifiableCollection(coll);
	}
	
	public synchronized void register(Object commandHolder) {
		for (CommandRegistration<C> reg : CommandRegistration.<C>create(commandHolder)) {
			for (String label : reg.getLabels()) {
				Collection<CommandRegistration<C>> col = commands.get(label);
				
				if (col == null) {
					col = new ArrayList<>();
					commands.put(label, col);
				}
				
				col.add(reg);
			}
		}
	}
	
	public synchronized Collection<String> getDirectSubcommands(String command) {
		Collection<String> subCommands = new HashSet<>();
		for (String subCommand : commands.keySet()) {
			if (command.isEmpty()){
				subCommand = subCommand.split(" ", 2)[0];
				subCommands.add(subCommand);
			} else if (subCommand.startsWith(command + " ")) {
				subCommand = subCommand.substring(command.length() + 1);
				subCommand = subCommand.split(" ", 2)[0];
				subCommands.add(subCommand);
			}
		}
		return subCommands;
	}
	
	public ArgumentParserLibrary<? super C> getArgumentParserLib() {
		return parserLib;
	}
	
}
