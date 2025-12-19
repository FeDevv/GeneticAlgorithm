package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Rectangle.</strong></p>
 *
 * <p>Represents a rectangular domain centered at the origin {@code (0, 0)}.
 * The domain spans from {@code [-width/2, -height/2]} to {@code [width/2, height/2]}.</p>
 */
public class RectangleDomain implements Domain {

    // ------------------- FIELDS -------------------

    private final double width;
    private final double height;

    /**
     * Optimization: Pre-calculated half-dimensions.
     * Used to leverage symmetry checks (Math.abs) instead of range checks.
     */
    private final double halfWidth;
    private final double halfHeight;

    /**
     * For a rectangular domain, the bounding box coincides exactly with the domain boundaries.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs a Rectangular domain centered at {@code (0, 0)}.
     *
     * @param width  The total width of the rectangle.
     * @param height The total height of the rectangle.
     * @throws DomainConstraintException If dimensions are not strictly positive.
     */
    public RectangleDomain(double width, double height) {

        // Deep Protection: Integrity check
        if (width <= 0) {
            throw new DomainConstraintException("width", "Must be strictly positive.");
        }

        if (height <= 0) {
            throw new DomainConstraintException("height", "Must be strictly positive.");
        }

        // I campi sono final, garantendo l'immutabilità del dominio.
        this.width = width;
        this.height = height;

        // Optimization: Pre-calculate boundaries relative to origin
        this.halfWidth = width / 2.0;
        this.halfHeight = height / 2.0;

        // Bounding Box: Top-Left at (-w/2, -h/2)
        this.boundingBox = new Rectangle2D.Double(-width/2, -height/2, width, height);
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the rectangle.
     *
     * <p><strong>Optimization:</strong>
     * Since the rectangle is centered at (0,0), a point is outside if its absolute distance
     * from the center along an axis exceeds half the dimension of that axis.
     * Formula: {@code |x| > width/2 || |y| > height/2}
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is strictly outside the boundaries.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Check using symmetry:
        // Instead of: x < -half || x > half
        // We do: Math.abs(x) > half
        return Math.abs(x) > halfWidth || Math.abs(y) > halfHeight;
    }

    /**
     * Validates an entire individual against the rectangular boundary.
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
     * Retrieves the Bounding Box.
     * For this specific domain type, the Bounding Box is geometrically identical to the domain itself.
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
