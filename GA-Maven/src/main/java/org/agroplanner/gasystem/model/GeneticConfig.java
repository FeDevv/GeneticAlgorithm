package org.agroplanner.gasystem.model;

/**
 * Static registry for Genetic Algorithm (GA) hyperparameters.
 *
 * <p><strong>Architecture & Tuning:</strong></p>
 * This utility class centralizes the configuration of the evolutionary engine. The constants defined here
 * control the fundamental trade-off between <strong>Exploration</strong> (probing new regions of the search space)
 * and <strong>Exploitation</strong> (refining promising solutions).
 *
 * <p><strong>Context:</strong></p>
 * These values are critical for the convergence behavior of the algorithm.
 *
 * <ul>
 * <li>High mutation/crossover encourages exploration but risks destroying good solutions.</li>
 * <li>High selection pressure (tournament size) encourages exploitation but risks premature convergence.</li>
 * </ul>
 */
public final class GeneticConfig {

    /**
     * Enforces the Utility Class pattern (non-instantiable).
     * <p>Throws {@link IllegalStateException} to prevent reflective instantiation.</p>
     */
    private GeneticConfig() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    // ------------------- TERMINATION & RESOURCES -------------------

    /**
     * The hard limit on the evolutionary loop iterations.
     * <p><strong>Role:</strong> Acts as the primary termination condition. The algorithm stops
     * strictly after this many generations, ensuring predictable execution time (Computational Budget).</p>
     */
    public static final int GENERATIONS = 800;

    /**
     * The fixed cardinality of the population pool.
     * <p><strong>Impact:</strong>
     * <ul>
     * <li>High values (>200) increase <em>Genetic Diversity</em>, reducing the risk of getting stuck in local optima,
     * but linearly increase the CPU time per generation.</li>
     * <li>Low values (<50) are faster but may lead to <em>Premature Convergence</em> due to lack of genetic material.</li>
     * </ul>
     * </p>
     */
    public static final int POPULATION_SIZE = 100;

    // ------------------- SELECTION STRATEGY -------------------

    /**
     * The bracket size for Tournament Selection.
     * <p><strong>Impact (Selection Pressure):</strong>
     * Defines how competitive the mating selection is.
     * <ul>
     * <li>Small (2-3): Low pressure. Preserves diversity, allowing weaker individuals a chance to reproduce.</li>
     * <li>Large (>5): High pressure. Quickly eliminates weak solutions, driving fast convergence but risking stagnation.</li>
     * </ul>
     * </p>
     */
    public static final int TOURNAMENT_SIZE = 3;

    /**
     * The fraction of top-performing individuals guaranteed to survive to the next generation.
     * <p><strong>Role (Monotonicity):</strong>
     * Elitism ensures that the max fitness of the population acts as a monotonic non-decreasing function.
     * Without elitism, genetic operators (mutation/crossover) could accidentally destroy the best solution found so far.
     * </p>
     */
    public static final double ELITES_PERCENTAGE = 0.05;

    // ------------------- GENETIC OPERATORS -------------------

    /**
     * The probability of recombination occurring between two parents.
     * <p>
     * Controls the mixing of genetic traits. If the probability check fails, the offspring are exact clones
     * of the parents (asexual reproduction). High values promote the combination of partial solutions.
     * </p>
     */
    public static final double CROSSOVER_PROB = 0.9;

    /**
     * The initial scalar magnitude for mutation displacement.
     * <p><strong>Role (Adaptive Mutation):</strong>
     * Used in dynamic strategies (like Simulated Annealing). The mutation strength decays over time:
     * early generations make large jumps (Exploration), while later generations make micro-adjustments (Refinement).
     * </p>
     */
    public static final double INITIAL_MUTATION_STRENGTH = 1.0;

    /**
     * The probability of a stochastic variation occurring on a single gene (Point).
     * <p><strong>Role (Diversity Maintenance):</strong>
     * Mutation is the primary mechanism to reintroduce lost genetic material and "shake" the population
     * out of local optima valleys in the fitness landscape.
     * </p>
     */
    public static final double MUTATION_PROB = 0.02;
}
