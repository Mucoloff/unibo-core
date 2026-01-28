package dev.sweety.unibo.api.flag;

import org.jetbrains.annotations.NotNull;

public abstract class NumberFlag<T extends Number> extends Flag<T> {

    public NumberFlag(@NotNull String name, @NotNull T value) {
        super(name, value);
    }

}
