package pcd.ass03.model;

/**
 * 2D Vector, used to represent velocity
 * Copyright 2000-2001-2002  aliCE team at deis.unibo.it
 * @param x x coordinate
 * @param y y coordinate
 */
public record V2d(double x, double y) {

    /**
     * Sums this vector with another vector.
     * @param v the vector to sum
     * @return a new vector representing the sum
     */
    public V2d sum(V2d v) {
        return new V2d(this.x + v.x, this.y + v.y);
    }

    /**
     * Calculates the absolute value of this vector.
     * @return the absolute value of the vector
     */
    public double abs() {
        return Math.sqrt(this.x * this.x + this.y * this.y);
    }

    /**
     * Normalizes this vector, returning a new vector with the same direction but unit length.
     * @return a new normalized vector
     */
    public V2d getNormalized() {
        double module = Math.sqrt(x * x + y * y);
        return new V2d(this.x / module, this.y / module);
    }

    /**
     * Multiplies this vector by a scalar factor.
     * @param factor the scalar factor
     * @return the new vector resulting from the multiplication
     */
    public V2d mul(double factor) {
        return new V2d(this.x * factor, this.y * factor);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {
        return "V2d(" + this.x + ", " + this.y + ")";
    }
}