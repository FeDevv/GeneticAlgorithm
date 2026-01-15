package org.agroplanner.gasystem.services.helpers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.model.GeneticConfig;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;
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

    private final Domain domain;
    private final PlantInventory inventory;

    public PopulationFactory(Domain domain, PlantInventory inventory) {
        this.domain = domain;
        this.inventory = inventory;
    }

    public List<Individual> createFirstGeneration() {
        // Ottimizzazione memoria: Size pre-calcolata
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildRandomIndividual());
        }
        return firstGen;
    }

    private Individual buildRandomIndividual() {
        List<Point> genes = new ArrayList<>(inventory.getTotalPopulationSize());
        Rectangle2D limits = domain.getBoundingBox();

        // 1. Unrolling Phase: Batch -> Atoms
        for (InventoryEntry entry : inventory.getEntries()) {

            // Dati Generici
            int quantity = entry.getQuantity();

            // Dati Specifici (Estratti dallo Sheet)
            PlantVarietySheet sheet = entry.getVarietySheet();
            double radius = sheet.getMinDistance(); // Usiamo quello dello sheet per coerenza
            int varId = sheet.getId();
            String varName = sheet.getVarietyName();

            for (int i = 0; i < quantity; i++) {
                // FIX: Delega a RandomUtils.
                // Ora usiamo ThreadLocalRandom (thread-safe e veloce) invece di Math.random (synchronized).
                Point p = RandomUtils.insideBoxGenerator(
                        limits,
                        radius,
                        sheet.getType(),
                        varId,
                        varName
                );

                genes.add(p);
            }
        }

        // 2. Alignment Phase (HOMOLOGY ENFORCEMENT)
        // Ordiniamo per Tipo E POI per ID Varietà.
        // Questo garantisce che se ho [Pomodoro A, Pomodoro B], l'ordine sia sempre A, B in tutti gli individui.
        genes.sort(Comparator.comparing(Point::getType)
                .thenComparingInt(Point::getVarietyId));

        return new Individual(genes);
    }
}