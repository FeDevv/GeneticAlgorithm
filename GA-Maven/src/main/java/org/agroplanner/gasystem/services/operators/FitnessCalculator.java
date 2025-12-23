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
 * Component responsible for the <strong>Evaluation Phase (Objective Function)</strong>.
 *
 * <p><strong>Architecture & Performance:</strong></p>
 * This class translates the physical state of a solution into a scalar "Fitness Score".
 * It implements a <strong>Hybrid Algorithm</strong> to optimize collision detection based on data scale:
 * <ul>
 * <li><strong>Small Scale (N <= 80):</strong> Uses a <em>Quadratic Strategy</em> (O(N^2)).
 * While asymptotically slower, for small datasets the contiguous memory access (CPU Cache Locality)
 * and zero setup overhead make it faster than complex spatial structures.</li>
 * <li><strong>Large Scale (N > 80):</strong> Switches to a <em>Spatial Hashing Strategy</em> (O(N) average).
 * Uses a grid-based lookup to check only local neighbors. The overhead of building the grid is amortized
 * by the massive reduction in distance checks.</li>
 * </ul>
 */
public class FitnessCalculator {

    // ------------------- PENALTY CONFIGURATION -------------------

    /**
     * <strong>Hard Constraint Weight:</strong>
     * Applied when a point violates the fundamental domain geometry (e.g., outside the fence).
     * The magnitude is effectively infinite (10^4) to create a "Death Valley" in the fitness landscape,
     * ensuring such individuals have near-zero probability of survival.
     */
    private static final double DOMAIN_PENALTY = 10000.0;

    /**
     * <strong>Soft Constraint Weight:</strong>
     * Multiplier for the overlap magnitude. A value of 100.0 creates a steep gradient,
     * guiding the algorithm to resolve collisions quickly while still allowing minor overlaps
     * during the intermediate exploration phases.
     */
    private static final double OVERLAP_WEIGHT = 100.0;

    /**
     * The empirical crossover point for strategy switching.
     * <p>Benchmarks show that below 80 items, the cost of allocating `ArrayLists` for Grid Buckets
     * exceeds the cost of performing {N(N-1)}/{2} distance checks.</p>
     */
    private static final int HASHING_THRESHOLD = 80;

    // ------------------- DEPENDENCIES -------------------

    private final Domain currentDomain;
    private final DistanceCalculator distanceCalculator;

    // Strategy Pattern: Pre-instantiated engines to avoid allocation in the hot loop.
    private final OverlapStrategy quadraticStrategy;
    private final OverlapStrategy spatialStrategy;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the evaluation engine.
     *
     * @param domain    The geometric boundary definition.
     * @param maxRadius The maximum possible radius of a plant. Crucial for the Spatial Strategy
     * to determine the correct cell size.
     */
    public FitnessCalculator(Domain domain, double maxRadius) {
        this.currentDomain = domain;
        this.distanceCalculator = new DistanceCalculator(); // Helper for Euclidean math

        // Strategy Initialization:
        // We prepare both engines upfront. The choice of which to use is made dynamically per individual.
        this.quadraticStrategy = new OverlapQuadratic();
        this.spatialStrategy = new OverlapSpatial(maxRadius);
    }

    // ------------------- EVALUATION LOGIC -------------------

    /**
     * Calculates the normalized fitness score for a candidate solution.
     *
     * <p><strong>Transformation Logic (Inverse Normalization):</strong></p>
     * Converts the unbounded penalty score P ∈ [0, ∞) into a normalized fitness F ∈ (0, 1].
     * <br>
     * F = {1.0}/{1.0 + P_{total}}
     * <ul>
     * <li>If P = 0 (Perfect solution) ⇒ F = 1.0.</li>
     * <li>If P → ∞ (Heavy violations) ⇒ F → 0.0.</li>
     * </ul>
     *
     * @param individual The candidate solution (Genotype).
     * @return The fitness score (Phenotype).
     */
    public double getFitness(Individual individual) {
        List<Point> chromosomes = individual.getChromosomes();
        int n = chromosomes.size();
        double totalPenalty = 0.0;

        // 1. DOMAIN CONSTRAINTS (Hard Boundaries)
        // Complexity: O(N) linear scan.
        for (Point p : chromosomes) {
            // Check geometric containment.
            if (currentDomain.isPointOutside(p.getX(), p.getY())) {
                totalPenalty += DOMAIN_PENALTY;
            }
        }

        // 2. OVERLAP CONSTRAINTS (Soft Collisions)
        // Complexity: Adaptive (O(N^2) or O(N)).
        if (n <= HASHING_THRESHOLD) {
            // STRATEGY A: Brute Force (Cache Friendly)
            // Preferred for small N to avoid overhead.
            totalPenalty += quadraticStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        } else {
            // STRATEGY B: Spatial Hashing (Scalable)
            // Preferred for large N to reduce comparison pairs.
            totalPenalty += spatialStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        }

        // 3. NORMALIZATION
        // Transform Penalty to Fitness.
        // The "+ 1.0" denominator ensures no DivisionByZero and caps the score at 1.0.
        return 1.0 / (1.0 + totalPenalty);
    }

}
