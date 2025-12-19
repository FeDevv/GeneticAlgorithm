package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Rectangular Frame.</strong></p>
 *
 * <p>Represents a non-convex domain defined by the area between two concentric rectangles centered at {@code (0, 0)}.
 * Valid points must lie inside the outer rectangle but outside the inner rectangle (the hole).</p>
 */
public class FrameDomain implements Domain {

    // ------------------- FIELDS -------------------

    // Original dimensions (kept for toString and external info)
    private final double innerWidth;
    private final double innerHeight;
    private final double outerWidth;
    private final double outerHeight;

    /**
     * Optimization: Pre-calculated half-dimensions (boundaries).
     * Used to avoid repeated division operations in the {@link #isPointOutside(double, double)} loop.
     */
    private final double halfOuterWidth, halfOuterHeight;
    private final double halfInnerWidth, halfInnerHeight;

    /** The bounding box is defined by the outer rectangle. */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a Frame domain.
     *
     * @param innerWidth  Width of the central hole.
     * @param innerHeight Height of the central hole.
     * @param outerWidth  Width of the outer boundary.
     * @param outerHeight Height of the outer boundary.
     * @throws DomainConstraintException If dimensions are non-positive or if inner >= outer (invalid topology).
     */
    public FrameDomain(double innerWidth, double innerHeight, double outerWidth, double outerHeight) {

        // Deep Protection: Intrinsic Validity
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

        // Deep Protection: Topology Consistency
        // The inner rectangle must be strictly contained within the outer one.
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

        // Pre-calculate boundaries relative to origin (0,0)
        this.halfOuterWidth = outerWidth / 2.0;
        this.halfOuterHeight = outerHeight / 2.0;
        this.halfInnerWidth = innerWidth / 2.0;
        this.halfInnerHeight = innerHeight / 2.0;

        // Bounding Box: Centered at (0,0)
        this.boundingBox = new Rectangle2D.Double(-outerWidth / 2.0, -outerHeight / 2.0, outerWidth, outerHeight);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the frame area.
     * <p>
     * Logic: A point is invalid if:
     * <ul>
     * <li>It is outside the outer rectangle (Math.abs(coord) > half_outer_dimension).</li>
     * <li>OR</li>
     * <li>It is inside the inner hole (Math.abs(coord) < half_inner_dimension).</li>
     * </ul>
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is invalid.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Use absolute values to check symmetry against origin (0,0)
        double absX = Math.abs(x);
        double absY = Math.abs(y);

        // 1. Outer Boundary Check
        // If |x| > width/2 OR |y| > height/2, it is outside the outer box.
        if (absX > halfOuterWidth || absY > halfOuterHeight) {
            return true;
        }

        // 2. Inner Hole Check
        // If we are here, we are inside the outer box.
        // To be in the hole (invalid), we must be strictly inside BOTH inner dimensions.
        // i.e., |x| < innerWidth/2 AND |y| < innerHeight/2

        return (absX < halfInnerWidth) && (absY < halfInnerHeight);
    }

    /**
     * Validates an entire individual against the frame constraints.
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
     * Retrieves the Bounding Box of the outer rectangle.
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
