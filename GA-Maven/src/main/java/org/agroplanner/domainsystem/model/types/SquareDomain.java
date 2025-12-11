package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Square.</strong></p>
 *
 * <p>Represents a square domain centered at the origin {@code (0, 0)}.
 * The domain spans from {@code [-side/2, -side/2]} to {@code [side/2, side/2]}.
 * This class guarantees immutability and acts as a strict spatial constraint for the Genetic Algorithm.</p>
 */
public class SquareDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** The length of the side of the square. */
    private final double side;

    /**
     * Optimization: Pre-calculated half-side length.
     * Used to leverage symmetry checks (Math.abs) instead of range checks logic.
     */
    private final double halfSide;

    /**
     * For a square domain, the bounding box coincides exactly with the domain boundaries.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a Square domain centered at {@code (0, 0)}.
     *
     * @param side The length of the side.
     * @throws DomainConstraintException If the side is not strictly positive.
     */
    public SquareDomain(double side) {

        // Deep Protection: Integrity check
        // Even if the Factory filters inputs, the Model ensures it cannot exist in an invalid state.
        if (side <= 0) {
            throw new DomainConstraintException("side", "Must be strictly positive.");
        }

        this.side = side;

        // Pre-calculate the boundary limit relative to the origin.
        this.halfSide = side / 2.0;

        // Bounding Box: Top-Left starts at (-s/2, -s/2).
        this.boundingBox = new Rectangle2D.Double(-side/2 , -side/2 , side, side);
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the square.
     *
     * <p><strong>Optimization:</strong>
     * Since the square is centered at (0,0), a point is outside if its absolute distance
     * from the center along either axis exceeds half the side length.
     * Formula: {@code |x| > side/2 || |y| > side/2}
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is strictly outside the boundaries.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized check using symmetry:
        // Instead of checking ranges (min <= x <= max), we check absolute distance from center.
        return Math.abs(x) > halfSide || Math.abs(y) > halfSide;
    }

    /**
     * Validates an entire individual against the square boundary.
     * <p>Complexity: O(N), where N is the number of points.</p>
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            // Delega la verifica della posizione al metodo isPointOutside().
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Retrieves the Bounding Box.
     * For a square, this is identical to the domain geometry itself.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Square { side = %.2f }", side);
    }
}
