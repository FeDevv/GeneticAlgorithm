package org.agroplanner.shared.utils;

import org.agroplanner.gasystem.model.Point;

/**
 * <p><strong>Geometric Utility: Distance Engine.</strong></p>
 *
 * <p>This component encapsulates the logic for calculating spatial separation between points.
 * It is designed as an instantiable service (rather than a static utility) to allow for
 * future dependency injection or strategy swapping (e.g., changing from Euclidean to Manhattan distance).</p>
 */
public class DistanceCalculator {

    /**
     * Computes the <strong>Euclidean Distance</strong> between the centers of two points.
     * <p>
     * Formula: {@code sqrt((x2-x1)^2 + (y2-y1)^2)}
     * </p>
     *
     * @param p1 The first point (source).
     * @param p2 The second point (target).
     * @return The absolute distance as a double.
     */
    public double getDistance(Point p1, Point p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();

        // Implementation Choice: Math.hypot()
        // While slightly slower than explicit sqrt(dx*dx + dy*dy), Math.hypot protects against
        // intermediate overflow or underflow constraints when dealing with very large or very small coordinates.
        // It guarantees better numerical stability.
        return Math.hypot(dx, dy);
    }
}
