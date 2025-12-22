package org.agroplanner.inventory.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Enumeration defining the canonical set of plant species supported by the AgroPlanner domain.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Role:</strong> Acts as a strong-typed identifier (Domain Constant) for biological entities.</li>
 * <li><strong>Separation of Concerns:</strong> This class isolates the <em>Identity</em> of a species
 * from its <em>Physical Configuration</em>. Biological parameters (e.g., cultivation radius, growth rate)
 * are excluded from this Enum to allow runtime configuration via external inputs (Database/User Form),
 * preventing hard-coded constraints within the compiled code.</li>
 * </ul>
 */
public enum PlantType {
    TOMATO(1, "TOMATO", "🍅"),
    CORN(2, "CORN", "🌽"),
    POTATO(3, "POTATO", "🥔"),
    CARROT(4, "CARROT", "🥕"),
    WHEAT(5, "WHEAT", "🌾"),
    ZUCCHINI(6, "ZUCCHINI", "🥒"),
    PUMPKIN(7, "PUMPKIN", "🎃");

    /**
     * The internal unique numeric identifier used for persistence and logic mapping.
     */
    private final int id;

    /**
     * The system-internal name of the species.
     */
    private final String name;

    /**
     * The UTF-8 emoji used for UI representation.
     */
    private final String label;

    PlantType(int id, String name, String label) {
        this.id = id;
        this.name = name;
        this.label = label;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Retrieves a {@code PlantType} by its numeric identifier using a functional approach.
     *
     * <p><strong>Implementation Note:</strong></p>
     * Utilizes the Stream API to filter the enum constants. Returns an {@link Optional} container
     * to explicitly handle the case where an invalid ID is provided, enforcing null-safety
     * at the API level (Defensive Programming).
     *
     * @param id The numeric identifier to search for.
     * @return An {@code Optional<PlantType>} containing the matching constant, or empty if not found.
     */
    public static Optional<PlantType> getById(int id) {
        return Arrays.stream(values())
                .filter(type -> type.id == id)
                .findFirst();
    }
}
