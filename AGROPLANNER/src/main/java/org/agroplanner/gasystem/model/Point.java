package org.agroplanner.gasystem.model;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.exceptions.InvalidInputException;

/**
 * Immutable representation of a geometric entity in 2D space, functioning as the <strong>Gene</strong>
 * within the evolutionary algorithm.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Immutable Value Object.</li>
 * <li><strong>Role (The Gene):</strong> Represents an atomic, indivisible unit of the solution genotype.
 * A collection of these points forms the {@link Individual} (Chromosome).</li>
 * <li><strong>Concurrency & Safety:</strong>
 * Being deeply immutable, instances of this class are inherently <strong>Thread-Safe</strong>.
 * This allows the genetic operators (Crossover, Mutation) to function in parallel streams without
 * synchronization overhead. Furthermore, it prevents <em>Side-Effects</em>: modifying a child's gene
 * implies creating a new object, guaranteeing that the parent's genome remains historically intact.</li>
 * </ul>
 */
public class Point {

    // ------------------- STATE (IMMUTABLE) -------------------
    private final double x;
    private final double y;
    private final double radius;
    private final PlantType type;

    private final int varietyId;
    private final String varietyName;

    // ------------------- CONSTRUCTORS -------------------

    public Point(double x, double y, double radius, PlantType type, int varietyId, String varietyName) {
        // Validazioni standard
        if (Double.isNaN(x) || Double.isNaN(y)) throw new InvalidInputException("Coordinates cannot be NaN.");
        if (radius <= 0) throw new InvalidInputException("Point radius must be positive. Got: " + radius);
        if (type == null) throw new InvalidInputException("PlantType cannot be null.");
        if (varietyName == null) varietyName = "Unknown"; // Safety fallback

        this.x = x;
        this.y = y;
        this.radius = radius;
        this.type = type;
        this.varietyId = varietyId;
        this.varietyName = varietyName;
    }

    // ------------------- ACCESSORS -------------------

    public double getX() { return x; }
    public double getY() { return y; }
    public double getRadius() { return radius; }
    public PlantType getType() { return type; }

    public int getVarietyId() { return varietyId; }
    public String getVarietyName() { return varietyName; }

    // ------------------- OBJECT CONTRACT -------------------

    @Override
    public String toString() {
        return String.format("%s(%s)[id=%d]@(%.2f, %.2f)", type.name(), varietyName, varietyId, x, y);
    }
}
