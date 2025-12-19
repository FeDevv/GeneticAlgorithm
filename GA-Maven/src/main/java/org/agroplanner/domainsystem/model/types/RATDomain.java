package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Right-Angled Triangle.</strong></p>
 *
 * <p>Represents a triangular domain positioned in the first quadrant.
 * The right angle is located at the origin {@code (0, 0)}. The legs of the triangle extend along
 * the positive X-axis (Base) and the positive Y-axis (Height).</p>
 */
public class RATDomain implements Domain {

    // ------------------- FIELDS -------------------

    private final double base;   // Leg along the X-axis
    private final double height; // Leg along the Y-axis

    /**
     * Optimization: Pre-calculated slope of the hypotenuse (height / base).
     * Used to avoid repeated division operations during point validation.
     */
    private final double slope;

    /** The bounding box enclosing the triangle [0, 0, base, height]. */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a Right-Angled Triangle domain.
     *
     * @param base   The length of the leg on the X-axis.
     * @param height The length of the leg on the Y-axis.
     * @throws DomainConstraintException If dimensions are not strictly positive.
     */
    public RATDomain(double base, double height) {

        // Deep Protection: Integrity check
        if (base <= 0) {
            throw new DomainConstraintException("base", "Must be strictly positive.");
        }

        if (height <= 0) {
            throw new DomainConstraintException("height", "Must be strictly positive.");
        }

        this.base = base;
        this.height = height;

        // Pre-calculate the slope ratio.
        this.slope = height / base;

        // Bounding Box: Anchored at (0,0) extending to (base, height).
        this.boundingBox = new Rectangle2D.Double(0, 0, base, height);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the triangle.
     *
     * <p><strong>Geometric Logic:</strong>
     * The point is outside if:
     * <ul>
     * <li>It is to the left of the Y-axis (x < 0).</li>
     * <li>It is below the X-axis (y < 0).</li>
     * <li>It is above the hypotenuse line (y > H - (H/B) * x).</li>
     * </ul>
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is strictly outside the triangular area.
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Axes Constraint: Must be in the first quadrant
        boolean outsideAxes = (x < 0) || (y < 0);
        if (outsideAxes) return true;

        // Hypotenuse Constraint
        // The line equation is y = H - slope * x.
        // If y is greater than that value, the point is "above" the triangle.
        return y > (height - (slope * x));
    }

    /**
     * Validates an entire individual against the triangular boundary.
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
     * Retrieves the Bounding Box [0, 0, base, height].
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
