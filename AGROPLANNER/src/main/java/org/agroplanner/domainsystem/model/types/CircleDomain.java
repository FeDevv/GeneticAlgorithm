package org.agroplanner.domainsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.DomainConstraintException;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of a Circular geometric domain centered at the Cartesian origin {@code (0, 0)}.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Concrete Strategy for the {@link Domain} interface.</li>
 * <li><strong>Computational Optimization:</strong> Implements <em>Squared Euclidean Distance</em> comparisons
 * to eliminate expensive square root operations during boundary checks.</li>
 * <li><strong>Sampling Efficiency:</strong> The ratio between the circular domain area (pi r^2) and its
 * bounding box ((2r)^2) is pi/4 (approx. 0.785). This ensures a high acceptance rate (~78.5%)
 * during the initialization phase (Rejection Sampling), making it highly efficient for stochastic generation.</li>
 * </ul>
 */
public class CircleDomain implements Domain {

    // ------------------- FIELDS -------------------

    /**
     * The radial distance defining the domain boundary.
     */
    private final double radius;

    /**
     * The cached Minimum Bounding Rectangle (MBR).
     * <p>Defined as a square with side length {@code 2*radius} centered at the origin.</p>
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Circular domain centered at {@code (0, 0)}.
     *
     * <p><strong>Deep Protection:</strong></p>
     * Enforces the class invariant that a physical domain must have a strictly positive radius.
     * Invalid inputs are rejected immediately to prevent geometric corruption.
     *
     * @param radius The radius of the circle (must be strictly positive).
     * @throws DomainConstraintException If {@code radius <= 0}.
     */
    public CircleDomain(double radius) {

        // Invariant Enforcement
        if (radius <= 0) {
            throw new DomainConstraintException("radius", "Must be strictly positive.");
        }

        this.radius = radius;

        // Optimization: Pre-calculate the bounding box.
        // The box spans from [-r, -r] to [r, r]. width = 2r, height = 2r.
        // This is calculated once at instantiation (O(1)) to support O(1) retrieval later.
        this.boundingBox = new Rectangle2D.Double(-radius, -radius, radius*2, radius*2);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates geometric constraints using Squared Euclidean Distance.
     *
     * <p><strong>Algorithmic Optimization (Hot Path):</strong></p>
     * Instead of calculating the actual distance {@code d = sqrt(x² + y²)} and comparing {@code d > r},
     * the method compares the squared values: {@code (x² + y²) > r²}.
     * <br>
     * Since the square function is monotonic for positive numbers, the inequality holds true.
     * This avoids the {@link Math#sqrt(double)} CPU instruction, which is significantly more expensive
     * than simple multiplication, boosting performance during intensive fitness evaluations.
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the circular radius.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized Inequality: x² + y² > r²
        return (x * x + y * y) > (radius * radius);
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the circle.
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            // Delegation to the optimized geometric predicate
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Retrieves the pre-calculated Bounding Box.
     *
     * @return A {@link Rectangle2D} centered at (0,0) with dimensions {@code 2r x 2r}.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Circle { radius = %.2fm }", radius);
    }
}
