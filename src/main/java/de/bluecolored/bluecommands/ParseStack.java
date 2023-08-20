package de.bluecolored.bluecommands;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

class ParseStack<C, T> {

    private Frame top = new Frame(null);

    public Map<String, Object> getArguments() {
        return top.arguments;
    }

    public LinkedList<Command<C, T>> getCommandStack() {
        return top.stack;
    }

    public synchronized Frame push(Command<C, T> command) {
        top.push(command);
        return top;
    }

    public class Frame implements AutoCloseable {

        private final Map<String, Object> arguments;
        private final LinkedList<Command<C, T>> stack;
        private final Frame previous;

        private Frame(Frame previous) {
            this.arguments = new HashMap<>();
            this.stack = new LinkedList<>();
            this.previous = previous;
        }

        private void push(Command<C, T> command) {
            if (top != this) throw new IllegalStateException("This frame is not the top frame!");
            Frame newFrame = new Frame(this);
            newFrame.arguments.putAll(this.arguments);
            newFrame.stack.addAll(this.stack);
            newFrame.stack.addLast(command);
            top = newFrame;
        }

        @Override
        public void close() {
            if (top != this) throw new IllegalStateException("This frame is not the top frame!");
            top = previous;
        }

    }

}
