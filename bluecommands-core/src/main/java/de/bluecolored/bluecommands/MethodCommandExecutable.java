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
