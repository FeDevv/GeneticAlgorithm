package org.agroplanner.inventory.model;

import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.*;

/**
 * Aggregates user-defined botanical selections into a cohesive inventory structure.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Role:</strong> Acts as the data staging area between the User Interface (Input Layer)
 * and the Evolutionary Core. It collects distinct "batches" of plants before the simulation begins.</li>
 * <li><strong>Data Structure:</strong> Utilizes a {@link List} rather than a Map to support <strong>Batch Processing</strong>.
 * This design choice allows multiple entries for the same {@link PlantType} (e.g., a batch of Tomatoes with radius 0.5m
 * and another batch of Tomatoes with radius 0.3m) to coexist without key collisions.</li>
 * </ul>
 */
public class PlantInventory {

    /**
     * Internal storage for inventory batches.
     */
    private final List<InventoryEntry> entries;

    /**
     * Initializes an empty inventory container.
     */
    public PlantInventory() {
        this.entries = new ArrayList<>();
    }

    /**
     * Appends a new production batch to the inventory.
     *
     * <p><strong>Validation Strategy (Fail-Fast):</strong></p>
     * Performs immediate validation of input parameters to prevent the corruption of the domain model.
     * Invalid inputs are rejected before object instantiation occurs.
     *
     * @param type     The botanical species identifier.
     * @param quantity The number of individuals in this batch (must be positive).
     * @param radius   The physical radius required for each plant in this batch (must be positive).
     * @throws InvalidInputException if any parameter violates logical or physical constraints.
     */
    public void addEntry(PlantType type, int quantity, double radius) throws InvalidInputException {
        // Validation logic
        if (type == null) throw new InvalidInputException("PlantType cannot be null.");
        if (quantity <= 0) throw new InvalidInputException("Quantity must be positive.");
        if (radius <= 0) throw new InvalidInputException("Radius must be positive.");

        // State mutation
        entries.add(new InventoryEntry(type, quantity, radius));
    }

    /**
     * Retrieves the list of configured plant batches.
     *
     * <p><strong>Encapsulation:</strong></p>
     * Returns an {@link Collections#unmodifiableList(List)}. This creates a read-only view of the internal state,
     * preventing external consumers (like View or Service layers) from modifying the inventory contents directly.
     *
     * @return An unmodifiable list of {@link InventoryEntry} objects.
     */
    public List<InventoryEntry> getEntries() {
        return Collections.unmodifiableList(entries);
    }

    /**
     * Calculates the total number of individual plants across all batches.
     *
     * <p><strong>Implementation:</strong></p>
     * Uses a Map-Reduce stream operation to sum the quantities. This metric is essential for
     * initializing the Chromosome size in the Genetic Algorithm.
     *
     * @return The total population count.
     */
    public int getTotalPopulationSize() {
        return entries.stream().mapToInt(InventoryEntry::getQuantity).sum();
    }

    /**
     * Identifies the largest physical radius present in the inventory.
     *
     * <p><strong>Usage:</strong></p>
     * Used for pre-flight validation checks (e.g., ensuring the largest plant can physically fit
     * within the selected geometric domain).
     *
     * @return The maximum radius value found, or 0.0 if the inventory is empty.
     */
    public double getMaxRadius() {
        return entries.stream()
                .mapToDouble(InventoryEntry::getRadius)
                .max()
                .orElse(0.0);
    }
}
