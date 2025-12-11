package org.agroplanner.gasystem.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * <p><strong>The Chromosome (Candidate Solution).</strong></p>
 *
 * <p>This class represents a single individual in the population. It encapsulates:</p>
 * <ul>
 * <li><strong>Genotype:</strong> A sequence of Genes ({@link Point} objects).</li>
 * <li><strong>Phenotype/Quality:</strong> The fitness score representing how well it solves the problem.</li>
 * </ul>
 *
 * <p><strong>Architectural Note on Memory Management:</strong><br>
 * While the internal list of chromosomes is mutable (to allow specific mutations),
 * the class strictly enforces <strong>Genetic Isolation</strong> via defensive copying in constructors
 * and unmodifiable views in getters. This ensures that operators like Crossover do not
 * inadvertently modify the parents when creating children.</p>
 */
public class Individual {
    // ------------------- STATE -------------------

    /**
     * The sequence of genes.
     * <p>
     * <strong>Implementation Note:</strong> This is a mutable {@link ArrayList} internally
     * (to allow {@link #setChromosome(int, Point)}), but its reference is final.
     * External access is restricted to read-only views.
     * </p>
     */
    private final List<Point> chromosomes;

    /**
     * The fitness score calculated by the evaluation function.
     * A higher value indicates a better solution.
     * Initialized to {@link Double#NEGATIVE_INFINITY} to represent an unevaluated state.
     */
    private double fitness;

    // ------------------- CONSTRUCTORS -------------------

    /**
     * Primary Constructor: Creates a new individual from a list of genes.
     *
     * @param chromosomes The list of Points defining the solution.
     */
    public Individual(List<Point> chromosomes) {
        // GENETIC ISOLATION (Defensive Copy):
        // It is created a NEW ArrayList containing the same elements.
        // Since Point is immutable, a shallow copy of the list is sufficient to ensure deep safety.
        // Even if the source list is modified later, this individual remains unaffected.
        this.chromosomes = new ArrayList<>(chromosomes);

        // Default state: Worst possible fitness until evaluated.
        this.fitness = Double.NEGATIVE_INFINITY;
    }

    /**
     * Full Constructor: Used for cloning or restoring state.
     *
     * @param chromosomes The list of genes.
     * @param fitness     The pre-calculated fitness value.
     */
    public Individual(List<Point> chromosomes, double fitness) {
        // Consistency: Always perform the defensive copy.
        this.chromosomes = new ArrayList<>(chromosomes);
        this.fitness = fitness;
    }

    // ------------------- ACCESSORS (Getters & Setters) -------------------

    /**
     * Retrieves a <strong>Read-Only view</strong> of the chromosomes.
     *
     * <p><strong>Security:</strong> Using {@link Collections#unmodifiableList} prevents external actors
     * (like buggy crossover operators) from structurally modifying the chromosome list
     * (adding/removing genes), preserving the fixed dimension of the individual.</p>
     *
     * @return An unmodifiable list of Points.
     */
    public List<Point> getChromosomes() {
        return Collections.unmodifiableList(this.chromosomes);
    }

    /**
     * Performs a targeted mutation on a single gene.
     *
     * <p><strong>Design Choice: Controlled Mutability.</strong><br>
     * This is the <em>only</em> way to modify the genotype. It allows the Mutation operator
     * to swap a specific gene without exposing the entire list to modification risks.</p>
     *
     * @param index The index of the gene to replace.
     * @param point The new gene (Point).
     */
    public void setChromosome(int index, Point point) {
        this.chromosomes.set(index, point);
    }

    /**
     * Updates the fitness score.
     * Called by the {@code FitnessCalculator} after evaluation.
     *
     * @param fitness The new quality score.
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /**
     * Retrieves the current fitness score.
     * @return The fitness value.
     */
    public double getFitness() {
        return this.fitness;
    }

    /**
     * Returns the dimension of the problem (number of genes).
     * @return The size of the chromosome list.
     */
    public int getDimension() {
        return chromosomes.size();
    }

    // ------------------- UTILITY -------------------

    /**
     * Creates a deep copy of this individual.
     * <p>
     * Used mainly for <strong>Elitism</strong> (preserving the best individual across generations).
     * Since {@code Point} is immutable, we only need to duplicate the list structure and the fitness.
     * </p>
     *
     * @return A new independent {@code Individual} instance identical to this one.
     */
    public Individual copy() {
        // Optimization: We pass the list directly.
        // The logic of "new ArrayList<>(list)" is handled inside the target constructor.
        return new Individual(this.chromosomes, this.fitness);
    }

    /**
     * Returns a string representation of the individual (genes and structure).
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
