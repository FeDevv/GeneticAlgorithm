package org.agroplanner.exportsystem.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * <p><strong>Catalog of Supported Export Formats.</strong></p>
 *
 * <p>This enum defines the available output strategies for the generated solutions.
 * Like {@code DomainType}, it acts as a centralized <strong>Metadata Repository</strong> used by:</p>
 * <ul>
 * <li><strong>UI (View):</strong> To render selection menus via {@code getMenuId()} and {@code getDisplayName()}.</li>
 * <li><strong>Factory:</strong> To dispatch the correct {@code ExportStrategy} implementation.</li>
 * </ul>
 */
public enum ExportType {

    // ------------------- ENUM CONSTANTS -------------------

    /** Comma Separated Values */
    CSV(1, "CSV (Comma Separated Values)"),

    /** Microsoft Excel (.xlsx) */
    EXCEL(2, "Excel (Excel file)"),

    /** Plain Text (.txt) */
    TXT(3, "TXT (Plain text report)"),

    /** JavaScript Object Notation */
    JSON(4, "Json (JavaScript Object Notation)"),

    /** Portable Document Format */
    PDF(5, "PDF (Portable Document Format)");

    // ------------------- FIELDS -------------------

    /** Unique numeric identifier used for CLI menu selection. */
    private final int menuId;

    /** User-friendly name used for display in UI logs and prompts. */
    private final String displayName;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the export type metadata.
     *
     * @param menuId      The unique selection ID.
     * @param displayName The label shown to the user.
     */
    ExportType(int menuId, String displayName) {
        this.menuId = menuId;
        this.displayName = displayName;
    }

    /**
     * Retrieves the numeric ID for menu selection.
     * @return The unique integer ID.
     */
    public int getMenuId() {
        return menuId;
    }

    /**
     * Retrieves the user-friendly name of the format.
     * @return The display string.
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Resolves an {@link ExportType} from a numeric menu ID.
     * <p>
     * Used by the View/Controller to map raw user input (int) to a specific export enum.
     * </p>
     *
     * @param id The numeric ID entered by the user.
     * @return An {@link Optional} containing the matching type, or empty if not found.
     */
    public static Optional<ExportType> fromMenuId(int id) {
        return Arrays.stream(ExportType.values())
                .filter(type -> type.menuId == id)
                .findFirst();
    }

    @Override
    public String toString() {
        return this.displayName;
    }
}
