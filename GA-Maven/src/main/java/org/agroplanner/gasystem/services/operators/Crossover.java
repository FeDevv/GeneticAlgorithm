package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p><strong>Genetic Operator: Recombination (Crossover).</strong></p>
 *
 * <p>This component is responsible for mixing the genetic material of two parents to create a new offspring.
 * It implements the <strong>Uniform Crossover</strong> strategy, which treats each gene independently.</p>
 *
 * <p><strong>Algorithmic Strategy:</strong><br>
 * Unlike Single-Point or Multi-Point crossover (which preserve sequences of genes), Uniform Crossover
 * decides the source of each gene (Parent A or Parent B) with a coin toss. This is particularly effective
 * for problems where gene ordering has no spatial significance (like a set of independent coordinates).</p>
 */
public class Crossover {

    // ------------------- CONFIGURATION -------------------

    /**
     * The probability (0.0 to 1.0) that recombination occurs between two selected parents.
     * If the probability check fails, the offspring will be a clone of one parent.
     */
    private final double crossoverProbability;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the Crossover operator.
     *
     * @param crossoverProbability The likelihood of mixing genes (e.g., 0.9 for 90%).
     */
    public Crossover(double crossoverProbability) {
        // Immutability: The probability is fixed for the lifespan of the operator.
        this.crossoverProbability = crossoverProbability;
    }

    // ------------------- OPERATOR LOGIC -------------------

    /**
     * Executes the <strong>Uniform Crossover</strong> between two parents to generate a single child.
     *
     * @param i1 The first parent.
     * @param i2 The second parent.
     * @return A new {@link Individual} instance representing the offspring.
     */
    public Individual uniformCrossover(Individual i1, Individual i2) {
        int lenght = i1.getDimension();
        List<Point> childChromosomes = new ArrayList<>(lenght);

        // Probabilistic Check: Do we mix genes?
        if(RandomUtils.randDouble() < crossoverProbability) {

            // --- STRATEGY: UNIFORM CROSSOVER ---
            // We iterate through every gene slot and flip a coin to decide inheritance.
            for (int i = 0; i < lenght; i++) {

                // Coin Toss: 0 -> Gene from Parent 1, 1 -> Gene from Parent 2
                if (RandomUtils.coinToss() == 0) {
                    // Thread-Safety/Immutability Note:
                    // We add a reference to the existing Point object.
                    // Since Point is immutable, sharing it between Parent and Child is perfectly safe and memory-efficient.
                    childChromosomes.add(i1.getChromosomes().get(i));
                } else {
                    childChromosomes.add(i2.getChromosomes().get(i));
                }
            }

            // Return the mixed child.
            // The Individual constructor creates a new ArrayList, ensuring structural isolation from here on.
            return new Individual(childChromosomes);

        } else {
            // Fallback: Cloning (Asexual Reproduction)
            // If crossover doesn't happen, the child is an exact copy of one of the parents.

            // Randomly select which parent to clone to maintain fair genetic pressure.
            if (RandomUtils.coinToss() == 0) {
                // Restituisce un nuovo individuo geneticamente identico a I1.
                return new Individual(i1.getChromosomes());
            } else {
                // Restituisce un nuovo individuo geneticamente identico a I2.
                return new Individual(i2.getChromosomes());
            }

            // Implementation Choice:
            // Even when cloning, we create a 'new Individual'. This ensures that the offspring
            // is a distinct object in heap memory. If we just returned 'i1', a subsequent
            // mutation on the child would alter the parent, destroying the algorithm's logic.
        }
    }
}
