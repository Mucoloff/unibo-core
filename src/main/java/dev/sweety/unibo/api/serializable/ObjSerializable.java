package dev.sweety.unibo.api.serializable;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
public abstract class ObjSerializable implements ConfigurationSerializable {

    @Override
    public @NotNull Map<String, Object> serialize() {
        final HashMap<String, Object> me = new HashMap<>();
        serialize(me);
        return me;
    }

    public boolean id(String id) {
        return this.id().equals(id);
    }

    public abstract void serialize(@NotNull Map<String, Object> me);

    public abstract String id();

}