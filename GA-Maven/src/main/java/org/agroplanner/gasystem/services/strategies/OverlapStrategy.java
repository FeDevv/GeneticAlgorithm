package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.List;

/**
 * <p><strong>Strategy Interface for Collision Detection.</strong></p>
 *
 * <p>This interface defines the contract for algorithms responsible for quantifying geometric overlaps.
 * It implements the <strong>Strategy Pattern</strong>, allowing the {@code FitnessCalculator} to dynamically
 * switch between different implementations (e.g., O(N²) Brute Force vs. O(N) Spatial Hashing)
 * based on the problem size (population density).</p>
 */
public interface OverlapStrategy {

    /**
     * Computes the total penalty resulting from overlapping points in the chromosome list.
     *
     * @param chromosomes        The list of geometric points (genes) to evaluate.
     * @param overlapWeight      The scalar weight applied to the raw overlap magnitude.
     * Higher weights make collisions more "expensive" for the evolution.
     * @param distanceCalculator The utility used to compute Euclidean distances between points.
     * @return The total accumulated penalty score (0.0 means no overlaps).
     */
    double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    );
}
