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

import static org.bukkit.DyeColor.BLACK;
import static org.bukkit.DyeColor.LIME;
import static org.bukkit.DyeColor.RED;
import static org.bukkit.DyeColor.YELLOW;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableSet;
import net.minecrell.box.points.BoxPoint;
import org.bukkit.material.MaterialData;
import org.bukkit.material.Wool;

import java.util.Set;

public enum FontShape {
    BASE (new Wool(BLACK)),

    SUCCESS (new Wool(LIME),
            "  X X  ",
            "  X X  ",
            "       ",
            "X     X",
            " X   X ",
            "  XXX  "
    ),

    FAIL (new Wool(RED),
            "  X X  ",
            "  X X  ",
            "       ",
            "  XXX  ",
            " X   X ",
            "X     X"
    ),

    ONE (new Wool(YELLOW),
            "  XX   ",
            "   X   ",
            "   X   ",
            "   X   ",
            "  XXX  ",
            "       "
    ),

    TWO (new Wool(YELLOW),
            "  XXX  ",
            "    X  ",
            "   X   ",
            "  X    ",
            "  XXX  ",
            "       "
    ),

    THREE (new Wool(YELLOW),
            "  XXX  ",
            "    X  ",
            "  XXX  ",
            "    X  ",
            "  XXX  ",
            "       "
    );

    private static final int HEIGHT = 6, WIDTH = 7;

    private final MaterialData material;
    private final Set<BoxPoint> points;

    private FontShape(MaterialData material, String... shape) {
        this.material = material;

        if (shape.length > 0) {
            Preconditions.checkArgument(HEIGHT == shape.length, "invalid shape size");
            ImmutableSet.Builder<BoxPoint> points = ImmutableSet.<BoxPoint>builder();

            for (int y = 0; y < shape.length; y++) {
                char[] line = shape[y].toCharArray();
                Preconditions.checkArgument(WIDTH == line.length, "invalid shape size for " + name()
                        + " at line " + y);
                for (int x = 0; x < line.length; x++)
                    if (!Character.isWhitespace(line[x]))
                        points.add(new BoxPoint(x, y));
            }

            this.points = points.build();
        } else {
            this.points = null;
        }
    }

    public MaterialData getMaterial() {
        return material;
    }

    public Set<BoxPoint> getPoints() {
        return points;
    }
}
