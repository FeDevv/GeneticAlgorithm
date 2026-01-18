package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.List;

/**
 * Defines the contract for collision detection algorithms using the <strong>Strategy Pattern</strong>.
 *
 * <p><strong>Architecture & Performance:</strong></p>
 * This interface decouples the <em>definition</em> of overlap calculation from its <em>implementation</em>.
 * It enables <strong>Algorithmic Polymorphism</strong>, allowing the system to select the most efficient
 * computational complexity (Time Complexity vs Space Complexity trade-off) at runtime.
 * <ul>
 * <li>For small $N$, a quadratic approach (O(N^2)) might be faster due to lower constant factors.</li>
 * <li>For large $N$, a linear approach (O(N)) using spatial indexing is required to maintain scalability.</li>
 * </ul>
 */
public interface OverlapStrategy {

    /**
     * Quantifies the total geometric conflict within a set of points.
     *
     * <p><strong>Contract:</strong></p>
     * Implementations must iterate through the population of points and identify pairs that violate
     * the "hard sphere" constraint (distance < sum of radii). The severity of the violation is then
     * scaled by the {@code overlapWeight}.
     *
     * @param chromosomes        The list of genes (Points) representing the candidate solution.
     * @param overlapWeight      The penalty multiplier (Sensitivity). A high weight creates a steep
     * fitness landscape gradient, forcing the solver to prioritize separation.
     * @param distanceCalculator The service for Euclidean metric computation.
     * @return A non-negative double representing the cumulative penalty. Returns {@code 0.0} if the configuration is valid (collision-free).
     */
    double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    );
}
