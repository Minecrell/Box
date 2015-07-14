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
        return this == EAST || this == SOUTH;
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
