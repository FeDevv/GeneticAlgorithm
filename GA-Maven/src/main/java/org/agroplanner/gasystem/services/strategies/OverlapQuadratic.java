package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.services.helpers.PenaltyHelper;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.List;

/**
 * Concrete implementation of the collision strategy using a <strong>Brute-Force (Quadratic)</strong> approach.
 *
 * <p><strong>Architecture & Hardware Efficiency:</strong></p>
 * Although asymptotically slower (O(N^2)), this strategy is the optimal choice for small datasets (Small N).
 * <ul>
 * <li><strong>Memory Access Pattern:</strong> It iterates linearly over an {@link java.util.ArrayList}, maximizing
 * <strong>CPU Cache Locality</strong> (Spatial Locality). Modern CPUs can pre-fetch data efficiently.</li>
 * <li><strong>Zero Overhead:</strong> Unlike Spatial Hashing, it requires zero memory allocation for auxiliary structures
 * (no grids, no buckets) and has no setup time.</li>
 * </ul>
 */
public class OverlapQuadratic implements OverlapStrategy{

    /**
     * Calculates the total penalty by pairwise comparison.
     *
     * <p><strong>Algorithmic Logic (Triangular Iteration):</strong></p>
     * The nested loops iterate only over the unique pairs of the set (Combinations C(N, 2)).
     * <br>
     * Total Checks = \sum_{i=0}^{N-1} (N - 1 - i) = {N(N-1)}/{2}
     * <br>
     * By starting the inner loop at {@code j = i + 1}, we strictly avoid:
     * <ol>
     * <li><strong>Reflexivity:</strong> Checking an object against itself (i=j).</li>
     * <li><strong>Symmetry Redundancy:</strong> Checking A vs B and then B vs A.</li>
     * </ol>
     *
     * @param chromosomes        The dense list of points.
     * @param overlapWeight      The penalty multiplier.
     * @param distanceCalculator The distance metric service.
     * @return The scalar penalty sum.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double penalty = 0.0;
        int n = chromosomes.size();

        // Outer Loop: Reference Source
        for (int i = 0; i < n; i++) {
            Point referencePoint = chromosomes.get(i);

            // Inner Loop: Comparison Target
            // Starts at i+1 to ensure we only traverse the "Upper Triangle" of the interaction matrix.
            for (int j = i + 1; j < n; j++) {
                Point neighborPoint = chromosomes.get(j);

                // Delegation: Compute penalty for this specific pair.
                penalty += PenaltyHelper.calculatePairPenalty(referencePoint, neighborPoint, overlapWeight, distanceCalculator);

            }
        }
        return penalty;
    }
}
