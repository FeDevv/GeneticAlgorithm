package org.agroplanner.gasystem.services.operators;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.utils.RandomUtils;

/**
 * Component responsible for the <strong>Mutation (Genetic Diversity)</strong> phase.
 *
 * <p><strong>Architecture & Algorithms:</strong></p>
 * <ul>
 * <li><strong>Strategy:</strong> Implements a <strong>Creep Mutation</strong> (Gaussian-like perturbation).
 * Instead of randomly resetting a gene to a new location (Random Resetting), it shifts the existing point
 * by a small vector. This preserves the structural information gained so far.</li>
 * <li><strong>Adaptive Dynamics (Simulated Annealing):</strong> Uses a dynamic mutation strength that decays
 * over time. This allows the algorithm to transition from coarse-grained Exploration (large jumps)
 * in early generations to fine-grained Exploitation (micro-adjustments) in later stages.</li>
 * </ul>
 */
public class Mutation {

    // ------------------- CONFIGURATION -------------------

    /**
     * The probability ($P_{mut}$) that a specific gene locus undergoes mutation.
     * <p>Typically low (e.g., 0.01 - 0.02) to prevent the algorithm from degenerating into a Random Search.</p>
     */
    private final double mutationProbability;

    /**
     * The scalar magnitude of the displacement vector at Generation 0.
     */
    private final double initialMutationStrength;

    /**
     * Contextual geometric constraints.
     */
    private final Domain domain;

    /**
     * The simulation horizon used to calculate the decay rate.
     */
    private final int totalGenerations;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Configures the mutation operator.
     *
     * @param mutationProbability     Probability of mutating a single gene.
     * @param initialMutationStrength Maximum movement range at the start.
     * @param domain                  The domain for boundary clamping.
     * @param totalGenerations        Total generations for the cooling schedule.
     */
    public Mutation(double mutationProbability, double initialMutationStrength, Domain domain, int totalGenerations) {
        // Immutability: Configuration is fixed at instantiation.
        this.mutationProbability = mutationProbability;
        this.initialMutationStrength = initialMutationStrength;
        this.domain = domain;
        this.totalGenerations = totalGenerations;
    }

    // ------------------- OPERATOR LOGIC -------------------

    /**
     * Introduces stochastic variations into an individual's genome.
     *
     * <p><strong>Performance Note (In-Place Modification):</strong></p>
     * Unlike Crossover, this operator modifies the {@link Individual} object directly (mutating its internal list).
     * This avoids the overhead of creating new list structures, but requires the caller to ensure that
     * the individual being mutated is a fresh offspring and not a reference to an Elite parent.
     *
     * <p><strong>Geometric Strategy (Hybrid Clamping):</strong></p>
     * When a point moves, we constrain it using {@link Math#clamp} against the Domain's Bounding Box.
     * We do <em>not</em> check the exact domain shape (e.g., Circle radius) here.
     * <br>
     * <em>Reasoning:</em> Checking complex shapes is expensive. Checking a rectangular box is O(1).
     * If a point ends up inside the box but outside the shape (e.g., corner of a circle's box),
     * the Fitness Function will penalize it later. This maximizes the throughput of the mutation loop.
     *
     * @param individual        The candidate solution to mutate.
     * @param currentGeneration The current epoch index (0 to N).
     */
    public void mutate(Individual individual, int currentGeneration) {

        // 1. Calculate Dynamic Scope (Simulated Annealing)
        double adaptiveStrenght = calculateAdaptiveStrength(currentGeneration);

        // 2. Optimization: Cache Boundary Limits (Hot Path)
        // Extract primitives to avoid calling getBoundingBox() inside the loop.
        java.awt.geom.Rectangle2D boundingBox = domain.getBoundingBox();
        double minX = boundingBox.getMinX();
        double minY = boundingBox.getMinY();
        double maxX = boundingBox.getMaxX();
        double maxY = boundingBox.getMaxY();

        // 3. Genome Iteration
        for (int i = 0; i < individual.getDimension(); i++) {

            // Stochastic Trigger: Does this gene mutate?
            if (RandomUtils.randDouble() < mutationProbability) {

                Point oldPoint = individual.getChromosomes().get(i);
                double radius = oldPoint.getRadius(); // Invariant
                PlantType type = oldPoint.getType(); // Invariant (Identity Preservation)

                // --- PERTURBATION LOGIC ---
                // Generate a random displacement vector (dx, dy) in range [-strength, +strength].
                // Math: (random * 2 - 1) shifts range [0,1] to [-1,1].
                double newX = oldPoint.getX() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;
                double newY = oldPoint.getY() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;

                // --- CONSTRAINT HANDLING (SOFT CLAMPING) ---
                // Keep the point within the "Universe" (Bounding Box).
                double finalX = Math.clamp(newX, minX, maxX);
                double finalY = Math.clamp(newY, minY, maxY);

                // State Update:
                // Since Point is immutable, we instantiate a new one.
                Point newPoint = new Point(
                        finalX,
                        finalY,
                        radius,
                        type,
                        oldPoint.getVarietyId(),   // <--- PRESERVA ID
                        oldPoint.getVarietyName()  // <--- PRESERVA NOME
                );
                individual.setChromosome(i, newPoint);
            }
        }
    }

    // ------------------- HELPER METHODS -------------------

    /**
     * Computes the current mutation magnitude using an Inverse Decay function.
     *
     * <p><strong>Formula:</strong></p>
     * S_{gen} = {S_{initial}} / {1 + k•{gen}/{total}}
     * <br>
     * Where k=5.0 is the steepness factor.
     * <ul>
     * <li>At Gen 0: Strength = 100%</li>
     * <li>At Gen 50%: Strength ~ 28%</li>
     * <li>At Gen 100%: Strength ~ 16%</li>
     * </ul>
     * This ensures the algorithm retains fine-tuning capabilities until the very end.
     *
     * @param gen The current generation index.
     * @return The maximum displacement allowed in meters.
     */
    private double calculateAdaptiveStrength(int gen) {
        // Steepness Factor (k=5.0) empirically tuned for convergence.
        return this.initialMutationStrength / (1.0 + 5.0 * ((double)gen / this.totalGenerations));
    }
}
