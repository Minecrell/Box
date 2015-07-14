/*
 * Copyright (c) 2014, Minecrell <https://github.com/Minecrell>
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
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
