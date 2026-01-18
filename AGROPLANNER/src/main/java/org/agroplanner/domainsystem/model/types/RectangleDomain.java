package org.agroplanner.domainsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.DomainConstraintException;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of a Rectangular geometric domain centered at the Cartesian origin {@code (0, 0)}.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Concrete Strategy for the {@link Domain} interface.</li>
 * <li><strong>Coordinate System:</strong> Defines the valid region as spanning from {@code [-width/2, -height/2]}
 * to {@code [+width/2, +height/2]}. This origin-centered approach facilitates efficient boundary checking
 * using axial symmetry.</li>
 * <li><strong>Immutability:</strong> The class state is frozen upon instantiation, ensuring Thread-Safety
 * without synchronization mechanisms.</li>
 * </ul>
 */
public class RectangleDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** The scalar width of the domain along the X-axis. */
    private final double width;

    /** The scalar height of the domain along the Y-axis. */
    private final double height;

    /**
     * <strong>Performance Cache (X-Axis):</strong> Stores {@code width / 2.0}.
     * <p>Used to optimize the {@link #isPointOutside(double, double)} method by replacing runtime division
     * with a memory lookup.</p>
     */
    private final double halfWidth;

    /**
     * <strong>Performance Cache (Y-Axis):</strong> Stores {@code height / 2.0}.
     * <p>Used to optimize boundary checks along the vertical axis.</p>
     */
    private final double halfHeight;

    /**
     * The cached Minimum Bounding Rectangle (MBR).
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Rectangular domain centered at {@code (0, 0)}.
     *
     * <p><strong>Deep Protection:</strong></p>
     * Validates that both physical dimensions are strictly positive before object construction completes.
     * This enforces the "Class Invariant" that a physical domain must have non-zero, positive area.
     *
     * @param width  The total width along the X-axis.
     * @param height The total height along the Y-axis.
     * @throws DomainConstraintException If either dimension is {@code <= 0}.
     */
    public RectangleDomain(double width, double height) {

        // Invariant Enforcement: Width
        if (width <= 0) {
            throw new DomainConstraintException("width", "Must be strictly positive.");
        }

        // Invariant Enforcement: Height
        if (height <= 0) {
            throw new DomainConstraintException("height", "Must be strictly positive.");
        }

        this.width = width;
        this.height = height;

        // Optimization: Pre-calculate boundaries relative to the origin.
        // This moves the computational cost from the "Hot Path" (isPointOutside) to the constructor.
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;

        // Bounding Box Initialization:
        // Top-Left corner is calculated as (-w/2, -h/2).
        this.boundingBox = new Rectangle2D.Double(-halfWidth, -halfHeight, width, height);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates geometric constraints using axial symmetry.
     *
     *
     * <p><strong>Algorithmic Efficiency:</strong></p>
     * A point is strictly outside the domain if its absolute distance from the center along
     * <em>any</em> axis exceeds the corresponding semi-dimension.
     * <br>
     * Formula: {@code |x| > (width/2) OR |y| > (height/2)}
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the rectangular boundaries.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized check using symmetry (Math.abs)
        // Eliminates the need for 4 comparisons (x < min, x > max, y < min, y > max)
        // reducing it to 2 comparisons with absolute values.
        return Math.abs(x) > halfWidth || Math.abs(y) > halfHeight;
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points (genes).</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the rectangle.
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
     * Retrieves the pre-calculated Bounding Box.
     *
     * <p><strong>Geometric Identity:</strong></p>
     * Since the domain is a rectangle aligned with the Cartesian axes, its Minimum Bounding Rectangle (MBR)
     * is geometrically identical to the domain itself. Returns the cached instance in O(1) time.
     *
     * @return The immutable boundary definition.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Rectangle { width = %.2fm, height = %.2fm }", width, height);
    }
}