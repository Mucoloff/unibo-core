package dev.sweety.unibo.api.flag.impl;

import dev.sweety.unibo.api.flag.Flag;
import org.jetbrains.annotations.NotNull;

public class EnumFlag<E extends Enum<E>> extends Flag<E> {

    private final Class<E> enumClass;

    public EnumFlag(@NotNull String name, @NotNull E defaultValue, @NotNull Class<E> enumClass) {
        super(name, defaultValue);
        this.enumClass = enumClass;
    }

    @Override
    public String serialize(E value) {
        return String.valueOf(value.ordinal());
    }

    @Override
    public E deserialize(String value) {
        return this.enumClass.getEnumConstants()[Integer.parseInt(value)];
    }
}