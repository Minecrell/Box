package net.minecrell.box.points;

public class BoxPoint {
    protected final int x, y;

    public BoxPoint(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public BoxPoint(BoxPoint point) {
        this(point.x, point.y);
    }

    public final int getX() {
        return x;
    }

    public final int getY() {
        return y;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof BoxPoint)) return false;
        BoxPoint point = (BoxPoint) o;
        return x == point.x && y == point.y;

    }

    @Override
    public int hashCode() {
        return 31 * x + y;
    }

    @Override
    public String toString() {
        return "(" + x + "|" + y + ")";
    }
}
