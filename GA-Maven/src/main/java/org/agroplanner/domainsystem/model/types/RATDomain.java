package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of a Right-Angled Triangle geometric domain.
 *
 * <p><strong>Architecture & Geometry:</strong></p>
 * <ul>
 * <li><strong>Coordinate System:</strong> The triangle is anchored in the first quadrant of the Cartesian plane.
 * The right angle coincides with the origin {@code (0, 0)}. The legs extend along the positive X-axis (Base)
 * and the positive Y-axis (Height).</li>
 * <li><strong>Linear Constraints:</strong> Unlike rectangular domains which rely solely on orthogonal bounds,
 * this class implements a <em>Linear Inequality Constraint</em> to define the hypotenuse boundary.</li>
 * <li><strong>Sampling Efficiency:</strong> The ratio between the domain area and its Bounding Box is exactly 0.5 (50%).
 * This implies that during the initial population generation (Rejection Sampling), approximately half of the
 * random points generated within the bounding box will be discarded as invalid.</li>
 * </ul>
 */
public class RATDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** The scalar length of the leg lying on the X-axis. */
    private final double base;

    /** The scalar length of the leg lying on the Y-axis. */
    private final double height;

    /**
     * <strong>Performance Cache:</strong> Stores the slope coefficient {@code (height / base)}.
     * <p>
     * Pre-calculating this value eliminates a division operation from the {@link #isPointOutside}
     * method (Hot Path), transforming the hypotenuse check into a faster multiplication and subtraction sequence.
     * </p>
     */
    private final double slope;

    /**
     * The Minimum Bounding Rectangle (MBR) enclosing the triangle.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a Right-Angled Triangle domain anchored at {@code (0,0)}.
     *
     * <p><strong>Deep Protection:</strong></p>
     * Validates that the geometric dimensions allow for a non-degenerate triangle with positive area.
     *
     * @param base   The length of the horizontal leg (must be strictly positive).
     * @param height The length of the vertical leg (must be strictly positive).
     * @throws DomainConstraintException If dimensions are {@code <= 0}.
     */
    public RATDomain(double base, double height) {

        // Invariant Enforcement
        if (base <= 0) {
            throw new DomainConstraintException("base", "Must be strictly positive.");
        }

        if (height <= 0) {
            throw new DomainConstraintException("height", "Must be strictly positive.");
        }

        this.base = base;
        this.height = height;

        // Optimization: Linear coefficient calculation.
        // Represents 'm' in the line equation y = mx + q (where q is height, but we use an intercept form).
        this.slope = height / base;

        // Bounding Box Initialization:
        // Anchored at (0,0) and extending to the max dimensions (base, height).
        this.boundingBox = new Rectangle2D.Double(0, 0, base, height);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates the geometric constraints comprising two orthogonal boundaries and one linear boundary.
     * <p><strong>Geometric Logic:</strong></p>
     * A point {@code P(x,y)} is considered <strong>outside</strong> if it satisfies any of the following:
     * <ul>
     * <li><strong>Orthogonal Violation:</strong> {@code x < 0} OR {@code y < 0} (Point is not in the first quadrant).</li>
     * <li><strong>Hypotenuse Violation:</strong> The point lies above the line connecting {@code (0, height)} and {@code (base, 0)}.
     * <br>Equation derived from: {@code y > height - (slope * x)}</li>
     * </ul>
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the triangular region.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // 1. Orthogonal Constraints (Fastest checks first)
        // Check if point is outside the first quadrant axes.
        boolean outsideAxes = (x < 0) || (y < 0);
        if (outsideAxes) return true;

        // 2. Linear Constraint (Hypotenuse)
        // The line equation is: y = height - (slope * x)
        // If the point's Y is greater than the Y on the line for that X, it is outside.
        return y > (height - (slope * x));
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the triangle boundaries.
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            // Delegation to the geometric predicate
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Retrieves the Bounding Box defined as {@code [0, 0, base, height]}.
     *
     * <p><strong>Algorithmic Note:</strong></p>
     * For a Right-Angled Triangle, the Bounding Box covers exactly twice the area of the domain.
     * This ensures a 50% acceptance rate for uniform random point generation, which is highly efficient
     * for Rejection Sampling algorithms.
     *
     * @return The immutable boundary definition.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Right Angled Triangle { base = %.2fm, height = %.2fm }", base, height);
    }
}