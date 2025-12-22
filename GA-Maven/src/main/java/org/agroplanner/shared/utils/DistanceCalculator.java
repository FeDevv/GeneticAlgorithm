package org.agroplanner.shared.utils;

import org.agroplanner.gasystem.model.Point;

/**
 * Service component responsible for computing spatial metrics between geometric entities.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Design Pattern:</strong> Designed as an instantiable Service (Bean) rather than a static utility.
 * This structural choice facilitates the <strong>Strategy Pattern</strong>, allowing the distance metric
 * (e.g., Euclidean, Manhattan, Chebyshev) to be swapped via Dependency Injection without modifying
 * the consuming classes (Open/Closed Principle).</li>
 * <li><strong>Testability:</strong> Enables mocking of the distance function during Unit Testing,
 * isolating geometric logic from complex calculation contexts.</li>
 * </ul>
 */
public class DistanceCalculator {

    /**
     * Computes the Euclidean distance (L2 Norm) between the centroids of two points.
     *
     * <p><strong>Implementation Note (Numerical Stability):</strong></p>
     * Utilizes {@link Math#hypot(double, double)} instead of the direct {@code sqrt(dx*dx + dy*dy)} formula.
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
