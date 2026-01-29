package dev.sweety.unibo.player.features.teleport;

import dev.sweety.unibo.file.language.Language;

public enum CancelReasons {
    OFFLINE(Language.TELEPORT_TPA_CANCEL_REASONS_OFFLINE),
    QUIT(Language.TELEPORT_TPA_CANCEL_REASONS_QUIT),
    MOVED(Language.TELEPORT_TPA_CANCEL_REASONS_MOVED),
    FAILED(Language.TELEPORT_TPA_CANCEL_REASONS_FAILED),
    CANCEL(Language.TELEPORT_TPA_CANCEL_REASONS_CANCEL);

    private final Language lang;

    CancelReasons(final Language lang) {
        this.lang = lang;
    }

    public Language lang() {
        return lang;
    }
}
