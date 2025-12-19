package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Annulus (Ring).</strong></p>
 *
 * <p>Represents a non-convex domain defined by the area between two concentric circles centered at {@code (0, 0)}.
 * Valid points must lie <em>inside</em> the outer circle but <em>outside</em> the inner circle (the hole).</p>
 */
public class AnnulusDomain implements Domain {

    // ------------------- FIELDS -------------------

    private final double innerRadius;
    private final double outerRadius;

    /**
     * Optimization: Squared radii are pre-calculated to avoid repetitive multiplication
     * during the {@link #isPointOutside(double, double)} check.
     */
    private final double innerRadiusSq;
    private final double outerRadiusSq;

    /** The bounding box is defined solely by the outer circle. */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs an Annulus domain.
     *
     * @param innerRadius The radius of the central hole.
     * @param outerRadius The radius of the outer boundary.
     * @throws DomainConstraintException If radii are non-positive or if {@code innerRadius >= outerRadius}.
     */
    public AnnulusDomain(double innerRadius, double outerRadius) {

        // Deep Protection: Intrinsic validity of single values
        if (innerRadius <= 0) {
            throw new DomainConstraintException("innerRadius", "must be strictly positive (> 0).");
        }
        if (outerRadius <= 0) {
            throw new DomainConstraintException("outerRadius", "must be strictly positive (> 0).");
        }
        // Deep Protection: Topology Consistency
        // Essential condition: The inner hole must be strictly smaller than the outer boundary
        // to form a valid ring area.
        if (innerRadius >= outerRadius) {
            throw new DomainConstraintException(
                    String.format("Invalid Topology: Inner radius (%.2f) must be strictly smaller than Outer radius (%.2f).",
                            innerRadius, outerRadius)
            );
        }

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        // Pre-calculate squares
        this.innerRadiusSq = innerRadius * innerRadius;
        this.outerRadiusSq = outerRadius * outerRadius;

        // The bounding box encloses the outer circle: [-R_out, -R_out] to [R_out, R_out].
        this.boundingBox = new Rectangle2D.Double(
                -outerRadius,
                -outerRadius,
                outerRadius * 2,
                outerRadius * 2
        );
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the annulus area.
     * <p>
     * Logic: A point is invalid (outside) if:
     * <ul>
     * <li>It is outside the outer container (Distance > OuterRadius).</li>
     * <li>OR</li>
     * <li>It is inside the inner hole (Distance < InnerRadius).</li>
     * </ul>
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is invalid.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Squared Euclidean distance from origin
        double distSq = x * x + y * y;

        // 1. Outer Boundary Check: Must be inside the outer circle.
        boolean isOutsideOuter = distSq > outerRadiusSq;

        // 2. Inner Hole Check: Must be outside the inner circle.
        boolean isInsideHole = distSq < innerRadiusSq;

        // Invalid if it breaks either boundary condition
        return isOutsideOuter || isInsideHole;
    }

    /**
     * Validates an entire individual against the annulus constraints.
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
     * Used for initial random generation (points generated in the hole will be discarded by validation).
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