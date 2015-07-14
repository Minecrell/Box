package net.minecrell.box.config;

import org.bukkit.Sound;

import static org.bukkit.Sound.*;

public final class BoxSound {
    private BoxSound() {}

    public static Sound
            COUNTDOWN = NOTE_PLING,
            WAY = CHICKEN_EGG_POP,
            REMOVE = GLASS,
            STEP_SUCCESS = NOTE_SNARE_DRUM,
            FAIL = HURT_FLESH;
}
