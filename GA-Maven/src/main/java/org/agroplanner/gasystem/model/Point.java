package org.agroplanner.gasystem.model;

/**
 * <p><strong>The Fundamental Unit of the Genetic Algorithm (The Gene).</strong></p>
 *
 * <p>This class represents a geometric point with a radius (a circular entity) in the 2D space.
 * In the context of the Genetic Algorithm, a {@code Point} acts as a <strong>Gene</strong>:
 * an atomic, indivisible part of a solution ({@link Individual}).</p>
 *
 * <p><strong>Architectural Choice: Immutability.</strong><br>
 * This class is designed to be completely immutable (all fields are {@code final}).
 * This provides several critical benefits:</p>
 * <ul>
 * <li><strong>Thread Safety:</strong> Instances can be safely shared across parallel streams without synchronization.</li>
 * <li><strong>Genetic Isolation:</strong> When Mutation or Crossover operators act, they create <em>new</em> points
 * rather than modifying existing ones. This prevents side-effects where mutating a child accidentally mutates the parent.</li>
 * </ul>
 */
public class Point {
    // ------------------- STATE -------------------

    /** The X coordinate of the center. */
    private final double x;

    /** The Y coordinate of the center. */
    private final double y;

    /**
     * The radius (size) of the object.
     * Defines the spatial footprint for overlap calculations.
     */
    private final double radius;

    // ------------------- CONSTRUCTORS -------------------

    /**
     * Primary constructor: Creates a full Gene.
     *
     * @param x      The X coordinate.
     * @param y      The Y coordinate.
     * @param radius The physical radius of the object.
     */
    public Point(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    /**
     * Retrieves the X coordinate.
     * @return The value on the horizontal axis.
     */
    public double getX() { return x; }

    /**
     * Retrieves the Y coordinate.
     * @return The value on the vertical axis.
     */
    public double getY() { return y; }

    /**
     * Retrieves the radius.
     * @return The size of the object.
     */
    public double getRadius() { return radius; }

    /**
     * Returns a formatted string representation of the point coordinates.
     * <p>Format: {@code (x.xxxx, y.yyyy)}</p>
     *
     * @return The string representation suitable for logging or export.
     */
    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", this.x, this.y);
    }
}
