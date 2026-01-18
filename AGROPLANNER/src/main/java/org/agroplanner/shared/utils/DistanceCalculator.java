package org.agroplanner.shared.utils;

import org.agroplanner.gasystem.model.Point;

/**
 * Service component responsible for computing spatial metrics between geometric entities.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Designed as an instantiable Service (Bean) rather than a static utility.
 * This structural choice facilitates the <strong>Strategy Pattern</strong>. By injecting this class (or an interface)
 * into consumers, the distance algorithm (e.g., Euclidean, Manhattan) can be swapped or mocked during
 * Unit Testing without modifying the dependent logic.</li>
 * <li><strong>Responsibility:</strong> Pure calculation logic, decoupled from the state of the entities being measured.</li>
 * </ul>
 */
public class DistanceCalculator {

    /**
     * Computes the Euclidean distance (L2 Norm) between the coordinates of two points.
     *
     * <p><strong>Implementation Note (Numerical Stability):</strong></p>
     * This implementation utilizes {@link Math#hypot(double, double)} instead of the direct
     * {@code sqrt(dx*dx + dy*dy)} formula.
     * <br>
     * While strictly computationally heavier, {@code hypot} guarantees protection against
     * <strong>intermediate overflow or underflow</strong>. It ensures correct results even when intermediate
     * squared values would exceed {@code Double.MAX_VALUE} or vanish below {@code Double.MIN_VALUE},
     * strictly adhering to IEEE 754 floating-point standards.
     *
     * @param p1 The source geometric point.
     * @param p2 The target geometric point.
     * @return The scalar distance between p1 and p2.
     */
    public double getDistance(Point p1, Point p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        return Math.hypot(dx, dy);
    }
}
