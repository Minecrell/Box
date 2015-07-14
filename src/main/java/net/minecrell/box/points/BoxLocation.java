/*
 * Copyright (c) 2015, Minecrell <https://github.com/Minecrell>
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

import com.google.common.base.Preconditions;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;

public class BoxLocation extends BoxVector {
    private final World world;

    public BoxLocation(World world, int x, int y, int z) {
        super(x, y, z);
        this.world = Preconditions.checkNotNull(world, "world");
    }

    public World getWorld() {
        return world;
    }

    public Location toLocation() {
        return new Location(world, x, y, z);
    }

    public Block getBlock() {
        return world.getBlockAt(x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoxLocation)) return false;
        if (!super.equals(o)) return false;
        BoxLocation that = (BoxLocation) o;
        return world.equals(that.world);
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + world.hashCode();
    }

    public static BoxLocation from(Location loc) {
        return BoxVector.from(loc).toLocation(loc.getWorld());
    }

    @Override
    public String toString() {
        return "(" + world.getName() + "|" + x + "|" + y + "|" + z + ")";
    }
}
