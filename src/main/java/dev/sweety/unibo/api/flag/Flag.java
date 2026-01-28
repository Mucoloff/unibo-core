package dev.sweety.unibo.api.flag;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

@Getter
@Setter
public abstract class Flag<T> {

    protected String name;
    protected T defaultValue;

    public Flag(@NotNull String name, @NotNull T defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public abstract String serialize(T value);

    public abstract T deserialize(String value);

}
