package dev.sweety.unibo.feature.home;

import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.feature.info.Stats;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.Map;

@Getter
@AllArgsConstructor
public class Homes {

    private Map<String, Position> homes;
    private int maxHomes;

    public boolean setHome(String name, Position position) {
        if (this.homes.size() >= maxHomes && !homes.containsKey(name)) return false;
        this.homes.put(name, position);
        return true;
    }

    public Position delHome(String name) {
        return this.homes.remove(name);
    }

    public Position home(String name) {
        return this.homes.get(name);
    }

    public void apply(final Homes copy) {
        this.homes = copy.homes;
        this.maxHomes = copy.maxHomes;
    }

}
