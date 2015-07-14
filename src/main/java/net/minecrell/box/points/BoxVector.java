package net.minecrell.box.points;

import org.bukkit.Location;
import org.bukkit.World;

public class BoxVector extends BoxPoint {
    protected final int z;

    public BoxVector(int x, int y, int z) {
        super(x, y);
        this.z = z;
    }

    public BoxVector(BoxVector vector) {
        this(vector.x, vector.y, vector.z);
    }

    public int getZ() {
        return z;
    }

    public BoxVector add(int x, int y, int z) {
        return new BoxVector(this.x + x, this.y + y, this.z + z);
    }

    public BoxVector add(BoxVector vector) {
        return this.add(vector.x, vector.y, vector.z);
    }

    public BoxVector subtract(int x, int y, int z) {
        return new BoxVector(this.x - x, this.y - y, this.z - z);
    }

    public BoxVector subtract(BoxVector vector) {
        return this.subtract(vector.x, vector.y, vector.z);
    }

    public BoxVector inverse() {
        return new BoxVector(-x, -y, -z);
    }

    public BoxLocation toLocation(World world) {
        return new BoxLocation(world, x, y, z);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoxVector) || !super.equals(o)) return false;
        BoxVector boxVector = (BoxVector) o;
        return z == boxVector.z;
    }

    @Override
    public int hashCode() {
        return 31 * super.hashCode() + z;
    }

    public static BoxVector from(Location loc) {
        return new BoxVector(loc.getBlockX(), loc.getBlockY(), loc.getBlockZ());
    }

    @Override
    public String toString() {
        return "(" + x + "|" + y + "|" + z + ")";
    }
}
