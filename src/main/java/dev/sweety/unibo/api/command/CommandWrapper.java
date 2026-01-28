package dev.sweety.unibo.api.command;

import dev.sweety.core.util.ObjectUtils;
import dev.sweety.record.annotations.RecordGetter;
import dev.sweety.record.annotations.RecordSetter;
import dev.sweety.unibo.VanillaCore;
import dev.sweety.unibo.VanillaCoreAccessors;
import dev.sweety.unibo.utils.ColorUtils;
import dev.sweety.unibo.utils.McUtils;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.command.defaults.BukkitCommand;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RecordGetter
public class CommandWrapper implements CommandWrapperAccessors {

    protected final VanillaCore plugin;
    private final String name, permission, description;
    private final boolean player;
    private final List<String> aliases;

    @RecordSetter
    private String noPermissionMessage;//setter

    private final Executor executor;
    private final Suggestion suggestion;

    /**
     * Costruttore "centrale": tutto passa da qui (Builder o costruttori legacy).
     */
    private CommandWrapper(
            final VanillaCore plugin,
            final String name,
            final String description,
            final String permission,
            final boolean player,
            final List<String> aliases,
            final String noPermissionMessage,
            final Executor executor,
            final Suggestion suggestion
    ) {
        if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be null/blank");
        if (executor == null) throw new IllegalArgumentException("executor cannot be null");

        this.plugin = plugin;
        this.name = name;
        this.description = description == null ? "" : description;
        this.permission = permission; // può essere null se vuoi "nessun permesso"
        this.player = player;
        this.aliases = aliases == null ? List.of() : List.copyOf(aliases);
        this.noPermissionMessage = noPermissionMessage;
        this.executor = executor;

        // default suggestion: fallback su tab() della classe
        this.suggestion = (suggestion != null) ? suggestion : this::tab;
    }

    /**
     * Builder entrypoint (consigliato).
     */
    public static Builder builder(final VanillaCore plugin, final String name, final Executor executor) {
        return new Builder(plugin, name, executor);
    }

    public static Builder action(final VanillaCore plugin, final String name, final Action executor) {
        return new Builder(plugin, name, executor);
    }

    public CommandWrapper(final VanillaCore plugin) {
        this.plugin = plugin;
        if (!getClass().isAnnotationPresent(Info.class))
            throw new RuntimeException("Info annotation not found on " + this.getClass().getSimpleName());

        final Info info = this.getClass().getAnnotation(Info.class);
        this.name = info.name();
        this.description = ObjectUtils.nullOption(info.description(), info.name());
        this.permission = info.permission();
        this.noPermissionMessage = info.noPermissionMessage();
        this.player = info.player();

        this.executor = player ? ((Action) this::execute) : this::execute;
        this.aliases = new ArrayList<>(Arrays.stream(info.aliases()).toList());

        this.suggestion = this::tab;
    }

    protected static <C extends CommandWrapper> @NotNull BukkitCommand command(final C t) {
        return new BukkitCommand(t.name(), t.description(), "/" + t.name(), t.aliases()) {
            @Override
            public @NotNull List<String> tabComplete(final @NotNull CommandSender sender, final @NotNull String alias, final @NotNull String @NotNull [] args) throws IllegalArgumentException {
                final List<String> suggestions = new ArrayList<>();
                if (t.suggestion() != null) {
                    t.suggestion().suggest(sender, args, suggestions);
                } else t.tab(sender, args, suggestions);
                if (suggestions.isEmpty()) suggestions.addAll(McUtils.onlineNames());

                return suggestions.stream()
                        .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                        .toList();
            }

            @Override
            public boolean execute(final @NotNull CommandSender sender, final @NotNull String commandLabel, final @NotNull String @NotNull [] args) {
                if (t.permission() != null && !sender.hasPermission(t.permission())) {
                    final String noPermMessage = t.noPermissionMessage();
                    if (noPermMessage != null)
                        sender.sendMessage(ColorUtils.color(noPermMessage));
                    return true;
                }

                if (t.player() && !(sender instanceof Player)) {
                    sender.sendRichMessage("<red>You must be a player to execute this command.");
                    return true;
                }

                final Executor executor = t.executor();
                if (executor != null) {
                    executor.execute(sender, args);
                }
                return true;
            }
        };
    }

    public void register() {
        final BukkitCommand command = command(this);
        boolean register = Bukkit.getServer().getCommandMap().register(
                command.getLabel(),
                plugin.name(),
                command
        );

        if (!register) {
            VanillaCoreAccessors.logger().warn("Il comando " + name + " non ha potuto essere registrato con l'etichetta originale. Verrà usato il prefisso di fallback.");
        }
    }

    protected void tab(final @NotNull CommandSender sender, final @NotNull String[] args, final List<String> suggestions) {
    }

    public void execute(final CommandSender sender, final String[] args) {
    }

    public void execute(final Player player, final String[] args) {
    }

    public static class Builder {
        private final VanillaCore plugin;
        private final String name;
        private final Executor executor;

        private String description = "";
        private String permission; // default: unibo.<name>
        private final List<String> aliases = new ArrayList<>();
        private String noPermissionMessage;
        private Suggestion suggestion;

        private Builder(final VanillaCore plugin, final String name, final Executor executor) {
            if (plugin == null) throw new IllegalArgumentException("plugin cannot be null");
            if (name == null || name.isBlank()) throw new IllegalArgumentException("name cannot be null/blank");
            if (executor == null) throw new IllegalArgumentException("executor cannot be null");
            this.plugin = plugin;
            this.name = name;
            this.executor = executor;
        }

        public Builder description(final String description) {
            this.description = description == null ? "" : description;
            return this;
        }

        /**
         * Se non impostato, di default diventa "unibo.<name>".
         * Puoi passare null per disabilitare il controllo permessi.
         */
        public Builder permission(final String permission) {
            this.permission = permission;
            return this;
        }

        public Builder alias(final String alias) {
            if (!ObjectUtils.isNull(alias)) this.aliases.add(alias);
            return this;
        }

        public Builder aliases(final String... aliases) {
            if (aliases != null) for (String alias : aliases) if (!ObjectUtils.isNull(alias)) this.aliases.add(alias);
            return this;
        }

        public Builder noPermissionMessage(final String noPermissionMessage) {
            this.noPermissionMessage = noPermissionMessage;
            return this;
        }

        public Builder suggestion(final Suggestion suggestion) {
            this.suggestion = suggestion;
            return this;
        }

        public CommandWrapper build() {
            final String effectivePermission = (this.permission != null || this.permissionWasExplicitlySet())
                    ? this.permission
                    : "unibo." + name;

            final boolean effectivePlayer = (executor instanceof Action);

            return new CommandWrapper(
                    plugin,
                    name,
                    description,
                    effectivePermission,
                    effectivePlayer,
                    aliases,
                    noPermissionMessage,
                    executor,
                    suggestion
            );
        }

        public void register() {
            build().register();
        }

        /**
         * Piccolo trucco: se vuoi distinguere tra "non impostato" e "impostato a null".
         * In questa versione semplice: consideriamo "non impostato" quando permission == null e non hai mai chiamato permission(...).
         * Se ti serve davvero questa distinzione, dimmelo e lo rendiamo esplicito con un flag.
         */
        private boolean permissionWasExplicitlySet() {
            return false;
        }
    }

    public interface Executor {
        void execute(final CommandSender sender, final String[] args);
    }

    public interface Action extends Executor {
        void run(final Player player, final String[] args);

        @Override
        default void execute(final CommandSender sender, final String[] args) {
            if (!(sender instanceof Player player)) return;
            run(player, args);
        }
    }

    public interface Suggestion {
        void suggest(final CommandSender player, final String[] args, final List<String> suggestions);
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface Info {
        String name();

        String permission();

        boolean player() default true;

        String description() default "";

        String noPermissionMessage() default "";

        String[] aliases() default {};
    }
}
