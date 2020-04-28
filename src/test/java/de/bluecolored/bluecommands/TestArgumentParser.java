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

import java.util.ArrayList;
import java.util.Collection;

import de.bluecolored.bluecommands.exceptions.CommandArgumentException;

public class TestArgumentParser implements ArgumentParser<CommandContext> {

	@Override
	public <T> ParseResult parse(CommandContext context, Class<T> type, String argumentsString) throws CommandArgumentException {
		if (!type.isAssignableFrom(String.class)) throw new IllegalArgumentException("Unsupported type: " + type.getCanonicalName());
		
		if (argumentsString.isEmpty()) throw new CommandArgumentException("Not enough arguments!", argumentsString, type, this);
		
		String result;
		int consumed;
		int nextSpace = argumentsString.indexOf(' ');
		if (nextSpace < 0) {
			result = argumentsString;
			consumed = argumentsString.length();
		} else {
			result = argumentsString.substring(0, nextSpace);
			consumed = nextSpace + 1;
		}
		
		if (!result.startsWith("valid")) throw new CommandArgumentException("String has to start with 'valid'!", result, type, this);
		
		return new ParseResult(result, consumed);
	}

	@Override
	public Collection<String> suggest(CommandContext context, Class<?> type, String argumentsString) {
		Collection<String> suggestions = new ArrayList<>();
		suggestions.add("validArg");
		suggestions.add("validArgument");
		suggestions.add("validSomething");
		return suggestions;
	}

}
