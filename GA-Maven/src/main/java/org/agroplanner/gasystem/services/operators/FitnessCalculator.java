package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.services.strategies.OverlapQuadratic;
import org.agroplanner.gasystem.services.strategies.OverlapSpatial;
import org.agroplanner.gasystem.services.strategies.OverlapStrategy;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.List;

/**
 * <p><strong>The Objective Function (Evaluation Engine).</strong></p>
 *
 * <p>This component is responsible for quantifying the quality of a solution.
 * It translates geometric constraints (Boundary limits and Overlaps) into a single scalar value.</p>
 *
 * <p><strong>Algorithmic Strategy: Hybrid Collision Detection.</strong><br>
 * To maximize performance across different scales, this calculator switches strategies based on population size:
 * <ul>
 * <li><strong>Small N (<= 80):</strong> Uses a Quadratic Strategy O(N^2). Faster due to CPU cache locality and zero setup overhead.</li>
 * <li><strong>Large N (> 80):</strong> Uses a Spatial Hashing Strategy (Grid-based) O(N). The overhead of building the grid is amortized by the speed of neighbor lookup.</li>
 * </ul>
 * </p>
 */
public class FitnessCalculator {

    // ------------------- PENALTY CONFIGURATION -------------------

    /**
     * Hard Constraint Penalty.
     * <p>Applied when a point is strictly outside the domain boundaries.
     * The value is deliberately massive to force the algorithm to discard these solutions immediately.</p>
     */
    private static final double DOMAIN_PENALTY = 10000.0;

    /**
     * Soft Constraint Weight.
     * <p>Multiplier for the overlap penalty. A high weight prioritizes separating objects
     * over exploring new configurations.</p>
     */
    private static final double OVERLAP_WEIGHT = 100.0;

    /**
     * The efficiency threshold for strategy switching.
     * <p>Empirically determined point where the setup cost of Spatial Hashing becomes
     * negligible compared to the brute-force checks.</p>
     */
    private static final int HASHING_THRESHOLD = 80;

    // ------------------- DEPENDENCIES -------------------

    private final Domain currentDomain;
    private final DistanceCalculator distanceCalculator;

    // Strategies (Instantiated once to avoid allocation overhead during evaluation loop)
    private final OverlapStrategy quadraticStrategy;
    private final OverlapStrategy spatialStrategy;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the evaluator and prepares the collision strategies.
     *
     * @param domain    The geometric domain (Boundary Constraint).
     * @param maxRadius The maximum possible radius of a point (Required for grid cell sizing in Spatial Strategy).
     */
    public FitnessCalculator(Domain domain, double maxRadius) {
        this.currentDomain = domain;
        this.distanceCalculator = new DistanceCalculator(); // Helper for Euclidean math

        // Strategy Pattern Initialization:
        // We prepare both engines so the toggle can happen dynamically at runtime based on the individual's size.
        this.quadraticStrategy = new OverlapQuadratic();
        this.spatialStrategy = new OverlapSpatial(maxRadius);
    }

    // ------------------- EVALUATION LOGIC -------------------

    /**
     * Computes the fitness score for a candidate solution.
     *
     * <p><strong>Formula:</strong> {@code Fitness = 1.0 / (1.0 + Total_Penalty)}</p>
     * <ul>
     * <li><strong>0 Penalty:</strong> Fitness = 1.0 (Perfect Solution).</li>
     * <li><strong>High Penalty:</strong> Fitness approaches 0.0.</li>
     * </ul>
     *
     * @param individual The solution to evaluate.
     * @return A value between 0.0 (worst) and 1.0 (best).
     */
    public double getFitness(Individual individual) {
        List<Point> chromosomes = individual.getChromosomes();
        int n = chromosomes.size();
        double totalPenalty = 0.0;

        // DOMAIN CONSTRAINTS (Boundary Check) - Complexity: O(N)
        // Hard Constraint: Points must be strictly inside the shape.
        for (Point p : chromosomes) {
            // Optimization: The domain calculates geometry efficiently (e.g., radius check).
            if (currentDomain.isPointOutside(p.getX(), p.getY())) {
                totalPenalty += DOMAIN_PENALTY;
            }
        }

        // OVERLAP CONSTRAINTS (Collision Check) - Complexity: Hybrid
        // Soft Constraint: Points should not overlap.
        if (n <= HASHING_THRESHOLD) {
            // BRUTE FORCE MODE:
            // For small datasets, nested loops are faster than allocating a HashGrid.
            totalPenalty += quadraticStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        } else {
            // SPATIAL HASHING MODE:
            // For large datasets, we use a grid to check only nearby neighbors.
            totalPenalty += spatialStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        }

        // NORMALIZATION (Penalty -> Fitness)
        // Converts the unbounded penalty score (0 to infinity) into a normalized fitness score (1 to 0).
        // The "+ 1.0" in the denominator avoids division by zero.
        return 1.0 / (1.0 + totalPenalty);
    }

}
