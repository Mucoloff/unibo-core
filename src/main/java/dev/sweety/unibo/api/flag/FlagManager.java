package dev.sweety.unibo.api.flag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class FlagManager {

    private final Map<String, Flag<?>> flags = new HashMap<>();

    public Flag<?> get(String name) {
        return this.flags.get(name);
    }

    public void add(Flag<?> flag) {
        this.flags.put(flag.getName(), flag);
    }

    public void remove(Flag<?> flag) {
        this.flags.remove(flag.getName());
    }

    public List<Flag<?>> getFlags() {
        return new ArrayList<>(this.flags.values());
    }

    public List<String> getNames() {
        return new ArrayList<>(this.flags.keySet());
    }
}
