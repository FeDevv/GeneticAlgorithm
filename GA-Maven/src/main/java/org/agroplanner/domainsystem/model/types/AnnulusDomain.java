package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of an Annulus (Ring) geometric domain.
 *
 * <p><strong>Architecture & Topology:</strong></p>
 * <ul>
 * <li><strong>Geometry:</strong> Represents a <strong>non-convex</strong> domain defined by the region between two
 * concentric circles centered at {@code (0, 0)}. Valid points must reside within the "fleshy" part of the ring.</li>
 * <li><strong>Constraint Logic:</strong> Implements a dual-bound inequality check. A point is valid if and only if
 * its distance from the origin is greater than or equal to the inner radius AND less than or equal to the outer radius.</li>
 * <li><strong>Sampling Efficiency:</strong> The acceptance rate for Rejection Sampling depends heavily on the ring's thickness.
 * A very thin ring (where R_{in} is approx equal to R_{out}) will result in a lower acceptance rate as the valid area
 * becomes a small fraction of the bounding box.</li>
 * </ul>
 */
public class AnnulusDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** Radius of the inner exclusion zone (the hole). */
    private final double innerRadius;

    /** Radius of the outer containment boundary. */
    private final double outerRadius;

    /**
     * <strong>Performance Cache:</strong> Stores {@code innerRadius^2}.
     * <p>Used as the lower bound threshold for the squared distance check.</p>
     */
    private final double innerRadiusSq;

    /**
     * <strong>Performance Cache:</strong> Stores {@code outerRadius^2}.
     * <p>Used as the upper bound threshold for the squared distance check.</p>
     */
    private final double outerRadiusSq;

    /**
     * The Minimum Bounding Rectangle (MBR) defined solely by the outer radius.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Annulus domain centered at {@code (0, 0)}.
     *
     * <p><strong>Deep Protection & Topology Check:</strong></p>
     * Enforces both intrinsic validity (radii must be positive) and relational validity (Topology Consistency).
     * Specifically, it rejects geometries where the inner radius is not strictly smaller than the outer radius,
     * as this would define a null or negative valid area.
     *
     * @param innerRadius The radius of the central hole.
     * @param outerRadius The radius of the outer boundary.
     * @throws DomainConstraintException If radii are non-positive or if {@code innerRadius >= outerRadius}.
     */
    public AnnulusDomain(double innerRadius, double outerRadius) {

        // Phase 1: Intrinsic Validity
        if (innerRadius <= 0) {
            throw new DomainConstraintException("innerRadius", "must be strictly positive (> 0).");
        }
        if (outerRadius <= 0) {
            throw new DomainConstraintException("outerRadius", "must be strictly positive (> 0).");
        }

        // Phase 2: Relational Validity (Cross-Field Validation)
        // Ensure the ring has a positive thickness.
        if (innerRadius >= outerRadius) {
            throw new DomainConstraintException(
                    String.format("Invalid Topology: Inner radius (%.2f) must be strictly smaller than Outer radius (%.2f).",
                            innerRadius, outerRadius)
            );
        }

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        // Optimization: Pre-calculate squared thresholds to avoid sqrt() in Hot Path.
        this.innerRadiusSq = innerRadius * innerRadius;
        this.outerRadiusSq = outerRadius * outerRadius;

        // Bounding Box Initialization:
        // Covers the entire outer circle: [-R_out, -R_out] to [R_out, R_out].
        this.boundingBox = new Rectangle2D.Double(
                -outerRadius,
                -outerRadius,
                outerRadius * 2,
                outerRadius * 2
        );
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Evaluates geometric constraints using Squared Euclidean Distance.
     *
     * <p><strong>Algorithmic Logic (Hot Path):</strong></p>
     * Checks if the point falls outside the valid "Band".
     * <br>
     *
     * Validity Condition: {@code innerRadiusSq <= (x² + y²) <= outerRadiusSq}.
     * <br>
     * Therefore, a point is <strong>invalid (outside)</strong> if:
     * <ul>
     * <li>{@code (x² + y²) > outerRadiusSq} (Too far from center)</li>
     * <li>OR</li>
     * <li>{@code (x² + y²) < innerRadiusSq} (Inside the hole)</li>
     * </ul>
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the valid ring area.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Squared Euclidean distance from origin (avoiding sqrt)
        double distSq = x * x + y * y;

        // Check 1: Is it beyond the outer limit?
        boolean isOutsideOuter = distSq > outerRadiusSq;

        // Check 2: Is it inside the exclusion zone (hole)?
        boolean isInsideHole = distSq < innerRadiusSq;

        return isOutsideOuter || isInsideHole;
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the annulus.
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
     * Retrieves the Bounding Box of the outer boundary.
     *
     * <p><strong>Rejection Sampling Note:</strong></p>
     * Random points generated within this box may fall into the central "hole".
     * The domain validation logic ({@link #isPointOutside}) acts as the filter to discard these invalid points.
     *
     * @return A {@link Rectangle2D} covering the outer circle.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Annulus { inner radius = %.2fm, outer radius = %.2fm }", innerRadius, outerRadius);
    }
}