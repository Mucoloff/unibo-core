package dev.sweety.unibo.feature.grave;

import lombok.Getter;
import lombok.Setter;
import org.bukkit.Color;
import org.bukkit.inventory.meta.components.CustomModelDataComponent;
import org.jspecify.annotations.NonNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class SimpleCustomModelDataComponent implements CustomModelDataComponent {
    private List<Float> floats = new ArrayList<>();
    private List<Boolean> flags = new ArrayList<>();
    private List<String> strings = new ArrayList<>();
    private List<Color> colors = new ArrayList<>();

    @Override
    public @NonNull Map<String, Object> serialize() {
        return Map.of(
            "floats", floats,
            "flags", flags,
            "strings", strings,
            "colors", colors
        );
    }
}
