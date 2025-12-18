package org.agroplanner.inventory.model;

public class InventoryEntry {
    private final PlantType type;
    private final int quantity;
    private final double radius;

    public InventoryEntry(PlantType type, int quantity, double radius) {
        this.type = type;
        this.quantity = quantity;
        this.radius = radius;
    }

    public PlantType getType() { return type; }
    public int getQuantity() { return quantity; }
    public double getRadius() { return radius; }
}
