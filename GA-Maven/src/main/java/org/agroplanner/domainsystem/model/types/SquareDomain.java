package org.agroplanner.domainsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.DomainConstraintException;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of a Square geometric domain centered at the Cartesian origin {@code (0, 0)}.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Strategy Implementation (Concrete Strategy for {@link Domain}).</li>
 * <li><strong>Coordinate System:</strong> By enforcing an origin-centered definitions (from {@code -side/2} to {@code +side/2}),
 * this class simplifies boundary checks using axial symmetry, reducing computational overhead compared to arbitrary positioning.</li>
 * <li><strong>Immutability:</strong> The class is fully immutable. All derived geometric properties (e.g., boundaries)
 * are pre-calculated at instantiation time to maximize read performance during the evolutionary cycle.</li>
 * </ul>
 */
public class SquareDomain implements Domain {

    // ------------------- FIELDS -------------------

    /**
     * The absolute length of the square's side.
     */
    private final double side;

    /**
     * <strong>Performance Cache:</strong> Stores {@code side / 2.0}.
     * <p>
     * Pre-calculating this value avoids repetitive division operations during the
     * {@link #isPointOutside(double, double)} checks, which are executed millions of times
     * per simulation run (Hot Path optimization).
     * </p>
     */
    private final double halfSide;

    /**
     * The cached Minimum Bounding Rectangle (MBR).
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Square domain centered at {@code (0, 0)}.
     *
     * <p><strong>Deep Protection:</strong></p>
     * Enforces strict class invariants immediately. Although the Factory provides a first layer of validation,
     * this constructor acts as the final gatekeeper to ensure no invalid geometric state can ever exist
     * within the application memory.
     *
     * @param side The length of the side (must be strictly positive).
     * @throws DomainConstraintException If {@code side <= 0}.
     */
    public SquareDomain(double side) {

        // Invariant Enforcement
        if (side <= 0) {
            throw new DomainConstraintException("side", "Must be strictly positive.");
        }

        this.side = side;

        // Optimization: Pre-calculate the boundary limit relative to the origin.
        this.halfSide = side / 2.0;

        // Initialization: Bounding Box.
        // Since the square is centered, top-left is (-s/2, -s/2).
        this.boundingBox = new Rectangle2D.Double(-side/2 , -side/2 , side, side);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates geometric constraints using axial symmetry.
     *
     * <p><strong>Algorithmic Efficiency:</strong></p>
     * Exploits the origin-centered property. A point is outside if its absolute distance from the
     * center along either axis exceeds the half-side length.
     * <br>
     * Formula: {@code |x| > (side/2) OR |y| > (side/2)}
     *
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the square boundaries.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized check using symmetry (Math.abs)
        // Eliminates the need for separate min/max range checks (e.g., x < min || x > max).
        return Math.abs(x) > halfSide || Math.abs(y) > halfSide;
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points (genes) in the individual.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the square.
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
     * <p><strong>Geometric Identity:</strong></p>
     * For a Square domain aligned with the axes, the Bounding Box is geometrically identical
     * to the domain itself. This method returns the cached {@link Rectangle2D} object in O(1) time.
     *
     * @return The immutable boundary definition.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Square { side = %.2fm }", side);
    }
}
