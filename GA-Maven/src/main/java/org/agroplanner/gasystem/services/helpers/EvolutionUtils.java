package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.gasystem.model.Individual;

import java.util.List;

/**
 * Stateless utility toolbox providing helper algorithms for the evolutionary process.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Utility Class. Consists exclusively of static methods and cannot be instantiated.</li>
 * <li><strong>Role:</strong> Offloads specific algorithmic tasks (Reduction, Estimation) from the main
 * {@code EvolutionService}, keeping the core logic clean and focused on the orchestration of the lifecycle.</li>
 * <li><strong>Statelessness:</strong> Functions are pure (output depends only on input), making them inherently
 * thread-safe and testable in isolation.</li>
 * </ul>
 */
public final class EvolutionUtils {

    /**
     * Private constructor to strictly prevent instantiation.
     */
    private EvolutionUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Identifies the optimal solution by comparing the current generation's best against the historical record.
     *
     * <p><strong>Algorithmic Complexity:</strong> O(N)</p>
     * Performs a linear scan of the population to find the local maximum. This is computationally more efficient
     * than sorting the entire population (O(N log N)), which is unnecessary just to find the single best element.
     *
     * <p><strong>Monotonicity Guarantee:</strong></p>
     * This method enforces the <em>Elitism Principle</em> at the global level. By comparing the local champion
     * against {@code currentRecord}, it ensures that the "Global Best" quality never degrades over generations,
     * effectively making the optimization process monotonic non-decreasing.
     *
     * @param population    The list of individuals in the current generation (must not be empty).
     * @param currentRecord The best individual found in all previous generations (nullable for the first epoch).
     * @return The absolute superior solution between the current local best and the historical global best.
     */
    public static Individual findBestSolution(List<Individual> population, Individual currentRecord) {
        // Step 1: Linear Reduction to find Local Max (King of the Generation)
        Individual king = population.getFirst();
        for (int i = 1; i < population.size(); i++) {
            if (king.getFitness() < population.get(i).getFitness()) {
                king = population.get(i);
            }
        }

        // Handle cold start (First Generation)
        if (currentRecord == null) {
            return king;
        }

        // Step 2: Global Comparison
        // If the new king outperforms the old record, promote him. Otherwise, retain history.
        return (king.getFitness() > currentRecord.getFitness()) ? king : currentRecord;
    }

    /**
     * Estimates a dynamic computational budget (Time Limit) based on problem complexity.
     *
     * <p><strong>Heuristic Model:</strong> {@code T(n) = Base + (Slope * n)}</p>
     * Calculates a timeout ceiling that scales linearly with the dimensionality of the solution.
     * <ul>
     * <li><strong>Base (5000ms):</strong> Covers JVM warmup, class loading, and fixed framework overhead.</li>
     * <li><strong>Slope (100ms/gene):</strong> Estimated processing cost per gene across all generations.</li>
     * </ul>
     * This adaptive approach prevents "False Positive" timeouts on physically large problems that genuinely
     * require more CPU time to converge.
     *
     * @param individualSize The number of genes (points) in the solution vector.
     * @return The calculated execution time budget in milliseconds.
     */
    public static long calculateTimeBudget(int individualSize) {
        long baseTime = 5000;   // 5s Fixed Overhead
        long timePerGene = 100; // Linear scaling factor
        return baseTime + (individualSize * timePerGene);
    }
}
