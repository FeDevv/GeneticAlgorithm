package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.*;

/**
 * <p><strong>Genetic Operator: Selection.</strong></p>
 *
 * <p>This component determines which individuals from the current generation are chosen
 * to survive or reproduce. It implements two distinct strategies to balance evolutionary pressure:</p>
 * <ul>
 * <li><strong>Elitism:</strong> Preserves the absolute best solutions to prevent regression (Non-Degeneration).</li>
 * <li><strong>Tournament Selection:</strong> Probabilistically selects parents for reproduction based on fitness comparison.</li>
 * </ul>
 */
public class Selection {

    // ------------------- CONFIGURATION -------------------

    /**
     * The number of individuals competing in a single tournament.
     * <p>Higher values increase selection pressure (convergence speed) but reduce diversity.</p>
     */
    private final int tournamentSize;

    /**
     * The fraction of the population to preserve as elite (e.g., 0.05 for 5%).
     */
    private final double elitesPercentage;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the Selection operator.
     *
     * @param tournamentSize   The size N of the tournament.
     * @param elitesPercentage The percentage of top individuals to keep.
     */
    public Selection(int tournamentSize, double elitesPercentage) {
        this.tournamentSize = tournamentSize;
        this.elitesPercentage = elitesPercentage;
    }

    // ------------------- SELECTION STRATEGIES -------------------

    /**
     * Executes the <strong>Elitism Strategy</strong>.
     * <p>
     * It identifies and extracts the top K% individuals from the population to ensure
     * they are copied unchanged into the next generation.
     * </p>
     *
     * @param oldPopulation The current generation.
     * @return A list containing the best individuals (the elites).
     */
    public List<Individual> selectElites(List<Individual> oldPopulation) {
        int populationSize = oldPopulation.size();

        // Calculate elite size, ensuring at least 1 survivor if the population is small.
        int eliteSize = Math.max(1 , (int)Math.floor(populationSize * elitesPercentage));

        // Implementation Choice: PriorityQueue as a Min-Heap.
        // Instead of sorting the entire population (O(N log N)), we use a fixed-size queue (O(N log K)).
        // Logic: The head of the queue (peek) is the "worst of the best". If we find someone better,
        // we kick the head out and let the new one in.
        PriorityQueue<Individual> elites = new PriorityQueue<>(eliteSize, Comparator.comparingDouble(Individual::getFitness));

        for (Individual ind : oldPopulation) {
            if (elites.size() < eliteSize) {
                // Fill the queue until we reach the elite capacity.
                elites.offer(ind);
            } else if (ind.getFitness() > Objects.requireNonNull(elites.peek()).getFitness()) {
                // The current individual is better than the worst elite currently in the queue.
                elites.poll();      // Remove the worst elite.
                elites.offer(ind);  // Add the new challenger.
            }
        }

        // Convert the queue to a list. Order is not guaranteed, but they are the top K.
        return elites.stream().toList();
    }

    /**
     * Executes the <strong>Tournament Selection</strong>.
     * <p>
     * Randomly picks N individuals from the population and returns the one with the highest fitness.
     * This method is called repeatedly to select parents for Crossover.
     * </p>
     * @param oldPopulation The source population.
     * @return The winning individual.
     */
    public Individual tournament(List<Individual> oldPopulation) {
        // Random Sampling: Extract N unique indices.
        // Implementation relies on RandomUtils to ensure no duplicate participants in the same tournament.
        List<Integer> indices = RandomUtils.uniqueIndices(tournamentSize, oldPopulation.size());


        List<Individual> participants = new ArrayList<>();
        for (int i : indices) {
            participants.add(oldPopulation.get(i));
        }

        // Competition: Find the winner (Max Fitness)
        Individual winner = participants.getFirst();
        for (Individual individual : participants) {
            if (winner.getFitness() < individual.getFitness()) {
                winner = individual;
            }
        }
        return winner;
    }
}
