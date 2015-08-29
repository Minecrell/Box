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
package net.minecrell.box;

import static java.util.Objects.requireNonNull;

import net.minecrell.box.config.FontShape;
import net.minecrell.box.point.BoxLocation;
import net.minecrell.box.point.BoxPoint;
import net.minecrell.box.point.BoxVector;
import net.minecrell.box.region.Region2i;
import org.bukkit.World;

import java.util.Set;
import java.util.stream.Stream;

public class BoxFont extends Region2i {

    protected final int baseA, baseY;
    protected final boolean x;

    protected BoxFont(World world, BoxFontBounds bounds) {
        super(world, bounds.b, bounds.a1, bounds.y1, bounds.a2, bounds.y2);
        this.x = bounds.x;
        this.baseA = bounds.baseA;
        this.baseY = bounds.baseY;
    }

    public Stream<BoxLocation> draw(FontShape shape) {
        Set<BoxPoint> points = shape.getPoints();
        return points != null ? points.stream().map(this::point) : this.stream();
    }

    public BoxLocation point(BoxPoint point) {
        return location(world, Math.abs(baseA + point.getX()), b, baseY - point.getY());
    }

    @Override
    protected BoxVector vector(int a, int b, int y) {
        return x ? super.vector(b, y, a) : super.vector(a, y, b);
    }

    @Override
    protected BoxLocation location(World world, int a, int b, int y) {
        return x ? super.location(world, b, y, a) : super.location(world, a, y, b);
    }

    public static BoxFont create(World world, BoxVector pos1, BoxVector pos2) {
        return new BoxFont(world, new BoxFontBounds(pos1, pos2));
    }

    public static class BoxFontBounds {

        protected final BoxVector pos1, pos2;
        protected final int a1, y1;
        protected final int a2, y2;
        protected final int b;
        protected final boolean x;
        protected final int baseA, baseY;

        public BoxFontBounds(BoxVector pos1, BoxVector pos2) {
            this.pos1 = requireNonNull(pos1, "pos1");
            this.pos2 = requireNonNull(pos2, "pos2");
            this.y1 = pos1.getY();
            this.y2 = pos2.getY();
            if (pos1.getX() == pos2.getX()) {
                this.x = true;
                this.b = pos1.getX();
                this.a1 = pos1.getZ();
                this.a2 = pos2.getZ();
            } else if (pos1.getZ() == pos2.getZ()) {
                this.x = false;
                this.b = pos1.getZ();
                this.a1 = pos1.getX();
                this.a2 = pos2.getX();
            } else {
                throw new IllegalArgumentException("No match found: " + pos1 + " <-> " + pos2);
            }

            this.baseA = a1 < a2 ? a1 : -a1;
            this.baseY = Math.max(y1, y2);
        }
    }
}
