package dev.sweety.unibo.api.serializable;

import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class NamedSerializable extends ObjSerializable {

    protected final String name;

    public NamedSerializable(String name) {
        this.name = name;
    }

    public NamedSerializable(Map<String, Object> me) {
        this.name = String.valueOf(me.get("name"));
    }

    @Override
    public void serialize(@NotNull Map<String, Object> me) {
        me.put("name", name);
    }

    @Override
    public String id() {
        return name;
    }

}