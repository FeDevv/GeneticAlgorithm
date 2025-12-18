package org.agroplanner.domainsystem.model;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

/**
 * <p><strong>Catalog of Supported Geometric Domains.</strong></p>
 *
 * <p>This enum acts as a centralized <strong>Metadata Repository</strong> for the domain subsystem.
 * It provides essential configuration data to other components without containing business logic:</p>
 * <ul>
 * <li><strong>For the View:</strong> It provides the {@code menuId} for selection and the {@code displayName} for output.</li>
 * <li><strong>For the Controller/View:</strong> It dictates which parameters (keys) must be collected from the user via {@code requiredParameters}.</li>
 * <li><strong>For the Factory:</strong> It acts as the key to dispatch the correct object instantiation.</li>
 * </ul>
 */
public enum DomainType {

    // ------------------- ENUM CONSTANTS -------------------

    /** Represents a circular domain defined by a radius. */
    CIRCLE(1,"CIRCLE", List.of("radius")),

    /** Represents a rectangular domain defined by width and height. */
    RECTANGLE(2,"RECTANGLE", List.of("width", "height")),

    /** Represents a square domain defined by side length. */
    SQUARE (3, "SQUARE", List.of("side")),

    /** Represents an elliptical domain defined by semi-width (x-axis) and semi-height (y-axis). */
    ELLIPSE(4, "ELLIPSE", List.of("semi-width", "semi-height")),

    /**
     * Represents a right-angled triangle.
     * Defined by base (along x-axis) and height (along y-axis) starting from origin (0,0).
     */
    RIGHT_ANGLED_TRIANGLE(5, "RIGHT TRIANGLE", List.of("base", "height")),

    /**
     * Represents a rectangular frame (picture frame).
     * Defined by inner and outer dimensions.
     */
    FRAME(6, "FRAME", List.of("innerWidth", "innerHeight","outerWidth", "outerHeight")),

    /**
     * Represents an annulus (ring).
     * Defined by an inner and an outer radius.
     */
    ANNULUS(7, "ANNULUS", List.of("innerRadius", "outerRadius"));

    // ------------------- FIELDS -------------------

    /** Unique numeric identifier used for CLI menu selection. */
    private final int menuId;

    /** User-friendly name used for display in UI logs and prompts. */
    private final String displayName;

    /**
     * The list of parameter keys (strings) that must be present in the configuration map
     * to successfully instantiate this domain via the Factory.
     */
    private final List<String> requiredParameters;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the domain type metadata.
     *
     * @param menuId             The unique ID for menu selection.
     * @param displayName        The name to display.
     * @param requiredParameters The immutable list of required parameter keys.
     */
    DomainType(int menuId, String displayName, List<String> requiredParameters) {
        this.menuId = menuId;
        this.displayName = displayName;
        this.requiredParameters = requiredParameters;
    }

    // ------------------- PUBLIC GETTERS -------------------

    /**
     * Retrieves the numeric ID for menu selection.
     * @return The unique integer ID.
     */
    public int getMenuId() { return menuId; }

    /**
     * Retrieves the list of parameter keys required to instantiate this domain.
     * <p>
     * This list is the <strong>Contract</strong> that the View must fulfill when collecting input
     * and that the Factory validates against.
     * </p>
     *
     * @return An immutable list of strings representing parameter names.
     */
    public List<String> getRequiredParameters() {
        return this.requiredParameters;
    }

    /**
     * Retrieves the user-friendly name of the domain.
     * @return The display name string.
     */
    public String getDisplayName() {
        return this.displayName;
    }

    // ------------------- UTILITY METHODS -------------------

    /**
     * Resolves a {@link DomainType} from a numeric menu ID.
     * <p>
     * Used by the View/Controller to map raw user input (int) to a specific domain enum.
     * </p>
     *
     * @param id The numeric ID entered by the user.
     * @return An {@link Optional} containing the matching {@code DomainType}, or empty if not found.
     */
    public static Optional<DomainType> fromMenuId(int id) {
        return Arrays.stream(DomainType.values())
                .filter(type -> type.menuId == id)
                .findFirst();
    }

    /**
     * Returns the user-friendly display name.
     * Useful for printing the object directly in logs or UI messages.
     */
    @Override
    public String toString() {
        return this.displayName;
    }
}
