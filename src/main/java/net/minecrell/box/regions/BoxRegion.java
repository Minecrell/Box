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
package net.minecrell.box.regions;

import net.minecrell.box.points.BlockDirection;
import net.minecrell.box.points.BoxVector;

import org.bukkit.World;

public class BoxRegion extends Region2D {
    public BoxRegion(World world, int y, int x1, int z1, int x2, int z2) {
        super(world, y, x1, z1, x2, z2);
    }

    public boolean contains(BoxVector vector) {
        return vector.getX() >= a1 && vector.getX() <= a2 &&
                vector.getY() == this.b &&
                vector.getZ() >= c1 && vector.getZ() <= c2;
    }

    public int size(BlockDirection dir) {
        return (dir.isLength() ? c2 - c1 : a2 - a1) + 1;
    }
}
