package net.minecrell.box;

import net.minecrell.box.config.FontShape;
import net.minecrell.box.points.BoxLocation;
import net.minecrell.box.points.BoxPoint;
import net.minecrell.box.points.BoxVector;
import net.minecrell.box.regions.Region2D;

import java.util.Set;

import com.google.common.base.Preconditions;
import com.google.common.collect.Iterables;

import org.bukkit.World;

public class BoxFont extends Region2D {
    protected final int baseA, baseY;
    protected final boolean x;

    protected BoxFont(World world, BoxFontBounds bounds) {
        super(world, bounds.b, bounds.a1, bounds.y1, bounds.a2, bounds.y2);
        this.x = bounds.x;
        this.baseA = bounds.baseA;
        this.baseY = bounds.baseY;
    }

    public Iterable<BoxLocation> draw(FontShape shape) {
        Set<BoxPoint> points = shape.getPoints();
        return points != null ? Iterables.transform(points, this::point) : this;
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
            this.pos1 = Preconditions.checkNotNull(pos1, "pos1");
            this.pos2 = Preconditions.checkNotNull(pos2, "pos2");
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
            } else throw new IllegalArgumentException("No match found: " + pos1 + " <-> " + pos2);

            this.baseA = a1 < a2 ? a1 : -a1;
            this.baseY = Math.max(y1, y2);
        }
    }
}
