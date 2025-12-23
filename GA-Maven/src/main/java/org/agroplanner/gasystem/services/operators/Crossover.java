package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Component responsible for the <strong>Recombination (Crossover)</strong> phase.
 *
 * <p><strong>Architecture & Algorithms:</strong></p>
 * <ul>
 * <li><strong>Strategy:</strong> Implements <strong>Uniform Crossover</strong>. Unlike Single-Point or Multi-Point strategies
 * which swap contiguous segments of the chromosome, Uniform Crossover treats each gene locus independently.</li>
 * <li><strong>Schema Disruption:</strong> This strategy has a high "Mixing Ratio" (~0.5). It is highly effective for
 * problems where the relative order of genes is irrelevant (like this spatial packing problem), as it allows
 * for a more granular exploration of the search space by dismantling gene blocks.</li>
 * </ul>
 */
public class Crossover {

    // ------------------- CONFIGURATION -------------------

    /**
     * The probability threshold ($P_{cross}$) determining if recombination occurs.
     * <p>Range: [0.0, 1.0]. Typically high (e.g., 0.9).</p>
     */
    private final double crossoverProbability;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Configures the crossover operator.
     *
     * @param crossoverProbability The likelihood of two parents mixing their genetic material.
     */
    public Crossover(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    // ------------------- OPERATOR LOGIC -------------------

    /**
     * Executes the stochastic mixing of two parent genomes to produce a single offspring.
     *
     * <p><strong>Logic Flow:</strong></p>
     * <ol>
     * <li><strong>Activation Check:</strong> A random number is compared against P_{cross}.</li>
     * <li><strong>If Activated (Sexual Reproduction):</strong> Iterates through every gene index.
     * A "coin toss" (Bernoulli trial, p=0.5) determines whether the child inherits the gene (allele)
     * from Parent A or Parent B.</li>
     * <li><strong>If Deactivated (Asexual Reproduction):</strong> The offspring is created as an exact clone
     * of one of the parents (chosen randomly).</li>
     * </ol>
     *
     * <p><strong>Memory Optimization:</strong></p>
     * Since {@link Point} objects are immutable, the child reuses the memory references of the parents' genes.
     * No deep copying of the {@code Point} objects is required, saving significant heap allocation time.
     *
     * @param i1 The first parent (Donor A).
     * @param i2 The second parent (Donor B).
     * @return A new {@link Individual} instance representing the next generation.
     */
    public Individual uniformCrossover(Individual i1, Individual i2) {
        int lenght = i1.getDimension();
        List<Point> childChromosomes = new ArrayList<>(lenght);

        // Phase 1: Probability Check
        // Does recombination occur?
        if(RandomUtils.randDouble() < crossoverProbability) {

            // --- STRATEGY: UNIFORM CROSSOVER ---
            // Independent selection per locus.
            for (int i = 0; i < lenght; i++) {

                // Stochastic Selection: 50% chance from Parent 1, 50% from Parent 2.
                if (RandomUtils.coinToss() == 0) {
                    // Safe Reference Sharing (Immutability)
                    childChromosomes.add(i1.getChromosomes().get(i));
                } else {
                    childChromosomes.add(i2.getChromosomes().get(i));
                }
            }

            // Return the recombinant offspring.
            // The Individual constructor ensures a new List structure is created.
            return new Individual(childChromosomes);

        } else {
            // Phase 2: Cloning (Fallback)
            // Crossover failed. The child is a clone of one parent.

            // Randomly select which parent to propagate to preserve fair selection pressure.
            if (RandomUtils.coinToss() == 0) {
                // Cloning Parent 1
                return new Individual(i1.getChromosomes());
            } else {
                // Cloning Parent 2
                return new Individual(i2.getChromosomes());
            }

            // Safety Note:
            // We use 'new Individual(List)' even when cloning.
            // Returning 'i1' directly would return a reference to the OLD generation object.
            // If that object were later mutated, we would corrupt the history.
            // Creating a new Individual guarantees GENETIC ISOLATION.
        }
    }
}
