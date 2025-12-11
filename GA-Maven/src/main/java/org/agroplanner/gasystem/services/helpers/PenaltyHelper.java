package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.DistanceCalculator;

/**
 * <p><strong>Utility for Constraint Violation Quantification.</strong></p>
 *
 * <p>This class encapsulates the mathematical logic to calculate penalties arising from
 * geometric collisions (overlaps). It isolates the "Soft Constraint" logic from the
 * main fitness evaluation loop.</p>
 *
 * <p>Utility Class (Stateless, Static methods).</p>
 */
public final class PenaltyHelper {

    /**
     * Private constructor to prevent instantiation.
     */
    private PenaltyHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Calculates the penalty score for a collision between two points.
     *
     * <p><strong>Mathematical Strategy: Quadratic Penalty.</strong><br>
     * Instead of a linear penalty, this method applies a quadratic function: {@code (overlap² * weight)}.
     * This ensures that:</p>
     * <ul>
     * <li><strong>Deep Overlaps</strong> incur a massive penalty, forcing the algorithm to fix them immediately.</li>
     * <li><strong>Slight Overlaps</strong> incur a small penalty, allowing for fine-tuning movements ("sliding").</li>
     * </ul>
     *
     * @param point1             The first geometric entity.
     * @param point2             The second geometric entity.
     * @param overlapWeight      The tuning parameter scaling the severity of the penalty.
     * @param distanceCalculator The utility used to compute Euclidean distance.
     * @return A positive double representing the penalty if an overlap exists; {@code 0.0} otherwise.
     */
    public static double calculatePairPenalty(
            Point point1, Point point2,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double radius1 = point1.getRadius();
        double radius2 = point2.getRadius();

        // The minimum distance required between centers to avoid touching.
        double requiredDistance = radius1 + radius2;

        // The actual Euclidean distance between centers.
        double actualDistance = distanceCalculator.getDistance(point1, point2);

        // Collision Detection:
        // If actual distance < sum of radii, the circles are overlapping.
        if (actualDistance < requiredDistance) {

            // Calculate the magnitude of the violation (how much they overlap).
            double overlap = requiredDistance - actualDistance;

            // Apply Quadratic Penalty: (overlap * overlap) * weight
            // Using overlap^2 makes the penalty gradient smoother and steeper for bad violations.
            return (overlap * overlap) * overlapWeight;
        }

        // No collision -> No penalty.
        return 0.0;
    }
}
