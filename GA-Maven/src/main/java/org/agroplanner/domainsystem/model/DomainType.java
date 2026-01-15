package org.agroplanner.domainsystem.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * Registry of supported geometric domain types.
 * <p>
 * Acts as the single source of truth for domain metadata, enabling dynamic UI generation
 * and schema validation without hardcoding field names in the View layer.
 * </p>
 */
public enum DomainType {

    // --- ENUM CONSTANTS (METADATA) ---

    /**
     * A circular domain defined by a single radius.
     * <p>Parameters: {@code radius}</p>
     */
    CIRCLE(1,"CIRCLE", List.of("radius")),

    /**
     * A rectangular domain defined by orthogonal width and height.
     * <p>Parameters: {@code width}, {@code height}</p>
     */
    RECTANGLE(2,"RECTANGLE", List.of("width", "height")),

    /**
     * A regular quadrilateral defined by a single side length.
     * <p>Parameters: {@code side}</p>
     */
    SQUARE (3, "SQUARE", List.of("side")),

    /**
     * An elliptical domain defined by semi-axes.
     * <p>Parameters: {@code semi-width}, {@code semi-height}</p>
     */
    ELLIPSE(4, "ELLIPSE", List.of("semi-width", "semi-height")),

    /**
     * A right-angled triangle aligned with the Cartesian axes.
     * <p>Parameters: {@code base}, {@code height}</p>
     */
    RIGHT_ANGLED_TRIANGLE(5, "RIGHT TRIANGLE", List.of("base", "height")),

    /**
     * A hollow rectangular region (picture frame) defined by inner and outer bounds.
     * <p>Parameters: {@code innerWidth}, {@code innerHeight}, {@code outerWidth}, {@code outerHeight}</p>
     */
    FRAME(6, "FRAME", List.of("innerWidth", "innerHeight","outerWidth", "outerHeight")),

    /**
     * A ring-shaped region defined by two concentric circles.
     * <p>Parameters: {@code innerRadius}, {@code outerRadius}</p>
     */
    ANNULUS(7, "ANNULUS", List.of("innerRadius", "outerRadius"));

    // ------------------- FIELDS -------------------

    /**
     * The unique numeric identifier used for CLI menu routing.
     */
    private final int menuId;

    /**
     * The human-readable label used for UI presentation.
     */
    private final String displayName;

    /**
     * The schema definition for this domain type.
     * Lists the keys that must be present in the configuration map.
     */
    private final List<String> requiredParameters;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Configures the metadata for a specific domain type.
     *
     * @param menuId             The selection ID.
     * @param displayName        The UI label.
     * @param requiredParameters The immutable list of mandatory parameter keys.
     */
    DomainType(int menuId, String displayName, List<String> requiredParameters) {
        this.menuId = menuId;
        this.displayName = displayName;
        this.requiredParameters = requiredParameters;
    }

    // ------------------- PUBLIC GETTERS -------------------

    /**
     * Retrieves the numeric selection ID.
     * @return The integer ID.
     */
    public int getMenuId() { return menuId; }

    /**
     * Retrieves the parameter schema required to instantiate this domain.
     *
     * <p><strong>Usage:</strong></p>
     * <ul>
     * <li><strong>View Layer:</strong> Iterates over this list to prompt the user for specific values
     * (e.g., "Enter width", "Enter height").</li>
     * <li><strong>Factory Layer:</strong> Validates the input Map against these keys to ensure completeness
     * before invoking the domain constructor.</li>
     * </ul>
     *
     * @return An immutable list of parameter names.
     */
    public List<String> getRequiredParameters() {
        return this.requiredParameters;
    }

    /**
     * Retrieves the display name.
     * @return The string representation for the UI.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    // ------------------- UTILITY METHODS -------------------

    /**
     * Resolves a {@link DomainType} from its numeric menu identifier.
     *
     * @param id The numeric ID to lookup.
     * @return An {@link Optional} containing the match, or empty if no match exists.
     */
    public static Optional<DomainType> fromMenuId(int id) {
        return Arrays.stream(DomainType.values())
                .filter(type -> type.menuId == id)
                .findFirst();
    }

    /**
     * Returns the display name of the domain.
     *
     * @return The {@code displayName} field.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}
