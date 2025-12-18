package org.agroplanner.gasystem.model;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.exceptions.InvalidInputException;

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

    /** The identity tag. */
    private final PlantType type;

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
     * @param type   The plant identifier.
     */
    public Point(double x, double y, double radius, PlantType type) {
        // Validation: Coordinates
        // Accettiamo coordinate negative? Dipende dal dominio, ma di solito i NaN sono il nemico.
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new InvalidInputException("Coordinates cannot be NaN.");
        }

        // Validation: Radius
        if (radius <= 0) {
            throw new InvalidInputException("Point radius must be positive. Got: " + radius);
        }

        // Validation: Type (CRITICO PER L'EXPORT)
        if (type == null) {
            throw new InvalidInputException("PlantType cannot be null. Every point must have a species identity.");
        }

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
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

    public PlantType getType() { return type; }

    /**
     * Returns a formatted string representation of the point coordinates.
     * <p>Format: {@code (x.xxxx, y.yyyy)}</p>
     *
     * @return The string representation suitable for logging or export.
     */

    @Override
    public String toString() {
        // Esempio output: Pomodoro[r=1.50]@(10.00, 20.00)
        return String.format("%s[r=%.2f]@(%.4f, %.4f)", type.name(), radius, x, y);
    }
}
