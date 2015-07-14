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
