package dev.sweety.unibo.api.flag.impl.number;

import dev.sweety.unibo.api.flag.NumberFlag;
import org.jetbrains.annotations.NotNull;

public class FloatFlag extends NumberFlag<Float> {

    public FloatFlag(@NotNull String name, @NotNull Float value) {
        super(name, value);
    }

    @Override
    public String serialize(Float value) {
        return value.toString();
    }

    @Override
    public Float deserialize(String value) {
        return Float.parseFloat(value);
    }
}
