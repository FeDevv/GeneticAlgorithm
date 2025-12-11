package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Circle.</strong></p>
 *
 * <p>Represents a 2D circular area centered at the origin {@code (0, 0)}.
 * This class guarantees immutability and state consistency upon creation.</p>
 */
public class CircleDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** The radius defining the boundary of the domain. */
    private final double radius;

    /**
     * The smallest square containing the circle.
     * Calculated once during initialization for performance.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a circular domain centered at {@code (0, 0)}.
     *
     * @param radius The radius of the circle.
     * @throws DomainConstraintException If the radius is not strictly positive.
     */
    public CircleDomain(double radius) {

        // Deep Protection / Defensive Programming:
        // Even if the Factory validates inputs, the Model itself must ensure it never exists in an invalid state.
        if (radius <= 0) {
            throw new DomainConstraintException("radius", "Must be strictly positive.");
        }

        this.radius = radius;

        // Implementation Choice: Pre-calculate the bounding box.
        // The box spans from [-r, -r] to [r, r]. width = 2r, height = 2r.
        this.boundingBox = new Rectangle2D.Double(-radius, -radius, radius*2, radius*2);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the circle.
     *
     * <p><strong>Optimization:</strong> Uses squared Euclidean distance {@code (x² + y² > r²)}
     * to avoid the computationally expensive {@link Math#sqrt(double)} operation.</p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is strictly outside the radius; {@code false} otherwise.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Inequality: x² + y² > r²
        return (x * x + y * y) > (radius * radius);
    }

    /**
     * Validates an entire individual against the circular boundary.
     * <p>Complexity: O(N), where N is the number of points (genes).</p>
     *
     * @param individual The solution to validate.
     * @return {@code true} if every point is within the circle.
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Retrieves the Bounding Box used for random point generation.
     *
     * @return A {@link Rectangle2D} centered at (0,0) with side length {@code 2*radius}.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Circle { radius = %.2f }", radius);
    }
}
