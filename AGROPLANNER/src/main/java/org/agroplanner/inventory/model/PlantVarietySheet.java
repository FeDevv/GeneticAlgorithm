package org.agroplanner.inventory.model;

import org.agroplanner.access.model.User;

/**
 * Domain Entity representing the technical specification of a plant variety.
 * <p>
 * Contains both biological metadata (Type, Name, Sowing Period) and physical constraints
 * (Minimum Distance) required by the layout algorithm.
 * </p>
 */
public class PlantVarietySheet {

    private int id;
    private PlantType type;
    private String varietyName;

    /**
     * The minimum physical distance required between two plants of this variety (radius).
     * Measured in meters.
     */
    private double minDistance;

    private String sowingPeriod;
    private String notes;

    private User author;

    public PlantVarietySheet() {}

    public PlantVarietySheet(PlantType type, String varietyName, double minDistance, User author) {
        this.type = type;
        this.varietyName = varietyName;
        this.minDistance = minDistance;
        this.author = author;
    }

    // GETTERS & SETTERS
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public PlantType getType() { return type; }
    public void setType(PlantType type) { this.type = type; }

    public String getVarietyName() { return varietyName; }
    public void setVarietyName(String varietyName) { this.varietyName = varietyName; }

    public double getMinDistance() { return minDistance; }
    public void setMinDistance(double minDistance) { this.minDistance = minDistance; }

    public String getSowingPeriod() { return sowingPeriod; }
    public void setSowingPeriod(String sowingPeriod) { this.sowingPeriod = sowingPeriod; }

    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }

    public User getAuthor() { return author; }
    public void setAuthor(User author) { this.author = author; }

    @Override
    public String toString() {
        return String.format("%s %s (%s)", type.getLabel(), varietyName, type.getLabel());
    }
}
