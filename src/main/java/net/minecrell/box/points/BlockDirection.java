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
package net.minecrell.box.points;

import org.bukkit.block.BlockFace;

public enum BlockDirection {
    NORTH (BlockFace.NORTH),
    SOUTH (BlockFace.SOUTH),
    EAST (BlockFace.EAST),
    WEST (BlockFace.WEST);

    private final BoxVector patch;

    private BlockDirection(BlockFace blockFace) {
        this.patch = new BoxVector(blockFace.getModX(), blockFace.getModY(), blockFace.getModZ());
    }

    public BoxVector getPatch() {
        return patch;
    }

    public boolean isLength() {
        return this == NORTH || this == SOUTH;
    }

    public boolean isWidth() {
        return this == EAST || this == WEST;
    }

    public BoxVector relative(BoxVector v) {
        return v.add(patch);
    }

    public BlockDirection opposite() {
        switch (this) {
            case NORTH: return SOUTH;
            case SOUTH: return NORTH;
            case EAST: return WEST;
            case WEST: return EAST;
        }

        return null;
    }
}
