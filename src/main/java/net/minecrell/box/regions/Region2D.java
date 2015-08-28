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

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;
import net.minecrell.box.points.BoxLocation;
import net.minecrell.box.points.BoxVector;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class Region2D implements Iterable<BoxLocation> {

    protected final World world;
    protected final int b;
    protected final int a1, c1;
    protected final int a2, c2;

    public Region2D(World world, int b, int a1, int c1, int a2, int c2) {
        this.world = Preconditions.checkNotNull(world, "world");
        this.b = b;
        this.a1 = Math.min(a1, a2);
        this.c1 = Math.min(c1, c2);
        this.a2 = Math.max(a1, a2);
        this.c2 = Math.max(c1, c2);
    }

    public World getWorld() {
        return world;
    }

    protected BoxVector vector(int a, int b, int c) {
        return new BoxVector(a, b, c);
    }

    protected BoxLocation location(World world, int a, int b, int c) {
        return new BoxLocation(world, a, b, c);
    }

    @Override
    public Iterator<BoxLocation> iterator() {
        return new Iterator<BoxLocation>() {
            private int a = a1, c = c1;
            private boolean done;

            @Override
            public boolean hasNext() {
                return !done;
            }

            @Override
            public BoxLocation next() {
                if (!hasNext()) throw new NoSuchElementException();
                BoxLocation result = location(world, a, b, c);
                if (++a > a2) {
                    a = a1;
                    if (++c > c2)
                        done = true;
                }

                return result;
            }
        };
    }

    public Iterable<Block> blocks() {
        return Iterables.transform(this, BoxLocation::getBlock);
    }
}
