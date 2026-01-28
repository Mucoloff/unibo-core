package dev.sweety.unibo.api.flag.impl.number;

import dev.sweety.unibo.api.flag.NumberFlag;
import org.jetbrains.annotations.NotNull;

public class LongFlag extends NumberFlag<Long> {

    public LongFlag(@NotNull String name, @NotNull Long value) {
        super(name, value);
    }

    @Override
    public String serialize(Long value) {
        return value.toString();
    }

    @Override
    public Long deserialize(String value) {
        return Long.parseLong(value);
    }
}
