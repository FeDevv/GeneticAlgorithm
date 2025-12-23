package org.agroplanner.gasystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a single candidate solution within the evolutionary population.
 *
 * <p><strong>Architecture & Terminology:</strong></p>
 * <ul>
 * <li><strong>Genotype:</strong> The internal representation of the solution, implemented as a sequence (List) of {@link Point} genes.</li>
 * <li><strong>Phenotype/Fitness:</strong> The numerical quality score ({@code fitness}) derived from evaluating the genotype against the objective function.</li>
 * <li><strong>Genetic Isolation:</strong> This class strictly enforces isolation between generations. Through defensive copying and
 * immutable views, it ensures that genetic operators (like Crossover) creating new children do not inadvertently
 * corrupt the state of parent individuals (Side-Effect Prevention).</li>
 * </ul>
 */
public class Individual {

    // ------------------- STATE -------------------

    /**
     * The ordered sequence of genes defining the solution.
     * <p>
     * <strong>Implementation Note:</strong> While declared as a {@link List}, the underlying implementation
     * is a mutable {@link ArrayList} to facilitate the {@link #setChromosome(int, Point)} mutation operator.
     * However, the reference is encapsulated and never exposed directly to external consumers.
     * </p>
     */
    private final List<Point> chromosomes;

    /**
     * The scalar quality score representing how well this individual solves the problem.
     * <p>Initialized to {@link Double#NEGATIVE_INFINITY} to mark the solution as "Dirty" (Unevaluated).</p>
     */
    private double fitness;

    // ------------------- CONSTRUCTORS -------------------

    /**
     * Constructs a new Individual from a raw genetic sequence.
     *
     * <p><strong>Defensive Copy Strategy:</strong></p>
     * Creates a new memory reference for the list ({@code new ArrayList<>(src)}).
     * Since the elements ({@link Point}) are immutable, this structural copy is sufficient to guarantee
     * complete independence from the source. Modifying the source list after this constructor returns
     * will have no effect on this individual.
     *
     * @param chromosomes The source list of genes.
     */
    public Individual(List<Point> chromosomes) {
        // Enforce Genetic Isolation immediately upon creation.
        this.chromosomes = new ArrayList<>(chromosomes);

        // Default state: Worst possible fitness.
        this.fitness = Double.NEGATIVE_INFINITY;
    }

    /**
     * Constructs a new Individual restoring full state (Cloning constructor).
     *
     * @param chromosomes The source list of genes.
     * @param fitness     The pre-calculated fitness value.
     */
    public Individual(List<Point> chromosomes, double fitness) {
        // Always enforce isolation via defensive copy.
        this.chromosomes = new ArrayList<>(chromosomes);
        this.fitness = fitness;
    }

    // ------------------- ACCESSORS -------------------

    /**
     * Retrieves a safe, read-only view of the genetic sequence.
     *
     * <p><strong>Encapsulation Boundary:</strong></p>
     * Returns a {@link Collections#unmodifiableList(List)}. This prevents external logic (e.g., buggy
     * crossover operators) from structurally modifying the chromosome (adding/removing genes), which
     * would violate the fixed-dimension constraint of the problem.
     *
     * @return An immutable wrapper around the internal list.
     */
    public List<Point> getChromosomes() {
        return Collections.unmodifiableList(this.chromosomes);
    }

    /**
     * Performs a controlled mutation on a single gene.
     *
     * <p><strong>Controlled Mutability:</strong></p>
     * This is the <em>only</em> access point permitted to modify the genotype. It allows the Mutation Operator
     * to swap a specific gene with a new {@link Point} instance, preserving the list's structural integrity (size).
     *
     * @param index The locus (index) of the gene to replace.
     * @param point The new allele (Point value).
     */
    public void setChromosome(int index, Point point) {
        this.chromosomes.set(index, point);
    }

    /**
     * Updates the cached fitness score.
     * <p>Typically invoked by the {@code EvolutionService} after the evaluation phase.</p>
     *
     * @param fitness The computed quality score.
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Retrieves the current fitness score.
     * @return The quality value (higher is better).
     */
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Returns the dimensionality of the solution (Genome Length).
     * @return The number of genes.
     */
    public int getDimension() {
        return chromosomes.size();
    }

    // ------------------- UTILITY -------------------

    /**
     * Creates an independent clone of this individual.
     *
     * <p><strong>Usage (Elitism):</strong></p>
     * Essential for preserving the best individuals across generations. By creating a deep structural copy,
     * we ensure that subsequent mutations on the "Elite" copy do not degrade the original best solution
     * stored in history.
     *
     * @return A new {@code Individual} instance with identical state but separate memory identity.
     */
    public Individual copy() {
        // Leverages the full constructor which handles the defensive copying.
        return new Individual(this.chromosomes, this.fitness);
    }

    /**
     * Serializes the individual's state to a string format.
     * Used for debug logging and final result export.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n");
        for (Point p : chromosomes) {
            sb.append(p.toString()).append("\n");
        }
        return sb.toString();
    }
}
