package dev.sweety.unibo.feature.region.command;

import dev.sweety.core.math.Pair;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.VanillaAPI;
import dev.sweety.unibo.api.command.CommandWrapper;
import dev.sweety.unibo.api.flag.impl.BooleanFlag;
import dev.sweety.unibo.api.flag.impl.PositionFlag;
import dev.sweety.unibo.api.flag.Flag;
import dev.sweety.unibo.api.flag.FlagType;
import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.feature.region.Region;
import dev.sweety.unibo.feature.region.RegionManager;
import dev.sweety.unibo.file.language.Language;
import dev.sweety.unibo.utils.McUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import net.kyori.adventure.text.event.HoverEvent;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@CommandWrapper.Info(name = "region", description = "Region command", permission = "unibo.region.command.region", aliases = {"rg", "regions"})
public class RegionCommand extends CommandWrapper {

    public static Map<UUID, Pair<Location>> LOCATIONS = new HashMap<>();
    private final RegionManager regionManager;

    public RegionCommand(final VanillaCore plugin) {
        super(plugin);
        this.regionManager = plugin.regionManager();
    }

    @Override
    public void execute(final Player player, final String[] args) {
        switch (args.length) {
            case 1 -> {
                switch (args[0]) {
                    case "pos1", "pos2" -> {
                        final Pair<Location> locations = LOCATIONS.getOrDefault(player.getUniqueId(), new Pair<>());

                        final Location location = player.getLocation();

                        if (args[0].equals("pos1")) {
                            locations.setFirst(location);
                            player.sendMessage(Language.REGIONS_POS_FIRST.component("%x%", String.valueOf(location.getBlockX()), "%y%", String.valueOf(location.getBlockY()), "%z%", String.valueOf(location.getBlockZ())));
                        } else {
                            locations.setSecond(location);
                            player.sendMessage(Language.REGIONS_POS_SECOND.component("%x%", String.valueOf(location.getBlockX()), "%y%", String.valueOf(location.getBlockY()), "%z%", String.valueOf(location.getBlockZ())));
                        }

                        LOCATIONS.put(player.getUniqueId(), locations);
                    }
                    case "list" -> {
                        Component text = Language.REGIONS_LIST__HEADER.component();
                        String hover = Language.REGIONS_REGION__ENTRY_HOVER__TEXT.get();
                        String command = Language.REGIONS_REGION__ENTRY_CLICK__COMMAND.get();
                        for (Region rg : regionManager.values()) {
                            String name = rg.getName();
                            text = text.append(Component.newline()).append(
                                    McUtils.component(name)
                                            .hoverEvent(HoverEvent.showText(McUtils.component(hover.replace("%region%", name))))
                                            .clickEvent(ClickEvent.runCommand(command.replace("%region%", name)))
                            );
                        }
                        player.sendMessage(text);
                    }
                    case "info" -> {
                        Region rg = this.regionManager.getWherePlayerIs(player);
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", ""));
                            return;
                        }

                        player.sendMessage(rg.info());
                    }
                    case "reload" -> {
                        player.sendMessage(Language.REGIONS_RELOAD.component());
                        regionManager.load();
                    }
                }
            }
            case 2 -> {
                String regionName = args[1];
                List<Region> match = regionManager.match(regionName);
                Region rg = !match.isEmpty() ? match.getFirst() : null;
                switch (args[0]) {
                    case "create" -> {

                        if (rg != null) {
                            player.sendMessage(Language.REGIONS_ERRORS_ALREADY__EXISTS.component("%region%", regionName));
                            return;
                        }
                        final Pair<Location> locations = LOCATIONS.getOrDefault(player.getUniqueId(), new Pair<>());
                        final int size = locations.size();

                        if (size != 2) {

                            player.sendMessage(Language.REGIONS_ERRORS_MISSING__POSITIONS.component());
                            return;
                        }

                        final Location first = locations.getFirst();
                        final Location second = locations.getSecond();
                        final Region reg = new Region(regionName, first, second);
                        regionManager.add(reg);

                        final String bound = "%s [%.2f:%.2f][%.2f:%.2f][%.2f:%.2f]".formatted(first.getWorld().getName(), first.getX(), second.getX(), first.getY(), second.getY(), first.getZ(), second.getZ());
                        player.sendMessage(Language.REGIONS_SUCCESS_CREATE.component("%region%", regionName, "%bounds%", bound));
                    }
                    case "redefine" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }
                        final Pair<Location> locations = LOCATIONS.getOrDefault(player.getUniqueId(), new Pair<>());
                        final int size = locations.size();

                        if (size != 2) {
                            player.sendMessage(Language.REGIONS_ERRORS_MISSING__POSITIONS.component());
                            return;
                        }

                        final Location first = locations.getFirst();
                        final Location second = locations.getSecond();
                        rg.redefine(first, second);
                        regionManager.add(rg);

                        final String bound = "%s [%.2f:%.2f][%.2f:%.2f][%.2f:%.2f]".formatted(first.getWorld().getName(), first.getX(), second.getX(), first.getY(), second.getY(), first.getZ(), second.getZ());
                        player.sendMessage(Language.REGIONS_SUCCESS_REDEFINE.component("%region%", regionName, "%bounds%", bound));
                    }
                    case "delete" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }
                        regionManager.delete(rg);

                        player.sendMessage(Language.REGIONS_SUCCESS_DELETE.component("%region%", regionName));
                    }
                    case "set-spawn" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }

                        rg.setFlagStatus(FlagType.SPAWN, Position.fromBukkitLocation(player.getLocation()));
                        player.sendMessage(Language.REGIONS_SUCCESS_SET__SPAWN.component("%region%", regionName));
                        regionManager.add(rg);
                    }
                    case "spawn" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }
                        FlagType.SPAWN.teleport(rg, player);
                        player.sendMessage(Language.REGIONS_SUCCESS_SPAWN.component("%region%", regionName));
                    }
                    case "reset" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }

                        rg.reset();
                        player.sendMessage(Language.REGIONS_SUCCESS_RESET__FLAG.component("%region%", regionName));
                        regionManager.add(rg);
                    }
                    case "info" -> {
                        if (rg == null) {
                            player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                            return;
                        }

                        player.sendMessage(rg.info());
                    }
                }
            }
            case 3 -> {
                String regionName = args[1];
                if (args[0].equals("flag")) {
                    List<Region> match = regionManager.match(regionName);
                    Region rg = !match.isEmpty() ? match.getFirst() : null;
                    if (rg == null) {
                        player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                        return;
                    }

                    Flag<?> flag = VanillaAPI.getFlag(args[2]);
                    if (flag == null) {
                        player.sendMessage(Language.REGIONS_ERRORS_INVALID__FLAG.component("%region%", args[2]));
                        return;
                    }

                    if (flag instanceof PositionFlag loc) {
                        Position position = Position.fromBukkitLocation(player.getLocation());
                        rg.setFlagStatus(loc, position);
                        player.sendMessage(Language.REGIONS_FLAG_FLAG__SET.component("%region%", regionName, "%flag%", flag.getName(), "%value%", position.serializeString()));
                    }
                    regionManager.add(rg);
                }
            }
            case 4 -> {
                String regionName = args[1];

                Flag<?> flag = VanillaAPI.getFlag(args[2]);
                if (flag == null) {
                    player.sendMessage(Language.REGIONS_ERRORS_INVALID__FLAG.component("%flag%", args[2]));
                    return;
                }

                List<Region> match = regionManager.match(regionName);
                Region rg = !match.isEmpty() ? match.getFirst() : null;
                if (rg == null) {
                    player.sendMessage(Language.REGIONS_ERRORS_REGION__NOT__FOUND.component("%region%", regionName));
                    return;
                }

                if (args[0].equals("flag")) {
                    if (flag instanceof PositionFlag loc) {
                        Position position = Position.fromBukkitLocation(player.getLocation());
                        switch (args[3]) {
                            case "default" -> rg.resetFlagStatus(loc);
                            case "null" -> rg.setFlagStatus(loc, Position.EMPTY);
                            case "this" -> rg.setFlagStatus(loc, position);
                            case "teleport" -> loc.teleport(rg, player);
                        }

                        switch (args[3]) {
                            case "default", "null", "this" -> {
                                player.sendMessage(Language.REGIONS_FLAG_FLAG__SET.component("%region%", regionName, "%flag%", flag.getName(), "%value%", rg.getFlagStatus(loc).serializeString()));
                                regionManager.add(rg);
                            }
                        }
                        return;
                    } else rg.setFromString(flag, args[3]);
                    player.sendMessage(Language.REGIONS_FLAG_FLAG__SET.component("%region%", regionName, "%flag%", flag.getName(), "%value%", args[3]));
                }
                regionManager.add(rg);
            }

        }
    }

    @Override
    protected void tab(@NotNull CommandSender sender, @NotNull String[] args, List<String> suggestions) {
        if (args.length == 1) {
            suggestions.addAll(List.of("pos1", "pos2", "create", "delete", "redefine", "flag", "exempt", "set-spawn", "spawn", "info", "list", "reload"));
        } else if (args[0].equals("delete") || args[0].equals("set-spawn") || args[0].equals("spawn") || args[0].equals("redefine") || args[0].equals("info") || args[0].equals("reset")) {
            if (args.length == 2) suggestions.addAll(regionManager.getNames());
        } else if (args[0].equals("flag") || args[0].equals("exempt")) {
            if (args.length == 2) suggestions.addAll(regionManager.getNames());
            else if (args.length == 3)
                suggestions.addAll(VanillaAPI.getFlagNames());
            else if (args.length == 4) {
                Flag<?> flag = VanillaAPI.getFlag(args[2]);
                if (flag != null) {
                    if (flag instanceof BooleanFlag)
                        suggestions.addAll(List.of("true", "false"));
                    else if (flag instanceof PositionFlag)
                        suggestions.addAll(List.of("default", "null", "this", "teleport"));
                    else
                        suggestions.add(flag.getDefaultValue().toString());
                }
            }
        }

    }
}
