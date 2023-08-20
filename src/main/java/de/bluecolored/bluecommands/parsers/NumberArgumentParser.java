package de.bluecolored.bluecommands.parsers;

import de.bluecolored.bluecommands.CommandParseException;
import de.bluecolored.bluecommands.InputReader;

import java.util.function.Function;

public class NumberArgumentParser<C, T extends Number> extends StringArgumentParser<C, T> {

    private final Class<T> type;
    private final StringParser<T> numberParser;
    private final double min, max;

    private NumberArgumentParser(Class<T> type, StringParser<T> numberParser) {
        super(numberParser,false, false);
        this.type = type;
        this.numberParser = numberParser;
        this.min = Double.NEGATIVE_INFINITY;
        this.max = Double.POSITIVE_INFINITY;
    }

    private NumberArgumentParser(Class<T> type, StringParser<T> numberParser, double min, double max) {
        super(numberParser,false, false);
        this.type = type;
        this.numberParser = numberParser;
        this.min = min;
        this.max = max;
    }

    @Override
    public T parse(C context, InputReader input) throws CommandParseException {
        T result = super.parse(context, input);
        if (result.doubleValue() < min) throw new CommandParseException(result + " is too small. It has to be greater or equal to " + min);
        if (result.doubleValue() > max) throw new CommandParseException(result + " is too big. It has to be smaller or equal to " + max);
        return result;
    }

    public NumberArgumentParser<C, T> withBounds(double min, double max) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public NumberArgumentParser<C, T> withMin(double min) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public NumberArgumentParser<C, T> withMax(double max) {
        return new NumberArgumentParser<>(type, numberParser, min, max);
    }

    public static <C> NumberArgumentParser<C, Byte> forBytes() {
        return createWrapped(Byte.class, Byte::parseByte);
    }

    public static <C> NumberArgumentParser<C, Short> forShorts() {
        return createWrapped(Short.class, Short::parseShort);
    }

    public static <C> NumberArgumentParser<C, Integer> forIntegers() {
        return createWrapped(Integer.class, Integer::parseInt);
    }

    public static <C> NumberArgumentParser<C, Long> forLongs() {
        return createWrapped(Long.class, Long::parseLong);
    }

    public static <C> NumberArgumentParser<C, Float> forFloats() {
        return createWrapped(Float.class, Float::parseFloat);
    }

    public static <C> NumberArgumentParser<C, Double> forDoubles() {
        return createWrapped(Double.class, Double::parseDouble);
    }

    private static <C, T extends Number> NumberArgumentParser<C, T> createWrapped(Class<T> type, Function<String, T> numberParser) {
        return new NumberArgumentParser<>(type, wrap(numberParser, type));
    }

    private static <T> StringParser<T> wrap(Function<String, T> numberParser, Class<T> type) {
        return s -> {
            try {
                return numberParser.apply(s);
            } catch (RuntimeException ex) {
                throw new CommandParseException("'" + s +"' is not a vaild " + type);
            }
        };
    }

}
