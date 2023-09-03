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

import de.bluecolored.bluecommands.annotations.Argument;
import de.bluecolored.bluecommands.annotations.Priority;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.function.Function;

public class MethodCommandExecutable<C> implements CommandExecutable<C, Object> {

    private final Method method;
    private final Object holder;
    private final int priority;

    private final BlueCommands<C> blueCommands;

    public MethodCommandExecutable(Method method, Object holder, BlueCommands<C> blueCommands) {
        this.method = method;
        this.holder = holder;
        this.blueCommands = blueCommands;

        this.method.setAccessible(true);

        Priority priority = method.getAnnotation(Priority.class);
        this.priority = priority != null ? priority.value() : 0;
    }

    @Override
    public Object execute(C context, Map<String, Object> arguments) {
        Parameter[] parameters = method.getParameters();
        Object[] parameterValues = new Object[parameters.length];
        for (int i = 0; i < parameters.length; i++){
            Parameter parameter = parameters[i];

            Argument argument = parameter.getAnnotation(Argument.class);
            if (argument != null) {
                parameterValues[i] = arguments.get(argument.value());
                continue;
            }

            // supply by context
            Function<C, ?> contextResolver = blueCommands.getContextResolver(parameter.getType());
            if (contextResolver != null) {
                parameterValues[i] = contextResolver.apply(context);
                continue;
            }

            // supply context itself
            if (parameter.getType().isInstance(context)){
                parameterValues[i] = context;
                continue;
            }

            // still here? error!
            throw new CommandSetupException("Failed to resolve parameter for command execution.\nMethod: " + method + "\nParameter: " + parameter);
        }

        try {
            return method.invoke(holder, parameterValues);
        } catch (IllegalAccessException | InvocationTargetException e) {
            throw new CommandSetupException("Failed to invoke method for command execution!\nMethod:" + method, e);
        }
    }

    @Override
    public int getPriority() {
        return priority;
    }

    @Override
    public boolean isValid(C context) {
        return blueCommands.checkContext(context, method);
    }
}
