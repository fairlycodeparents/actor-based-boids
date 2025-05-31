package pcd.ass03.model;

/**
 * 2D Point.
 * @param x x coordinate
 * @param y y coordinate
 */
public record P2d(double x, double y) {

    /**
     * Updates the coordinates of this point by adding a vector.
     * @param v the vector to add
     * @return a new point with updated coordinates
     */
    public P2d sum(V2d v) {
        return new P2d(x + v.x(), y + v.y());
    }

    /**
     * Subtracts a point from this point, returning a vector.
     * @param p the point to subtract
     * @return a vector representing the difference
     */
    public V2d sub(P2d p) {
        return new V2d(x - p.x(), y - p.y());
    }

    /**
     * Calculates the distance from this point to another point.
     * @param p the other point
     * @return the distance
     */
    public double distance(P2d p) {
        double dx = p.x() - x;
        double dy = p.y() - y;
        return Math.sqrt(dx * dx + dy * dy);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "P2d(" + x + ", " + y + ")";
    }
}