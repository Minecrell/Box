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
