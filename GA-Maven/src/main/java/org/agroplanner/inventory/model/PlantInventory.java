package org.agroplanner.inventory.model;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Aggregates user-defined botanical selections into a cohesive inventory structure.
 * <p>
 * <strong>Architecture:</strong> Acts as the data staging area between the Input Layer
 * and the Evolutionary Core. It utilizes a List structure to support <strong>Batch Processing</strong>,
 * allowing multiple entries for the same Species but different Varieties to coexist.
 * </p>
 */
public class PlantInventory {

    private final List<InventoryEntry> entries;

    public PlantInventory() {
        this.entries = new ArrayList<>();
    }

    /**
     * Appends a new production batch to the inventory.
     *
     * @param sheet    The technical sheet of the plant variety.
     * @param quantity The number of individuals requested.
     * @throws InvalidInputException If inputs are null, negative, or physically invalid.
     */
    public void addEntry(PlantVarietySheet sheet, int quantity) throws InvalidInputException {
        if (sheet == null) throw new InvalidInputException("Variety Sheet cannot be null.");
        if (quantity <= 0) throw new InvalidInputException("Quantity must be positive.");

        if (sheet.getMinDistance() <= 0) throw new InvalidInputException("The selected variety has an invalid radius.");

        entries.add(new InventoryEntry(sheet, quantity));
    }

    /**
     * Returns a read-only view of the current inventory batches.
     * @return An unmodifiable list of {@link InventoryEntry}.
     */
    public List<InventoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Calculates the total number of individual plants across all batches.
     */
    public int getTotalPopulationSize() {
        return entries.stream().mapToInt(InventoryEntry::getQuantity).sum();
    }

    /**
     * Identifies the largest physical radius present in the inventory for validation purposes.
            * @return The maximum radius found, or 0.0 if empty.
     */
    public double getMaxRadius() {
        return entries.stream()
                .mapToDouble(InventoryEntry::getRadius)
                .max()
                .orElse(0.0);
    }

    /**
     * Flattens the inventory batches into a sequential list of individual plant definitions.
     * <p>
     * <strong>Usage:</strong> This method is critical for initializing the Genetic Algorithm's chromosome,
     * expanding {@code {Variety A, 2}} into {@code [Variety A, Variety A]}.
     * </p>
     *
     * @return A list containing one {@link PlantVarietySheet} reference for every individual plant.
     */
    public List<PlantVarietySheet> getAllIndividualPlants() {
        List<PlantVarietySheet> flatList = new ArrayList<>();

        for (InventoryEntry entry : this.entries) {
            for (int i = 0; i < entry.getQuantity(); i++) {
                flatList.add(entry.getVarietySheet());
            }
        }
        return flatList;
    }

    /**
     * Reconstructs an inventory object from an existing genetic solution.
     * <p>
     * This factory method allows the system to reverse-engineer the "requested inventory"
     * from a saved simulation result, facilitating data export and review.
     * </p>
     *
     * @param solution        The loaded solution containing the chromosome.
     * @param availableSheets The reference list of all known varieties.
     * @return A reconstructed {@link PlantInventory}.
     */
    public static PlantInventory fromSolution(Individual solution,
                                              List<PlantVarietySheet> availableSheets) {
        PlantInventory inv = new PlantInventory();

        // 1. Grouping and Counting by Variety ID
        Map<Integer, Long> counts = solution.getChromosomes().stream()
                .collect(Collectors.groupingBy(
                        Point::getVarietyId,
                        Collectors.counting()
                ));

        // 2. Rebuilding Entries
        for (PlantVarietySheet sheet : availableSheets) {
            int qty = counts.getOrDefault(sheet.getId(), 0L).intValue();
            if (qty > 0) {
                try {
                    inv.addEntry(sheet, qty);
                } catch (InvalidInputException _) {
                    // Suppress exceptions during reconstruction of validated data
                }
            }
        }
        return inv;
    }
}
