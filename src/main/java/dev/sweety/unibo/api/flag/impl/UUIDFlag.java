package dev.sweety.unibo.api.flag.impl;

import com.github.retrooper.packetevents.util.UUIDUtil;
import dev.sweety.core.util.UUIDUtils;
import dev.sweety.unibo.api.flag.Flag;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class UUIDFlag extends Flag<UUID> {

    public UUIDFlag(@NotNull String name, @NotNull UUID value) {
        super(name, value);
    }

    @Override
    public String serialize(UUID value) {
        return value.toString();
    }

    @Override
    public UUID deserialize(String value) {
        return UUIDUtils.parseUuid(value);
    }
}
