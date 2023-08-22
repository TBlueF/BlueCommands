package de.bluecolored.bluecommands;

import java.util.Map;

public interface CommandExecutable<C, T> {
    
    T execute(C context, Map<String, Object> arguments);

    default int getPriority() {
        return 0;
    }

    default boolean isValid(C context) {
        return true;
    }

}
