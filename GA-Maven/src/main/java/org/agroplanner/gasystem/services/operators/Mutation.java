package org.agroplanner.gasystem.services.operators;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.shared.utils.RandomUtils;

/**
 * <p><strong>Genetic Operator: Mutation.</strong></p>
 *
 * <p>This component introduces genetic diversity into the population by randomly altering the coordinates of genes (Points).
 * It implements a <strong>"Creep Mutation"</strong> strategy (small local movements) combined with an
 * <strong>Adaptive Strength Mechanism</strong> (Simulated Annealing).</p>
 *
 * <p><strong>Algorithmic Strategy:</strong><br>
 * The mutation strength decays over generations. This allows for:
 * <ul>
 * <li><strong>Early Generations:</strong> Large movements to explore the search space (Exploration).</li>
 * <li><strong>Late Generations:</strong> Micro-adjustments to fine-tune the solution (Exploitation).</li>
 * </ul>
 * </p>
 */
public class Mutation {

    // ------------------- CONFIGURATION -------------------

    /**
     * The probability (0.0 to 1.0) that a single gene undergoes mutation.
     * Typically low (e.g., 0.01 - 0.05) to preserve good genetic structures.
     */
    private final double mutationProbability;

    /**
     * The initial maximum magnitude of the coordinate perturbation.
     * Represents the "radius" of movement at Generation 0.
     */
    private final double initialMutationStrength;

    /**
     * Reference to the problem domain.
     * Used to access the Bounding Box for spatial clamping.
     */
    private final Domain domain;

    /**
     * The total number of generations scheduled.
     * Required to calculate the decay factor for the adaptive strength.
     */
    private final int totalGenerations;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the Mutation operator with specific control parameters.
     *
     * @param mutationProbability     Probability of mutating a single gene.
     * @param initialMutationStrength Initial range of movement.
     * @param domain                  The geometric domain for boundary checks.
     * @param totalGenerations        Total duration of the evolution.
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
     * Executes the mutation process on a single individual.
     * <p>
     * <strong>Note:</strong> Unlike Crossover, this operation modifies the individual <strong>in-place</strong>.
     * </p>
     *
     * @param individual        The candidate solution to mutate.
     * @param currentGeneration The current step in the evolutionary process (used for adaptive strength).
     */
    public void mutate(Individual individual, int currentGeneration) {

        // Calculate the current movement range based on evolutionary progress.
        double adaptiveStrenght = calculateAdaptiveStrength(currentGeneration);

        // Pre-fetch Bounding Box limits (Optimization).
        // Accessing these once outside the loop is more efficient than calling getBoundingBox() N times.
        java.awt.geom.Rectangle2D boundingBox = domain.getBoundingBox();
        double minX = boundingBox.getMinX();
        double minY = boundingBox.getMinY();
        double maxX = boundingBox.getMaxX();
        double maxY = boundingBox.getMaxY();

        // Iterate over every gene (Point) in the chromosome.
        for (int i = 0; i < individual.getDimension(); i++) {

            // Probabilistic Check: Does this specific gene mutate?
            if (RandomUtils.randDouble() < mutationProbability) {

                Point oldPoint = individual.getChromosomes().get(i);
                double radius = oldPoint.getRadius(); // Radius remains invariant during mutation.
                PlantType type = oldPoint.getType(); // Preserviamo il tipo!

                // --- PERTURBATION LOGIC ---
                // Apply a random displacement in the range [-adaptiveStrength, +adaptiveStrength].
                // Formula: (Random[0,1] * 2 - 1) produces a value in [-1, 1].
                double newX = oldPoint.getX() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;
                double newY = oldPoint.getY() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;

                // --- CONSTRAINT HANDLING: SOFT CLAMPING ---
                // Force the new coordinates to stay within the Domain's Bounding Box.
                //
                // Architectural Decision:
                // We do NOT check the exact domain shape (e.g., Circle equation) here because it is computationally expensive.
                // We only check the rectangular bounds (O(1)). If a point is inside the box but outside the shape,
                // the Fitness Function will penalize it later. This is a "Hybrid Efficiency" strategy.
                double finalX = Math.clamp(newX, minX, maxX);
                double finalY = Math.clamp(newY, minY, maxY);

                // Create a NEW Point object (because Point is immutable).
                Point newPoint = new Point(finalX, finalY, radius, type);

                // Update the gene in the individual's chromosome list.
                individual.setChromosome(i, newPoint);
            }
        }
    }

    // ------------------- HELPER METHODS -------------------

    /**
     * Calculates the mutation strength (perturbation magnitude) for the current generation.
     *
     * <p><strong>Strategy: Inverse Decay.</strong><br>
     * The strength decreases as the generation count increases, following the curve:
     * {@code Strength = Initial / (1 + Factor * Progress)}
     * </p>
     *
     *
     *
     * @param gen The current generation number.
     * @return The maximum displacement allowed for this generation.
     */
    private double calculateAdaptiveStrength(int gen) {
        // Decay Factor (5.0): Controls the steepness of the curve.
        // A value of 5.0 ensures significant reduction while maintaining a long tail for fine-tuning.
        return this.initialMutationStrength / (1.0 + 5.0 * ((double)gen / this.totalGenerations));
    }
}
