package org.agroplanner.domainsystem.model.types;

import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * <p><strong>Concrete Domain Implementation: Ellipse.</strong></p>
 *
 * <p>Represents an elliptical domain centered at the origin {@code (0, 0)}.
 * The shape is defined by its two semi-axes: semi-width (along X) and semi-height (along Y).</p>
 */
public class EllipseDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** Semi-axis along X (often denoted as 'a'). */
    private final double semiWidth;

    /** Semi-axis along Y (often denoted as 'b'). */
    private final double semiHeight;

    /**
     * Optimization: Pre-calculated inverse squares of the semi-axes.
     * Used to replace division with multiplication in the inequality check.
     * invSemiWidthSq = 1 / (a^2)
     * invSemiHeightSq = 1 / (b^2)
     */
    private final double invSemiWidthSq;
    private final double invSemiHeightSq;

    /** The bounding box is a rectangle fully enclosing the ellipse. */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs an Elliptical domain.
     *
     * @param semiWidth  The semi-axis length along the X-axis (radius on X).
     * @param semiHeight The semi-axis length along the Y-axis (radius on Y).
     * @throws DomainConstraintException If either semi-axis is not strictly positive.
     */
    public EllipseDomain(double semiWidth, double semiHeight) {

        // Deep Protection: Integrity check
        if (semiWidth <= 0) {
            throw new DomainConstraintException("semi width", "must be strictly positive (> 0).");
        }
        if (semiHeight <= 0) {
            throw new DomainConstraintException("semi height", "must be strictly positive (> 0).");
        }

        this.semiWidth = semiWidth;
        this.semiHeight = semiHeight;

        // Pre-calculate the inverse squares.
        // Formula transformation: (x^2 / a^2) -> (x^2 * (1/a^2))
        this.invSemiWidthSq = 1.0 / (semiWidth * semiWidth);
        this.invSemiHeightSq = 1.0 / (semiHeight * semiHeight);

        // Bounding Box: Centered at (0,0), total width=2a, total height=2b.
        this.boundingBox = new Rectangle2D.Double(
                -semiWidth,
                -semiHeight,
                semiWidth * 2,
                semiHeight * 2
        );
    }

    // ------------------- DOMAIN CONTRACT IMPLEMENTATION -------------------

    /**
     * Checks if a coordinate {@code (x, y)} lies outside the ellipse.
     *
     * <p><strong>Mathematical Logic:</strong>
     * Uses the standard ellipse inequality: {@code (x²/a²) + (y²/b²) <= 1}.
     * To optimize performance, divisions are replaced by multiplications with pre-calculated inverses.
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return {@code true} if the point is strictly outside the ellipse boundary.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized Equation: (x^2 * invA^2) + (y^2 * invB^2) > 1
        double termX = (x * x) * invSemiWidthSq;
        double termY = (y * y) * invSemiHeightSq;

        return (termX + termY) > 1.0;
    }

    /**
     * Validates an entire individual against the elliptical boundary.
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
     * Retrieves the Bounding Box enclosing the ellipse.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Ellipse { semi-width = %.2f, semi-height = %.2f }", semiWidth, semiHeight);
    }

}
