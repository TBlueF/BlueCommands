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

import java.util.Collection;

import de.bluecolored.bluecommands.exceptions.CommandArgumentException;

public interface ArgumentParser<C extends CommandContext> {
	
	/**
	 * 
	 * @param <T>
	 * @param context
	 * @param type
	 * @param argumentsString
	 * @return
	 * @throws CommandArgumentException If the argumentsString is in the wrong format and can not be resolved
	 * @throws IllegalArgumentException If the type is not supported by this ArgumentParser
	 */
	<T> ParseResult parse(C context, Class<T> type, String argumentsString) throws CommandArgumentException, IllegalArgumentException;
	
	Collection<String> suggest(C context, Class<?> type, String argumentsString);
	
	static class ParseResult {
		private Object result;
		private int consumedChars;
		
		public ParseResult(Object result, int consumedChars) {
			this.result = result;
			this.consumedChars = consumedChars;
		}
		
		public Object getResult() {
			return result;
		}
		
		public int getConsumedChars() {
			return consumedChars;
		}
	}
	
}
