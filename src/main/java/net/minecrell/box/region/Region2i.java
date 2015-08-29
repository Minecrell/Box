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
package net.minecrell.box.region;

import static java.util.Objects.requireNonNull;
import static java.util.Spliterator.DISTINCT;
import static java.util.Spliterator.IMMUTABLE;
import static java.util.Spliterator.NONNULL;
import static java.util.Spliterator.SIZED;

import net.minecrell.box.point.BlockDirection;
import net.minecrell.box.point.BoxLocation;
import net.minecrell.box.point.BoxVector;
import net.minecrell.box.util.Streamable;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;

public class Region2i implements Streamable<BoxLocation> {

    protected final World world;
    protected final int b;
    protected final int a1, c1;
    protected final int a2, c2;
    private final int size;

    public Region2i(World world, int b, int a1, int c1, int a2, int c2) {
        this.world = requireNonNull(world, "world");
        this.b = b;
        this.a1 = Math.min(a1, a2);
        this.c1 = Math.min(c1, c2);
        this.a2 = Math.max(a1, a2);
        this.c2 = Math.max(c1, c2);
        this.size = (a2 - a1) * (c2 - c1);
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

    public boolean contains(BoxVector vector) {
        return vector.getX() >= a1 && vector.getX() <= a2 &&
                vector.getY() == this.b &&
                vector.getZ() >= c1 && vector.getZ() <= c2;
    }

    public int size() {
        return this.size;
    }

    public int size(BlockDirection dir) {
        return (dir.isLength() ? c2 - c1 : a2 - a1) + 1;
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
                if (!hasNext()) {
                    throw new NoSuchElementException();
                }

                BoxLocation result = location(world, a, b, c);
                if (++a > a2) {
                    a = a1;
                    if (++c > c2) {
                        done = true;
                    }
                }

                return result;
            }
        };
    }

    public Stream<Block> blocks() {
        return stream().map(BoxLocation::getBlock);
    }

    @Override
    public Spliterator<BoxLocation> spliterator() {
        return Spliterators.spliterator(iterator(), size, DISTINCT | IMMUTABLE | NONNULL | SIZED);
    }

}
