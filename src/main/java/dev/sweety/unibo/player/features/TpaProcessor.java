package dev.sweety.unibo.player.features;


import dev.sweety.unibo.api.serializable.Position;

import java.util.*;

public class TpaProcessor {

    private final Map<UUID, Position> requests = new HashMap<>();

    private boolean addRequest(final UUID id, final Position position) {
        return false;
    }

    /*

    tpa <player>
    tpahere <player>
    tpaccept [player]  //tpyes
    tpdeny //tpno
    tpacancel

     */
}
