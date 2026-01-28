package dev.sweety.unibo.feature.essential;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.command.CommandWrapper;
import lombok.experimental.UtilityClass;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

@UtilityClass
public class Spawn {

    public void register(final VanillaCore plugin) {

        CommandWrapper.action(plugin, "spawn", (player, args) -> spawn(player)).register();

        CommandWrapper.action(plugin, "setspawn", (player, args) -> {
                    Location location = player.getLocation();
                    World world = location.getWorld();
                    plugin.config().set("spawn.world", world.getName());
                    plugin.config().set("spawn.x", location.getX());
                    plugin.config().set("spawn.y", location.getY());
                    plugin.config().set("spawn.z", location.getZ());
                    plugin.config().set("spawn.yaw", location.getYaw());
                    plugin.config().set("spawn.pitch", location.getPitch());
                    plugin.instance().saveConfig();
                    if (args.length == 0) {
                        world.setSpawnLocation(location);
                        player.sendRichMessage("<green>Set %world% spawn at ".replace("%world%", world.getName()) + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());
                        return;
                    }

                    for (String arg : args) {
                        World w = Bukkit.getWorld(arg);
                        if (w == null) {
                            player.sendRichMessage("<red>World " + arg + " not found.");
                            return;
                        }
                        w.setSpawnLocation(location);
                        player.sendRichMessage("<green>Set %world% spawn at ".replace("%world%", w.getName()) + location.getBlockX() + " " + location.getBlockY() + " " + location.getBlockZ());

                    }
                })
                .permission("unibo.staff.setspawn")
                .suggestion((player, args, suggestions) -> {
                    suggestions.clear();
                    for (World world : Bukkit.getWorlds()) {
                        suggestions.add(world.getName());
                    }
                })
                .register();
    }

    public void spawn(final Player... players) {
        Location spawn = spawn();
        for (Player player : players) {
            if (spawn.getWorld() == null) {
                player.sendRichMessage("<red>Spawn world not found.");
                spawn.setWorld(player.getWorld());
            }
            player.teleportAsync(spawn);
        }
    }

    public @NotNull Location spawn() {
        String worldName = VanillaAPI.config().getString("spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        double x = VanillaAPI.config().getDouble("spawn.x");
        double y = VanillaAPI.config().getDouble("spawn.y");
        double z = VanillaAPI.config().getDouble("spawn.z");
        float yaw = (float) VanillaAPI.config().getDouble("spawn.yaw");
        float pitch = (float) VanillaAPI.config().getDouble("spawn.pitch");
        return new Location(world, x, y, z, yaw, pitch);
    }

}
