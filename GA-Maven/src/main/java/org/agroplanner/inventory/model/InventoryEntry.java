package org.agroplanner.inventory.model;

/**
 * Immutable value object representing a batch of homogeneous plants within the inventory.
 * <p>
 * This class creates a composition between a biological definition ({@link PlantVarietySheet})
 * and a requested quantity, serving as the fundamental unit for population generation.
 * </p>
 */
public class InventoryEntry {

    private final PlantVarietySheet varietySheet;
    private final int quantity;

    public InventoryEntry(PlantVarietySheet varietySheet, int quantity) {
        this.varietySheet = varietySheet;
        this.quantity = quantity;
    }

    public PlantVarietySheet getVarietySheet() { return varietySheet; }

    /**
     * Retrieves the high-level species category.
     * @return The {@link PlantType} associated with this entry.
     */
    public PlantType getType() { return varietySheet.getType(); }

    public int getQuantity() { return quantity; }

    /**
     * Retrieves the physical cultivation radius required by this variety.
     * <p>
     * The value is delegated from the underlying {@link PlantVarietySheet} to ensure single source of truth.
     * </p>
     * @return The minimum distance in meters.
     */
    public double getRadius() { return varietySheet.getMinDistance(); }
}
