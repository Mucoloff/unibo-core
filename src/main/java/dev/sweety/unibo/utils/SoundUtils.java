package dev.sweety.unibo.utils;

import com.github.retrooper.packetevents.protocol.player.User;
import com.github.retrooper.packetevents.protocol.sound.Sound;
import com.github.retrooper.packetevents.protocol.sound.SoundCategory;
import com.github.retrooper.packetevents.protocol.sound.Sounds;
import com.github.retrooper.packetevents.wrapper.play.server.WrapperPlayServerEntitySoundEffect;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.UtilityClass;

@UtilityClass
public class SoundUtils {

    @Getter
    private final WrapperPlayServerEntitySoundEffect soundWrap = new WrapperPlayServerEntitySoundEffect(
            Sounds.ENTITY_EXPERIENCE_ORB_PICKUP,
            SoundCategory.PLAYER,
            -1,
            1f,
            1f
    );

    private final Builder builder = new Builder();

    public Builder get() {
        return builder;
    }

    public void playSound(final User user, final Sound sound) {
        soundWrap.setEntityId(user.getEntityId());
        soundWrap.setSound(sound);
        user.writePacket(soundWrap);
    }

    @NoArgsConstructor
    public class Builder {

        public Builder sound(Sound sound) {
            soundWrap.setSound(sound);
            return this;
        }

        public Builder category(SoundCategory category) {
            soundWrap.setSoundCategory(category);
            return this;
        }

        public Builder volume(float volume) {
            soundWrap.setVolume(volume);
            return this;
        }

        public Builder pitch(float pitch) {
            soundWrap.setPitch(pitch);
            return this;
        }

        public void play(User user) {
            soundWrap.setEntityId(user.getEntityId());
            user.writePacket(soundWrap);
        }
    }
}
