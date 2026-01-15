package org.agroplanner.domainsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.DomainConstraintException;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of a Rectangular Frame geometric domain.
 *
 * <p><strong>Architecture & Topology:</strong></p>
 * <ul>
 * <li><strong>Geometry:</strong> Represents a <strong>non-convex</strong> domain defined by the region between two
 * concentric rectangles centered at {@code (0, 0)}. It functionally acts as a "Picture Frame".</li>
 * <li><strong>Constraint Logic:</strong> Implements a composite boundary check. Valid points must reside within
 * the "Band" defined by the outer container and the inner exclusion zone (hole).</li>
 * <li><strong>Sampling Efficiency:</strong> Similar to the Annulus, the efficiency of Rejection Sampling depends on the
 * frame thickness. Thin frames result in a higher rejection rate during initial population generation.</li>
 * </ul>
 */
public class FrameDomain implements Domain {

    // ------------------- FIELDS -------------------

    // Original dimensions
    private final double innerWidth;
    private final double innerHeight;
    private final double outerWidth;
    private final double outerHeight;

    /**
     * <strong>Performance Cache (Outer):</strong> Stores {@code outerWidth / 2.0}.
     * <p>Used to perform symmetric boundary checks on the container's X-axis.</p>
     */
    private final double halfOuterWidth;

    /**
     * <strong>Performance Cache (Outer):</strong> Stores {@code outerHeight / 2.0}.
     * <p>Used to perform symmetric boundary checks on the container's Y-axis.</p>
     */
    private final double halfOuterHeight;

    /**
     * <strong>Performance Cache (Inner):</strong> Stores {@code innerWidth / 2.0}.
     * <p>Used to perform symmetric boundary checks on the hole's X-axis.</p>
     */
    private final double halfInnerWidth;

    /**
     * <strong>Performance Cache (Inner):</strong> Stores {@code innerHeight / 2.0}.
     * <p>Used to perform symmetric boundary checks on the hole's Y-axis.</p>
     */
    private final double halfInnerHeight;

    /**
     * The Minimum Bounding Rectangle (MBR) defined solely by the outer dimensions.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Frame domain centered at {@code (0, 0)}.
     *
     * <p><strong>Deep Protection & Topology Check:</strong></p>
     * Enforces two layers of validation:
     * <ol>
     * <li><strong>Intrinsic:</strong> All physical dimensions must be strictly positive.</li>
     * <li><strong>Relational:</strong> The inner rectangle must be strictly smaller than the outer rectangle
     * along corresponding axes to ensure a valid topology with positive area.</li>
     * </ol>
     *
     * @param innerWidth  Width of the central hole.
     * @param innerHeight Height of the central hole.
     * @param outerWidth  Width of the outer boundary.
     * @param outerHeight Height of the outer boundary.
     * @throws DomainConstraintException If dimensions are non-positive or if {@code inner >= outer}.
     */
    public FrameDomain(double innerWidth, double innerHeight, double outerWidth, double outerHeight) {

        // Phase 1: Intrinsic Validity
        if (innerWidth <= 0) {
            throw new DomainConstraintException("inner width", "must be strictly positive (> 0).");
        }
        if (innerHeight <= 0) {
            throw new DomainConstraintException("inner height", "must be strictly positive (> 0).");
        }
        if (outerHeight <= 0) {
            throw new DomainConstraintException("outer height", "must be strictly positive (> 0).");
        }
        if (outerWidth <= 0) {
            throw new DomainConstraintException("outer width", "must be strictly positive (> 0).");
        }

        // Phase 2: Relational Validity (Topology Consistency)
        // Check if the hole fits inside the container.
        if (innerWidth >= outerWidth || innerHeight >= outerHeight) {
            throw new DomainConstraintException(
                    String.format("Invalid Topology: Inner dimensions (%.2fx%.2f) must be strictly smaller than Outer dimensions (%.2fx%.2f).",
                            innerWidth, innerHeight, outerWidth, outerHeight)
            );
        }

        this.outerWidth = outerWidth;
        this.outerHeight = outerHeight;
        this.innerWidth = innerWidth;
        this.innerHeight = innerHeight;

        // Optimization: Pre-calculate half-dimensions for axial symmetry checks.
        // Moves division cost from Hot Path to constructor.
        this.halfOuterWidth = outerWidth / 2.0;
        this.halfOuterHeight = outerHeight / 2.0;
        this.halfInnerWidth = innerWidth / 2.0;
        this.halfInnerHeight = innerHeight / 2.0;

        // Bounding Box Initialization:
        // Centered at (0,0), defined by outer dimensions.
        this.boundingBox = new Rectangle2D.Double(-outerWidth / 2.0, -outerHeight / 2.0, outerWidth, outerHeight);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates geometric constraints using axial symmetry and boolean logic.
     *
     *
     * <p><strong>Algorithmic Logic (Hot Path):</strong></p>
     * The method determines if a point is <strong>invalid</strong> by checking two conditions:
     * <ul>
     * <li><strong>Outer Violation (OR Logic):</strong> Checks if {@code |x| > outerX OR |y| > outerY}.
     * If true, the point is outside the container.</li>
     * <li><strong>Inner Violation (AND Logic):</strong> Checks if {@code |x| < innerX AND |y| < innerY}.
     * Both conditions must be true for the point to be inside the hole. (e.g., if x is in the hole range but y is not,
     * the point is in the valid frame band).</li>
     * </ul>
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the valid frame area.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Use absolute values to check symmetry against origin (0,0)
        double absX = Math.abs(x);
        double absY = Math.abs(y);

        // 1. Outer Boundary Check (Short-circuit OR)
        // If it breaks ANY outer boundary, it is invalid.
        if (absX > halfOuterWidth || absY > halfOuterHeight) {
            return true;
        }


        // 2. Inner Hole Check (Short-circuit AND)
        // To be strictly inside the hole (invalid), it must be within BOTH inner boundaries.
        // If it is within X bounds but outside Y bounds, it is in the valid "top" or "bottom" frame bar.
        return (absX < halfInnerWidth) && (absY < halfInnerHeight);
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the frame.
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
     * Retrieves the Bounding Box of the outer rectangle.
     *
     * @return A {@link Rectangle2D} covering the outer container.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Frame { Inner width = %.2fm, Inner height = %.2fm, Outer width = %.2fm, Outer height = %.2fm }",
                innerWidth, innerHeight, outerWidth, outerHeight);
    }
}
