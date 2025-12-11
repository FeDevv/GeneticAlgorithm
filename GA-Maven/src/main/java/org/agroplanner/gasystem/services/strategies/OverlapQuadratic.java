package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.services.helpers.PenaltyHelper;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.List;

/**
 * <p><strong>Collision Strategy: Brute Force (Quadratic).</strong></p>
 *
 * <p>This implementation checks every point against every other point to find overlaps.
 * While it has a complexity of <strong>O(N²)</strong>, it is the preferred strategy for small populations (N < Threshold)
 * because:</p>
 * <ul>
 * <li>It has <strong>zero setup overhead</strong> (no need to build grids or trees).</li>
 * <li>It benefits from high <strong>CPU Cache Locality</strong> (sequential access to the list).</li>
 * </ul>
 */
public class OverlapQuadratic implements OverlapStrategy{

    /**
     * Computes the total overlap penalty using a nested loop approach.
     *
     * <p><strong>Algorithmic Complexity:</strong> O(N * (N-1) / 2) ≈ O(N²).</p>
     *
     * @param chromosomes        The list of points to evaluate.
     * @param overlapWeight      The penalty weight.
     * @param distanceCalculator The Euclidean distance utility.
     * @return The accumulated penalty score.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double penalty = 0.0;
        int n = chromosomes.size();

        // Outer Loop: Select the reference point P_i.
        for (int i = 0; i < n; i++) {
            Point referencePoint = chromosomes.get(i);

            // Inner Loop: Compare P_i against all subsequent points P_j.
            // Starting at j = i + 1 is a critical optimization:
            // 1. Prevents Self-Comparison (checking P_i vs P_i).
            // 2. Prevents Double Counting (checking P_i vs P_j AND P_j vs P_i).
            // This effectively halves the number of checks required.
            for (int j = i + 1; j < n; j++) {
                Point neighborPoint = chromosomes.get(j);

                // Delegate the math to the helper (Separation of Concerns).
                penalty += PenaltyHelper.calculatePairPenalty(referencePoint, neighborPoint, overlapWeight, distanceCalculator);

            }
        }
        return penalty;
    }
}
