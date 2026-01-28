package dev.sweety.unibo.api.flag.impl.number;

import dev.sweety.unibo.api.flag.NumberFlag;
import org.jetbrains.annotations.NotNull;

public class DoubleFlag extends NumberFlag<Double> {

    public DoubleFlag(@NotNull String name, @NotNull Double value) {
        super(name, value);
    }

    @Override
    public String serialize(Double value) {
        return value.toString();
    }

    @Override
    public Double deserialize(String value) {
        try {
            return Double.parseDouble(value);
        }catch (NumberFormatException e) {
            return this.defaultValue;
        }
    }
}
