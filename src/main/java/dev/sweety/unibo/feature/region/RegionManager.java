package dev.sweety.unibo.feature.region;

import com.github.retrooper.packetevents.util.Vector3d;
import dev.sweety.core.math.MathUtils;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.serializable.SerializableManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FilenameFilter;
import java.util.HashMap;
import java.util.Map;

public class RegionManager extends SerializableManager<Region> {
    private final Map<String, DefaultRegion> defaultRegions = new HashMap<>();
    private final FilenameFilter regionFilter = (dir, name) ->
            name.endsWith(".region.yml") && !name.startsWith("default-");

    public RegionManager(final VanillaCore resource) {
        super(resource, Region.class);
    }

    @Override
    protected File[] listFiles() {
        return this.folder.listFiles(this.regionFilter);
    }

    public @NotNull Region getRegionFromLocation(final String worldName, final Vector3d position) {
        return MathUtils.findBest(this.values.values(), region -> region.inRegion(worldName, position) && !region.isDefault(), (a, b) -> a.priority() > b.priority(), getDefaultRegion(worldName));
    }

    @Override
    protected Region put(Region obj) {
        return super.put(obj);
    }

    public DefaultRegion getDefaultRegion(final String world) {
        if (this.defaultRegions.containsKey(world)) return this.defaultRegions.get(world);
        final DefaultRegion rg = findDefaultRegion(world);
        this.defaultRegions.put(world, rg);
        return rg;
    }

    @NotNull
    private DefaultRegion findDefaultRegion(final String world) {
        File file = getFile("default-" + world);
        if (file.exists() && load(file) instanceof DefaultRegion defaultRegion) return defaultRegion;
        DefaultRegion region = new DefaultRegion(world);
        save(region);
        return region;
    }

    public Region getWherePlayerIs(final Player player) {
        return this.resource.playerManager().getProfile(player).region();
    }

}