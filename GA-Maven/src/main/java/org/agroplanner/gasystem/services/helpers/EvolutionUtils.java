package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.gasystem.model.Individual;

import java.util.List;

/**
 * <p><strong>Stateless Utility Methods for the Evolutionary Process.</strong></p>
 *
 * <p>This class acts as a toolbox for the {@code EvolutionService}, providing helper methods
 * that do not require maintaining the state of the service. It handles:</p>
 * <ul>
 * <li><strong>Reduction Logic:</strong> Identifying the best candidate from a population (Max-Finding).</li>
 * <li><strong>Time Heuristics:</strong> calculating dynamic timeouts based on problem complexity.</li>
 * </ul>
 *
 * <p>Utility Class (Final, Static methods only).</p>
 */
public final class EvolutionUtils {

    /**
     * Private constructor to prevent instantiation.
     */
    private EvolutionUtils() {
        throw new IllegalStateException("Utility class");
    }

    /**
     * Identifies the absolute best solution between the current generation and the historical record.
     *
     * <p><strong>Logic:</strong>
     * <ol>
     * <li>Scans the current {@code population} to find the local best ("King of the Generation").</li>
     * <li>Compares the local King with the global {@code currentRecord} (Best solution found so far).</li>
     * <li>Returns the winner, ensuring the algorithm never "forgets" a superior solution found in the past.</li>
     * </ol>
     * </p>
     *
     * @param population    The list of individuals in the current generation.
     * @param currentRecord The best individual found in previous generations (can be {@code null} for the first run).
     * @return The absolute best {@link Individual}.
     */
    public static Individual findBestSolution(List<Individual> population, Individual currentRecord) {
        // Find the best within the current population (Local Max)
        Individual king = population.getFirst();
        for (int i = 1; i < population.size(); i++) {
            if (king.getFitness() < population.get(i).getFitness()) {
                king = population.get(i);
            }
        }

        // Handle First Generation case (No history yet)
        if (currentRecord == null) {
            return king;
        }

        // Global Comparison (Elitism Logic)
        // If the new king beats the old record, he becomes the new record.
        // Otherwise, we keep the old record.
        return (king.getFitness() > currentRecord.getFitness()) ? king : currentRecord;
    }

    /**
     * Calculates the dynamic Time Budget (Execution Ceiling) based on the problem size.
     *
     * <p><strong>Heuristic Formula:</strong> {@code T(n) = Base + (Factor * n)}</p>
     * <ul>
     * <li><strong>Base (5000ms):</strong> Fixed overhead for initialization, JVM warmup, and basic operations.</li>
     * <li><strong>Factor (100ms/gene):</strong> Estimated processing time per gene per cycle.</li>
     * </ul>
     * <p>This ensures the timeout scales linearly with complexity, preventing false positives on large inputs.</p>
     *
     * @param individualSize The number of genes (points) in the individual.
     * @return The calculated timeout limit in milliseconds.
     */
    public static long calculateTimeBudget(int individualSize) {
        long baseTime = 5000;   // 5 Seconds overhead
        long timePerGene = 100; // 0.1 Seconds per gene scaling factor
        return baseTime + ((long) individualSize * timePerGene);
    }
}
