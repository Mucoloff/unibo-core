package dev.sweety.unibo.api.flag.impl.number;

import dev.sweety.unibo.api.flag.NumberFlag;
import org.jetbrains.annotations.NotNull;

public class IntFlag extends NumberFlag<Integer> {

    public IntFlag(@NotNull String name, @NotNull Integer value) {
        super(name, value);
    }

    @Override
    public String serialize(Integer value) {
        return value.toString();
    }

    @Override
    public Integer deserialize(String value) {
        return Integer.parseInt(value);
    }
}
