package org.agroplanner.inventory.model;

/**
 * Immutable data carrier representing a homogeneous batch of plants within the inventory.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Immutable POJO / Value Object.</li>
 * <li><strong>Role:</strong> Acts as a composite entity (Tuple) that binds a biological identity ({@link PlantType})
 * with a specific quantity and physical dimension.</li>
 * <li><strong>Thread Safety:</strong> Since all fields are {@code final} and the state is set exclusively
 * via the constructor, instances of this class are inherently thread-safe and can be shared across
 * parallel streams in the Evolution Engine without synchronization overhead.</li>
 * </ul>
 *
 * @see PlantInventory
 */
public class InventoryEntry {

    /**
     * The biological classification of the plants in this batch.
     */
    private final PlantType type;

    /**
     * The number of individual organisms to be generated from this entry.
     */
    private final int quantity;

    /**
     * The physical radius assigned to every plant in this batch.
     * <p>Note: This value overrides any default radius associated with the species,
     * allowing for user-defined customization per batch.</p>
     */
    private final double radius;

    /**
     * Constructs a new inventory batch.
     *
     * <p><strong>Design Note:</strong>
     * This constructor does not perform validation. It assumes that data integrity checks
     * (e.g., positive quantity/radius) have already been enforced by the upstream aggregator
     * ({@link PlantInventory#addEntry}).
     * </p>
     *
     * @param type     The species identifier.
     * @param quantity The count of plants.
     * @param radius   The radius in meters.
     */
    public InventoryEntry(PlantType type, int quantity, double radius) {
        this.type = type;
        this.quantity = quantity;
        this.radius = radius;
    }

    /**
     * Retrieves the biological type.
     * @return The {@link PlantType} enum constant.
     */
    public PlantType getType() { return type; }

    /**
     * Retrieves the batch size.
     * @return The quantity of plants.
     */
    public int getQuantity() { return quantity; }

    /**
     * Retrieves the physical constraint.
     * @return The radius in meters.
     */
    public double getRadius() { return radius; }
}
