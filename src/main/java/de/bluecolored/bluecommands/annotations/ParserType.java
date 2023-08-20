package de.bluecolored.bluecommands.annotations;

import de.bluecolored.bluecommands.parsers.ArgumentParser;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface ParserType {

    Class<? extends ArgumentParser<?, ?>> value();

}
