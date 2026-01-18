package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.DistanceCalculator;

/**
 * Stateless utility responsible for quantifying geometric constraint violations via numerical penalties.
 *
 * <p><strong>Architecture & Math:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Helper / Calculator.</li>
 * <li><strong>Strategy (Soft Constraints):</strong> Implements the <em>Penalty Method</em>. Instead of treating collisions
 * as hard constraints (immediately discarding invalid individuals), this class assigns a scalable numerical cost.
 * This preserves the connectivity of the search space, allowing the algorithm to traverse through slightly invalid
 * states to reach better optima.</li>
 * </ul>
 */
public final class PenaltyHelper {

    /**
     * Private constructor to strictly prevent instantiation.
     */
    private PenaltyHelper() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Computes the scalar penalty for a geometric intersection between two entities.
     *
     * <p><strong>Mathematical Strategy: Quadratic Penalty Function.</strong></p>
     * The penalty is calculated as: P = weight×(overlap)^2.
     *
     * <p><strong>Why Quadratic? (Gradient Dynamics):</strong></p>
     * Compared to a linear penalty (P ∝ overlap), the quadratic approach offers superior evolutionary dynamics:
     * <ul>
     * <li><strong>Micro-Adjustments (Low Gradient):</strong> When overlap is minimal (circles barely touching),
     * the penalty curve is flat. This allows genes to "slide" against each other for dense packing without
     * incurring catastrophic fitness drops.</li>
     * <li><strong>Macro-Correction (High Gradient):</strong> As overlap increases, the penalty spikes non-linearly.
     * This creates immense selection pressure against deep collisions, forcing the population to fix major
     * structural errors immediately.</li>
     * </ul>
     *
     *
     *
     * @param point1             The first geometric entity (source).
     * @param point2             The second geometric entity (target).
     * @param overlapWeight      The hyperparameter scaling the severity of the penalty (Sensitivity).
     * @param distanceCalculator The service used to compute the Euclidean metric.
     * @return A strictly non-negative double: {@code > 0} if overlapping, {@code 0.0} if disjoint or touching.
     */
    public static double calculatePairPenalty(
            Point point1, Point point2,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double radius1 = point1.getRadius();
        double radius2 = point2.getRadius();

        // Contact Threshold: The minimum distance required between centers to avoid intersection.
        double requiredDistance = radius1 + radius2;

        // Metric: Actual Euclidean distance between centroids.
        double actualDistance = distanceCalculator.getDistance(point1, point2);

        // Collision Logic:
        // If the actual distance is less than the sum of radii, the distinct identities overlap.
        if (actualDistance < requiredDistance) {

            // Magnitude: How deep is the penetration?
            double overlap = requiredDistance - actualDistance;

            // Penalty Function: Quadratic growth.
            // Using (overlap * overlap) avoids Math.pow() overhead and provides the desired gradient.
            return (overlap * overlap) * overlapWeight;
        }

        // No collision -> No penalty.
        return 0.0;
    }
}
