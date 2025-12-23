package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.GeneticConfig;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.utils.RandomUtils;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Factory component responsible for the stochastic initialization (Genesis) of the population.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Role:</strong> Decouples the data manufacturing logic from the orchestration logic (Separation of Concerns).
 * It acts as the "Primordial Soup" generator for the evolutionary timeline (Generation 0).</li>
 * <li><strong>Strategy (Rejection Sampling preparation):</strong> Generates random points strictly within the
 * Domain's Bounding Box. Note that these points might initially fall outside the specific shape (e.g., in the corners of a Circle's box).
 * It is the job of the Fitness Function (penalty system) to evolutionary "push" them inside the valid area over time.</li>
 * </ul>
 */
public class PopulationFactory {

    // ------------------- DEPENDENCIES -------------------

    /** Defines the spatial boundaries for generation. */
    private final Domain domain;

    /** Defines the "Recipe" (ingredients) that every individual must contain. */
    private final PlantInventory inventory;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Constructs the factory with the simulation context.
     *
     * @param domain    The geometric constraints.
     * @param inventory The biological constraints.
     */
    public PopulationFactory(Domain domain, PlantInventory inventory) {
        this.domain = domain;
        this.inventory = inventory;
    }

    // ------------------- FACTORY METHODS -------------------

    /**
     * Manufactures the initial population pool (Generation 0).
     *
     * <p><strong>Performance Note:</strong></p>
     * This operation is O(P×G), where P is the population size and G is the genome length.
     * It pre-allocates memory to avoid resizing overhead during the loop.
     *
     * @return A list of completely random, yet structurally valid, {@link Individual}s.
     */
    public List<Individual> createFirstGeneration() {
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildRandomIndividual());
        }
        return firstGen;
    }

    /**
     * Assembles a single individual by "unrolling" the high-level inventory configuration into a flat gene sequence.
     *
     * <p><strong>Genotypic Alignment (Crucial Logic):</strong></p>
     * After generating the random points, the method performs a {@code genes.sort(...)} based on Plant Type.
     * <br>
     * This ensures <strong>Homology</strong>: the gene at index i will represent the same species across
     * ALL individuals in the population.
     *
     * <br>
     * <em>Why?</em> This allows standard Crossover operators to swap genetic material without violating the
     * Inventory constraints (e.g., swapping a Tomato slot with another Tomato slot preserves the total count of Tomatoes).
     *
     * @return A new random Individual.
     */
    private Individual buildRandomIndividual() {
        // Pre-allocate list size to prevent array copying during expansion.
        List<Point> genes = new ArrayList<>(inventory.getTotalPopulationSize());

        // Geometric Heuristic: Fetch the sampling limit once (MBR).
        Rectangle2D limits = domain.getBoundingBox();

        // 1. Unrolling Phase: Convert "Batch" (InventoryEntry) -> "Atoms" (Points)
        for (InventoryEntry entry : inventory.getEntries()) {

            int quantity = entry.getQuantity();
            double radius = entry.getRadius();
            PlantType type = entry.getType();

            // Expand the batch into individual genes
            for (int i = 0; i < quantity; i++) {
                // Stochastic Generation: Place the point somewhere in the bounding box.
                genes.add(RandomUtils.insideBoxGenerator(limits, radius, type));
            }
        }

        // 2. Alignment Phase: Enforce structural consistency.
        // the solution is SORTED to ensure that Index N always corresponds to the same PlantType
        // across the entire population (Homologous Chromosomes).
        genes.sort(Comparator.comparing(Point::getType));

        return new Individual(genes);
    }
}