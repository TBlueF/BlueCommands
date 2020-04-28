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

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

import de.bluecolored.bluecommands.exceptions.InsufficientPermissionException;

public class CommandsTest {

	private CommandHandler<TestContext> handler;
	
	@Before
	public void init() {
		handler = new CommandHandler<>();
		
		handler.getArgumentParserLib().register(TestArgumentParser.class, new TestArgumentParser());
		
		handler.register(this);
	}
	
	@Test
	public void executeValidTest() throws Exception {
		assertEquals(handler.execute(new TestContext(), "test valid validTestArgument"), 3);
	}
	
	@Test(expected = InsufficientPermissionException.class)
	public void executePermissionTest() throws Exception {
		handler.execute(new TestContext(), "test invalid permission validOtherTestArgument");
	}
	
	@Test(expected = IllegalArgumentException.class)
	public void executeTypeTest() throws Exception {
		handler.execute(new TestContext(), "test invalid argument type validTestArgumentFoo");
	}
	
	@Command(label = "test valid", permission = "test", description = "A test command that works")
	public int validTestCommand(String contextArgument1, int contextArg2, @Argument(type = TestArgumentParser.class) String someTestArgument) {
		assertEquals(someTestArgument, "validTestArgument");
		return contextArg2 + 2;
	}
	
	@Command(label = "test invalid permission", permission = "invalid", description = "A test command that the context has no permission to execute")
	public int permissionTestCommand(String contextArgument1, int contextArg2, @Argument(type = TestArgumentParser.class) String someTestArgument) {
		return contextArg2 + 2;
	}
	
	@Command(label = "test invalid argument type", permission = "test", description = "A test command that the context has no permission to execute")
	public int permissionTestCommand(String contextArgument1, int contextArg2, @Argument(type = TestArgumentParser.class) CommandContext someTestArgumentWithWrongType) {
		return contextArg2 + 2;
	}
	
}
