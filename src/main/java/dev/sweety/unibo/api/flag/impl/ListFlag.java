package dev.sweety.unibo.api.flag.impl;

import com.google.common.reflect.TypeToken;
import dev.sweety.core.config.GsonUtils;
import dev.sweety.unibo.api.flag.Flag;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ListFlag<T> extends Flag<List<T>> {

    public ListFlag(@NotNull String name) {
        super(name, new ArrayList<>());
    }

    public ListFlag(@NotNull String name, @NotNull List<T> value) {
        super(name, value);
    }

    @Override
    public String serialize(List<T> value) {
        return GsonUtils.write(value);
    }

    @Override
    public List<T> deserialize(String value) {
        Type listType = new TypeToken<List<T>>() {
        }.getType();
        return GsonUtils.gson().fromJson(value, listType);
    }
}
