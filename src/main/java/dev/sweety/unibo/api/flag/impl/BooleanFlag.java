package dev.sweety.unibo.api.flag.impl;

import dev.sweety.unibo.api.flag.Flag;
import org.jetbrains.annotations.NotNull;

public class BooleanFlag extends Flag<Boolean> {

    public BooleanFlag(@NotNull String name, @NotNull Boolean value) {
        super(name, value);
    }

    @Override
    public String serialize(Boolean value) {
        return value.toString();
    }

    @Override
    public Boolean deserialize(String value) {
        return Boolean.parseBoolean(value);
    }
}
