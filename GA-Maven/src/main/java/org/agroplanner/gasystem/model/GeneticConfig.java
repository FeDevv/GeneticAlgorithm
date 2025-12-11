package org.agroplanner.gasystem.model;

/**
 * <p><strong>Global Configuration Registry for Genetic Algorithm Hyperparameters.</strong></p>
 *
 * <p>This utility class defines the static constants used to tune the evolutionary process.
 * These parameters control the delicate balance between <strong>Exploration</strong> (searching new areas of the solution space)
 * and <strong>Exploitation</strong> (refining existing good solutions).</p>
 *
 * <p><strong>Design Pattern:</strong> Utility Class (Final class with private constructor).</p>
 */
public final class GeneticConfig {

    /**
     * Private constructor to prevent instantiation of this utility class.
     * <p>
     * Complies with SonarCloud/CheckStyle rules for classes containing only static members.
     * </p>
     */
    private GeneticConfig() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    // ------------------- EVOLUTION FLOW PARAMETERS -------------------

    /**
     * The maximum number of generations (iterations) the algorithm will execute.
     * <p>Acts as the primary termination condition for the main loop.</p>
     */
    public static final int GENERATIONS = 800;

    /**
     * The fixed size of the population pool.
     * <p>
     * <strong>Impact:</strong>
     * <ul>
     * <li>Larger values increase diversity (Exploration) but increase computational cost per generation.</li>
     * <li>Smaller values speed up execution but risk premature convergence to local optima.</li>
     * </ul>
     * </p>
     */
    public static final int POPULATION_SIZE = 100;

    // ------------------- OPERATOR PARAMETERS -------------------

    /**
     * The number of individuals competing in the Tournament Selection.
     * <p>
     * <strong>Impact (Selection Pressure):</strong>
     * A value of 2 or 3 provides low selection pressure (preserving diversity).
     * Higher values increase pressure, favoring only the fittest individuals (faster convergence but higher risk of stagnation).
     * </p>
     */
    public static final int TOURNAMENT_SIZE = 3;

    /**
     * The fraction of the best individuals to preserve unchanged in the next generation.
     * <p>
     * <strong>Mechanism:</strong> Elitism guarantees that the best solution found so far is never lost.
     * Value 0.05 implies the top 5% of the population is copied directly.
     * </p>
     */
    public static final double ELITES_PERCENTAGE = 0.05;

    /**
     * The probability of performing Crossover (Recombination) on a pair of parents.
     * <p>
     * If the probability check fails, the parents are cloned into the next generation without mixing.
     * A high value (e.g., 0.9) encourages the combination of genetic material.
     * </p>
     */
    public static final double CROSSOVER_PROB = 0.9;

    /**
     * The initial magnitude (step size) of the mutation displacement.
     * <p>
     * <strong>Usage:</strong> Used in dynamic mutation strategies (e.g., Simulated Annealing).
     * The mutation strength typically decays over generations, starting from this value
     * to allow fine-tuning in later stages.
     * </p>
     */
    public static final double INITIAL_MUTATION_STRENGTH = 1.0;

    /**
     * The probability of a single gene (Point) undergoing mutation.
     * <p>
     * <strong>Impact:</strong>
     * This is the primary mechanism for introducing new genetic material and escaping local optima.
     * A value of 0.02 means each gene has a 2% chance of moving.
     * </p>
     */
    public static final double MUTATION_PROB = 0.02;
}
