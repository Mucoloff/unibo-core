package dev.sweety.unibo.feature.essential;

import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.api.command.CommandWrapper;
import net.kyori.adventure.text.Component;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import static net.kyori.adventure.text.format.NamedTextColor.RED;

@CommandWrapper.Info(name = "speed", permission = "unibo.staff.speed", description = "set your speed", player = false)
public class Speed extends CommandWrapper {

    public Speed(VanillaCore plugin) {
        super(plugin);
    }

    @Override
    public void execute(CommandSender sender, String[] args) {
        if (!(sender instanceof Player p)) return;
        if (args.length == 0) {
            p.sendRichMessage("<red>Insert a value for speed");
            return;
        }
        float v = Math.min(1, Math.max(0, Float.parseFloat(args[0])));
        p.setFlySpeed(v);
        p.setWalkSpeed(v);
        p.sendMessage(Component.text("Speed: " + v).color(RED));
    }
}
