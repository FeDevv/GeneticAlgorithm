package org.agroplanner.gasystem.model;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.exceptions.InvalidInputException;

/**
 * Immutable representation of a geometric entity in 2D space, functioning as the <strong>Gene</strong>
 * within the evolutionary algorithm.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Immutable Value Object.</li>
 * <li><strong>Role (The Gene):</strong> Represents an atomic, indivisible unit of the solution genotype.
 * A collection of these points forms the {@link Individual} (Chromosome).</li>
 * <li><strong>Concurrency & Safety:</strong>
 * Being deeply immutable, instances of this class are inherently <strong>Thread-Safe</strong>.
 * This allows the genetic operators (Crossover, Mutation) to function in parallel streams without
 * synchronization overhead. Furthermore, it prevents <em>Side-Effects</em>: modifying a child's gene
 * implies creating a new object, guaranteeing that the parent's genome remains historically intact.</li>
 * </ul>
 */
public class Point {

    // ------------------- STATE (IMMUTABLE) -------------------

    /** The Cartesian X-coordinate (center of the plant). */
    private final double x;

    /** The Cartesian Y-coordinate (center of the plant). */
    private final double y;

    /** The biological identity of the plant (Metadata). */
    private final PlantType type;

    /**
     * The physical radius of the organism.
     * <p>Defines the spatial footprint used for collision detection (Fitness Function).</p>
     */
    private final double radius;

    // ------------------- CONSTRUCTORS -------------------

    /**
     * Constructs a new Gene (Point).
     *
     * <p><strong>Validation Logic (Fail-Fast):</strong></p>
     * Ensures that no "Zombie Genes" (invalid states) can exist within the population.
     *
     * @param x      The X coordinate.
     * @param y      The Y coordinate.
     * @param radius The physical radius (must be > 0).
     * @param type   The species identifier (must not be null).
     * @throws InvalidInputException if coordinates are NaN, radius is non-positive, or type is missing.
     */
    public Point(double x, double y, double radius, PlantType type) {
        // 1. Numerical Stability Check
        // NaN (Not a Number) propagates virally in calculations, corrupting the entire fitness score.
        if (Double.isNaN(x) || Double.isNaN(y)) {
            throw new InvalidInputException("Coordinates cannot be NaN.");
        }

        // 2. Physical Constraint Check
        if (radius <= 0) {
            throw new InvalidInputException("Point radius must be positive. Got: " + radius);
        }

        // 3. Metadata Integrity Check
        if (type == null) {
            throw new InvalidInputException("PlantType cannot be null. Every point must have a species identity.");
        }

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
    }

    // ------------------- ACCESSORS -------------------

    /**
     * Retrieves the horizontal position.
     * @return The X coordinate.
     */
    public double getX() { return x; }

    /**
     * Retrieves the vertical position.
     * @return The Y coordinate.
     */
    public double getY() { return y; }

    /**
     * Retrieves the physical footprint size.
     * @return The radius in meters.
     */
    public double getRadius() { return radius; }

    /**
     * Retrieves the biological classification.
     * @return The {@link PlantType} enum constant.
     */
    public PlantType getType() { return type; }


    // ------------------- OBJECT CONTRACT -------------------

    /**
     * Returns a compact string representation suitable for visualization and debugging.
     * <p>Format: {@code NAME[r=0.00]@(x.xxxx, y.yyyy)}</p>
     */
    @Override
    public String toString() {
        return String.format("%s[r=%.2f]@(%.4f, %.4f)", type.name(), radius, x, y);
    }
}
