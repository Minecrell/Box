/*
 * Box
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

import static org.bukkit.DyeColor.BLUE;
import static org.bukkit.DyeColor.LIME;
import static org.bukkit.DyeColor.RED;
import static org.bukkit.DyeColor.SILVER;

import org.bukkit.DyeColor;
import org.bukkit.Material;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

public final class BoxBlock {

    private BoxBlock() {
    }

    public static final MaterialData
            BASE = stainedGlass(SILVER),
            WAY = stainedGlass(BLUE),
            SUCCESS = stainedGlass(LIME),
            FAIL = stainedGlass(RED);

    private static MaterialData stainedGlass(DyeColor color) {
        return coloredBlock(Material.STAINED_GLASS, color);
    }

    private static MaterialData coloredBlock(Material material, DyeColor color) {
        Wool wool = new Wool(material);
        wool.setColor(color);
        return wool;
    }
}
