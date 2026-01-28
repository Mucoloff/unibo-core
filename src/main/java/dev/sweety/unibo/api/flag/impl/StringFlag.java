package dev.sweety.unibo.api.flag.impl;

import dev.sweety.unibo.api.flag.Flag;
import org.jetbrains.annotations.NotNull;

public class StringFlag extends Flag<String> {

    public StringFlag(@NotNull String name, @NotNull String value) {
        super(name, value);
    }

    @Override
    public String serialize(String value) {
        return value;
    }

    @Override
    public String deserialize(String value) {
        return value;
    }
}
