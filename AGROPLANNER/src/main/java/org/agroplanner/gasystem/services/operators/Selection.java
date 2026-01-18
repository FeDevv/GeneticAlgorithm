package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.*;

/**
 * Component responsible for the <strong>Selection Mechanism</strong> (Survival of the Fittest).
 *
 * <p><strong>Architecture & Algorithms:</strong></p>
 * This class governs the transition of genetic material between generations through two complementary strategies:
 * <ul>
 * <li><strong>Elitism (Exploitation):</strong> A deterministic strategy that preserves the absolute best solutions.
 * It guarantees <em>Monotonicity</em> (the quality of the best solution never decreases over time).</li>
 * <li><strong>Tournament Selection (Exploration/Diversity):</strong> A stochastic strategy used to choose parents.
 * By picking the winner of a random subset, it applies selection pressure while still allowing
 * slightly weaker individuals a chance to reproduce, preserving genetic diversity.</li>
 * </ul>
 */
public class Selection {

    // ------------------- CONFIGURATION -------------------

    /**
     * The cardinality of the tournament subset.
     * <p><strong>Impact:</strong> Controls the Selection Pressure.
     * Large values approximate "max-finding" (high pressure, fast convergence, risk of local optima).
     * Small values act more randomly (low pressure, high diversity, slow convergence).
     * </p>
     */
    private final int tournamentSize;

    /**
     * The ratio of the population to be preserved via Elitism (e.g., 0.05 = 5%).
     */
    private final double elitesPercentage;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Configures the selection operator.
     *
     * @param tournamentSize   The bracket size for tournaments (hyperparameter).
     * @param elitesPercentage The percentage of top-tier individuals to clone directly.
     */
    public Selection(int tournamentSize, double elitesPercentage) {
        this.tournamentSize = tournamentSize;
        this.elitesPercentage = elitesPercentage;
    }

    // ------------------- SELECTION STRATEGIES -------------------

    /**
     * Identifies the top-performing individuals to be promoted directly to the next generation.
     *
     * <p><strong>Algorithmic Optimization (Min-Heap):</strong></p>
     * Instead of sorting the entire population (O(N log N)), this implementation utilizes a
     * {@link PriorityQueue} configured as a <strong>Min-Heap</strong> with a fixed capacity of K (elite count).
     * <ul>
     * <li>We iterate through the population (N).</li>
     * <li>The heap head represents the "weakest of the elites".</li>
     * <li>If a candidate is better than the head, we swap them.</li>
     * </ul>
     * This reduces the complexity to <strong>O(N log K)</strong>, offering significant performance gains
     * for large populations where K << N.
     *
     * @param oldPopulation The current generation to scan.
     * @return A list containing the top K individuals (order not guaranteed).
     */
    public List<Individual> selectElites(List<Individual> oldPopulation) {
        int populationSize = oldPopulation.size();

        // Determine the target number of elites (at least 1 to ensure the best solution survives).
        int eliteSize = Math.max(1 , (int)Math.floor(populationSize * elitesPercentage));

        // Structure: Min-Heap based on Fitness.
        // The element at the top (peek) is the one with the LOWEST fitness among the elites.
        PriorityQueue<Individual> elites = new PriorityQueue<>(eliteSize, Comparator.comparingDouble(Individual::getFitness));

        for (Individual ind : oldPopulation) {
            if (elites.size() < eliteSize) {
                // Phase 1: Fill the buffer.
                elites.offer(ind);
            } else if (ind.getFitness() > Objects.requireNonNull(elites.peek()).getFitness()) {
                // Phase 2: Challenge.
                // If the current individual is strictly better than the "worst" elite currently accepted,
                // eject the worst one and accept the new challenger.
                elites.poll();      // Remove the worst elite.
                elites.offer(ind);  // Add the new challenger.
            }
        }

        // Return as a list (Note: PriorityQueue iteration order is undefined, but they are the top K).
        return elites.stream().toList();
    }

    /**
     * Selects a single parent using the Tournament strategy.
     *
     * <p><strong>Mechanism:</strong></p>
     * <ol>
     * <li><strong>Sampling:</strong> Randomly selects M unique individuals from the population (where M is tournament size).</li>
     * <li><strong>Competition:</strong> Compares their fitness values.</li>
     * <li><strong>Victory:</strong> Returns the individual with the highest fitness in the subset.</li>
     * </ol>
     * This method is typically invoked twice to select a pair of parents for Crossover.
     *
     * @param oldPopulation The source population pool.
     * @return The winning {@link Individual}.
     */
    public Individual tournament(List<Individual> oldPopulation) {

        // Step 1: Stochastic Sampling (Without Replacement)
        // Using RandomUtils ensures we don't pick the same index twice in one tournament.
        List<Integer> indices = RandomUtils.uniqueIndices(tournamentSize, oldPopulation.size());


        List<Individual> participants = new ArrayList<>();
        for (int i : indices) {
            participants.add(oldPopulation.get(i));
        }

        // Step 2: Deterministic Competition (Max-Finding in subset)
        Individual winner = participants.getFirst();
        for (Individual individual : participants) {
            if (winner.getFitness() < individual.getFitness()) {
                winner = individual;
            }
        }
        return winner;
    }
}
