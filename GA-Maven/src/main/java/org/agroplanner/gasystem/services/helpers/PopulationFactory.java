package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.GeneticConfig;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * <p><strong>Factory for Initializing the Population (Genesis).</strong></p>
 *
 * <p>This class is responsible for creating the initial set of candidate solutions (Generation 0).
 * By extracting this logic from the {@code EvolutionService}, we adhere to the <strong>Separation of Concerns</strong>:
 * the Service orchestrates the process, while this Factory handles the "manufacturing" of data.</p>
 */
public class PopulationFactory {

    // ------------------- DEPENDENCIES -------------------

    private final Domain domain;
    private final int individualSize;
    private final double pointRadius;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs the factory with the context required to generate valid points.
     *
     * @param domain         The geometric domain (provides the Bounding Box for random generation).
     * @param individualSize The number of genes (points) per individual.
     * @param pointRadius    The radius to assign to each generated point.
     */
    public PopulationFactory(Domain domain, int individualSize, double pointRadius) {
        this.domain = domain;
        this.individualSize = individualSize;
        this.pointRadius = pointRadius;
    }

    /**
     * Creates the initial population ("Primordial Soup").
     * <p>
     * It generates {@link GeneticConfig#POPULATION_SIZE} individuals with randomized DNA.
     * </p>
     *
     * @return A list of completely new, unevaluated individuals.
     */
    public List<Individual> createFirstGeneration() {
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildRandomIndividual());
        }
        return firstGen;
    }

    /**
     * Builds a single individual with random genes within the domain's search space.
     *
     * <p><strong>Generation Logic:</strong>
     * Points are generated uniformly within the {@link Domain#getBoundingBox()}.
     * <br><em>Note:</em> For non-rectangular domains (e.g., Circle), some initial points might fall
     * inside the bounding box but outside the specific shape. This is expected; the
     * evolutionary pressure (Fitness Function) will guide them into the valid area over generations.</p>
     *
     * @return A new Individual instance.
     */
    private Individual buildRandomIndividual() {
        List<Point> points = new ArrayList<>(individualSize);
        for (int i = 0; i < individualSize; i++) {
            // Uses the domain's Bounding Box as the limit for the random generator
            points.add(RandomUtils.insideBoxGenerator(domain.getBoundingBox(), pointRadius));
        }
        return new Individual(points);
    }
}
