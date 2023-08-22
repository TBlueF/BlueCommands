package de.bluecolored.bluecommands;

public class CommandSetupException extends RuntimeException {

    public CommandSetupException(String message) {
        super(message);
    }

    public CommandSetupException(String message, Throwable cause) {
        super(message, cause);
    }

}
