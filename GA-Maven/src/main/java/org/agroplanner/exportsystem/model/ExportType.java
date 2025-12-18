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
    CSV(1, "Raw Data", ".csv"),

    /** Microsoft Excel (.xlsx) */
    EXCEL(2, "Spreadsheet + Charts", ".xlsx"),

    /** Plain Text (.txt) */
    TXT(3, "Simple Text Report", ".txt"),

    /** JavaScript Object Notation */
    JSON(4, "Web/System Integration", ".json"),

    /** Portable Document Format */
    PDF(5, "Printable Report", ".pdf");

    // ------------------- FIELDS -------------------

    /** Unique numeric identifier used for CLI menu selection. */
    private final int menuId;

    /** User-friendly name used for display in UI logs and prompts. */
    private final String info;

    private final String extension;
    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the export type metadata.
     *
     * @param menuId      The unique selection ID.
     * @param info infos.
     */
    ExportType(int menuId, String info, String extension) {
        this.menuId = menuId;
        this.info = info;
        this.extension = extension;
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
    public String getExportInfo() {
        return info;
    }

    /**
     * Retrieves the file extension associated with this format.
     * @return The extension string (e.g., ".xlsx").
     */
    public String getExtension() {
        return extension;
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
        return this.info;
    }
}
