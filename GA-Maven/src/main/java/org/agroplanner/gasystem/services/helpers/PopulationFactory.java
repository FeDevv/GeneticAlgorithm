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
 * <p><strong>Factory for Initializing the Population (Genesis).</strong></p>
 *
 * <p>This class is responsible for creating the initial set of candidate solutions (Generation 0).
 * By extracting this logic from the {@code EvolutionService}, we adhere to the <strong>Separation of Concerns</strong>:
 * the Service orchestrates the process, while this Factory handles the "manufacturing" of data.</p>
 */
/**
 * <p><strong>Factory for Initializing the Population (Genesis).</strong></p>
 *
 * <p>Responsible for "manufacturing" the initial solutions based on the {@link PlantInventory}.
 * Uses {@link RandomUtils} to generate points within the domain bounds.</p>
 */
public class PopulationFactory {

    // ------------------- DEPENDENCIES -------------------

    private final Domain domain;
    private final PlantInventory inventory;

    // ------------------- CONSTRUCTOR -------------------

    public PopulationFactory(Domain domain, PlantInventory inventory) {
        this.domain = domain;
        this.inventory = inventory;
    }

    // ------------------- FACTORY METHODS -------------------

    public List<Individual> createFirstGeneration() {
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildRandomIndividual());
        }
        return firstGen;
    }

    /**
     * Builds a single individual by "unrolling" the inventory.
     */
    private Individual buildRandomIndividual() {
        // Pre-allocazione per performance
        List<Point> genes = new ArrayList<>(inventory.getTotalPopulationSize());

        // Otteniamo il Bounding Box una volta sola per chiarezza (anche se è veloce)
        Rectangle2D limits = domain.getBoundingBox();

    // Iteriamo sulla lista dei lotti (Batch List)
        for (InventoryEntry entry : inventory.getEntries()) {

            int quantity = entry.getQuantity();
            double radius = entry.getRadius();
            PlantType type = entry.getType();

            // Per ogni pianta richiesta in questo lotto...
            for (int i = 0; i < quantity; i++) {
                // ...generiamo un gene con QUEL raggio specifico e QUEL tipo.
                // Se hai inserito pomodori grandi e piccoli, qui verranno creati
                // correttamente con le dimensioni giuste.
                genes.add(RandomUtils.insideBoxGenerator(limits, radius, type));
            }
        }

        // Mescoliamo i geni per favorire il Crossover
        genes.sort(Comparator.comparing(Point::getType));

        return new Individual(genes);
    }
}