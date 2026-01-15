package org.agroplanner.domainsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.DomainConstraintException;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Concrete implementation of an Elliptical geometric domain centered at the Cartesian origin {@code (0, 0)}.
 *
 * <p><strong>Architecture & Performance:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Concrete Strategy for the {@link Domain} interface.</li>
 * <li><strong>Arithmetic Optimization:</strong> This class implements a specific optimization for Floating-Point arithmetic.
 * Since the canonical ellipse equation involves division, and division is a CPU-intensive operation compared to multiplication,
 * this implementation pre-calculates the multiplicative inverses of the semi-axes. This transforms the boundary check
 * into a pure multiplication-addition sequence.</li>
 * <li><strong>Sampling Efficiency:</strong> Similar to the circular domain, the ratio of the elliptical area (pi•a•b)
 * to its bounding rectangle (4•a•b) is constant at pi/4 (~78.5%), ensuring efficient Rejection Sampling.</li>
 * </ul>
 */
public class EllipseDomain implements Domain {

    // ------------------- FIELDS -------------------

    /** The semi-axis length along the X-axis (commonly denoted as 'a'). */
    private final double semiWidth;

    /** The semi-axis length along the Y-axis (commonly denoted as 'b'). */
    private final double semiHeight;

    /**
     * <strong>Performance Cache:</strong> Stores {@code 1.0 / (semiWidth^2)}.
     * <p>Used to replace the divisor 'a^2' in the ellipse equation with a multiplier.</p>
     */
    private final double invSemiWidthSq;

    /**
     * <strong>Performance Cache:</strong> Stores {@code 1.0 / (semiHeight^2)}.
     * <p>Used to replace the divisor 'b^2' in the ellipse equation with a multiplier.</p>
     */
    private final double invSemiHeightSq;

    /**
     * The Minimum Bounding Rectangle (MBR) fully enclosing the ellipse.
     */
    private final Rectangle2D boundingBox;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes a new Elliptical domain.
     *
     * <p><strong>Deep Protection:</strong></p>
     * Validates that both semi-axes are strictly positive to prevent degenerate geometry or division-by-zero errors
     * during the inverse calculation phase.
     *
     * @param semiWidth  The radius along the X-axis ($a$).
     * @param semiHeight The radius along the Y-axis ($b$).
     * @throws DomainConstraintException If dimensions are {@code <= 0}.
     */
    public EllipseDomain(double semiWidth, double semiHeight) {

        // Invariant Enforcement
        if (semiWidth <= 0) {
            throw new DomainConstraintException("semi width", "must be strictly positive (> 0).");
        }
        if (semiHeight <= 0) {
            throw new DomainConstraintException("semi height", "must be strictly positive (> 0).");
        }

        this.semiWidth = semiWidth;
        this.semiHeight = semiHeight;

        // Optimization: Pre-calculate inverse squares.
        // Computational Cost Shift: We pay the cost of division twice here (construction time),
        // to save millions of divisions later during fitness evaluation.
        this.invSemiWidthSq = 1.0 / (semiWidth * semiWidth);
        this.invSemiHeightSq = 1.0 / (semiHeight * semiHeight);

        // Bounding Box Initialization:
        // Centered at (0,0), total width=2a, total height=2b.
        this.boundingBox = new Rectangle2D.Double(
                -semiWidth,
                -semiHeight,
                semiWidth * 2,
                semiHeight * 2
        );
    }

    /**
     * Evaluates geometric constraints using the optimized canonical ellipse equation.
     *
     *
     * <p><strong>Algorithmic Logic (Hot Path):</strong></p>
     * The standard inequality is: {x^2}/{a^2} + {y^2}/{b^2} > 1.
     * <br>
     * This implementation uses the optimized form:
     * x^2•(1/a^2) + y^2•(1/b^2) > 1.
     * <br>
     * By using the pre-calculated inverses, the logic avoids division instructions entirely.
     *
     * @param x The Cartesian X-coordinate.
     * @param y The Cartesian Y-coordinate.
     * @return {@code true} if the point lies strictly outside the elliptical boundary.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Optimized check: Multiply by inverse square instead of dividing by square.
        double termX = (x * x) * invSemiWidthSq;
        double termY = (y * y) * invSemiHeightSq;

        return (termX + termY) > 1.0;
    }

    /**
     * Validates the spatial integrity of a candidate solution.
     *
     * <p><strong>Complexity:</strong> O(N), where N is the number of points.</p>
     *
     * @param individual The candidate solution.
     * @return {@code true} if all points fall within the ellipse.
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
     * Retrieves the Bounding Box enclosing the ellipse defined as {@code [-a, -b]} to {@code [a, b]}.
     *
     * @return The immutable boundary definition.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return String.format("Ellipse { semi-width = %.2fm, semi-height = %.2fm }", semiWidth, semiHeight);
    }

}
