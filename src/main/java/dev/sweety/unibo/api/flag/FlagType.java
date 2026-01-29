package dev.sweety.unibo.api.flag;

import dev.sweety.unibo.api.serializable.Position;
import dev.sweety.unibo.api.flag.impl.BooleanFlag;
import dev.sweety.unibo.api.flag.impl.ListFlag;
import dev.sweety.unibo.api.flag.impl.PositionFlag;
import dev.sweety.unibo.api.flag.impl.number.DoubleFlag;
import lombok.Getter;

import java.util.function.Consumer;

@Getter
public enum FlagType {
    JOIN(true),
    JOIN_COMBAT(true),
    LEAVE(true),
    ATTACK(true),
    SIGN(true),
    BUILD(true),
    BREAK(true),
    PLACE(true),
    ELYTRA(true),
    THROWABLE(true),
    INTERACT_BLOCK(true),
    SATURATION_CHANGE(true),
    REGEN_HEALTH(true),
    INTERACT_ENTITY(true),
    ENTITY_ACTION(true),
    FALL(true),
    DEATH_DROPS(true),
    INV_DROPS(true)

    ;
    public static final ListFlag<String> BANNED_COMMANDS = new ListFlag<>("banned_commands");
    public static final PositionFlag SPAWN = new PositionFlag("spawn", Position.EMPTY);
    public static final DoubleFlag DAMAGE = new DoubleFlag("damage", 1d);

    private final boolean baseValue;
    private final BooleanFlag flag;

    FlagType(final boolean baseValue) {
        this.flag = new BooleanFlag(name().toLowerCase(), this.baseValue = baseValue);
    }

    public static void init(final Consumer<Flag<?>> add) {
        for (FlagType value : values()) add.accept(value.getFlag());
        add.accept(BANNED_COMMANDS);
        add.accept(SPAWN);
        add.accept(DAMAGE);
    }

    public String getName() {
        return name().toLowerCase();
    }
}
