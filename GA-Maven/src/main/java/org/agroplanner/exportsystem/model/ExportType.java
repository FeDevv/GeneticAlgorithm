package org.agroplanner.exportsystem.model;

import java.util.Arrays;
import java.util.Optional;

/**
 * Defines the catalog of supported output formats.
 *
 * <p><strong>Architecture & Role:</strong></p>
 * This Enum acts as a centralized <strong>Metadata Registry</strong> used across the Export MVC layers:
 * <ul>
 * <li><strong>View Layer:</strong> Uses {@code menuId} and {@code info} to render user-friendly selection menus.</li>
 * <li><strong>Controller Layer:</strong> Uses {@code fromMenuId()} to map raw user input to strong types.</li>
 * <li><strong>Service Layer:</strong> Uses {@code extension} to configure file system I/O writers.</li>
 * </ul>
 */
public enum ExportType {

    // ------------------- ENUM CONSTANTS -------------------

    /**
     * Raw Data Format.
     * Best for simple data exchange or importing into other lightweight tools.
     */
    CSV(1, "Raw Data", ".csv"),

    /**
     * Spreadsheet Format (Microsoft Excel).
     * Includes formatting and potential for charts/formulas. Best for end-user analytics.
     */
    EXCEL(2, "Spreadsheet + Charts", ".xlsx"),

    /**
     * Human-Readable Text Report.
     * Best for quick console inspection or simple logs.
     */
    TXT(3, "Simple Text Report", ".txt"),

    /**
     * Machine-Readable Format (JSON).
     * Best for interoperability, web integration, or REST API payloads.
     */
    JSON(4, "Web/System Integration", ".json"),

    /**
     * Portable Document Format.
     * Best for finalized, printable reports that must maintain visual integrity.
     */
    PDF(5, "Printable Report", ".pdf");

    // ------------------- FIELDS -------------------

    /**
     * The unique numeric key used for CLI menu binding.
     */
    private final int menuId;

    /**
     * The descriptive label shown in the User Interface.
     */
    private final String info;

    /**
     * The file system suffix associated with this format.
     * Used by the writer strategy to generate the correct filename.
     */
    private final String extension;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Configures the export format metadata.
     *
     * @param menuId    The unique selection ID.
     * @param info      The display name description.
     * @param extension The file extension (including the dot).
     */
    ExportType(int menuId, String info, String extension) {
        this.menuId = menuId;
        this.info = info;
        this.extension = extension;
    }

    // ------------------- ACCESSORS -------------------

    /**
     * Retrieves the numeric ID for menu selection.
     * @return The unique integer ID.
     */
    public int getMenuId() {
        return menuId;
    }

    /**
     * Retrieves the user-friendly description.
     * @return The display string.
     */
    public String getExportInfo() {
        return info;
    }

    /**
     * Retrieves the standard file extension.
     * @return The suffix string (e.g., ".xlsx").
     */
    public String getExtension() {
        return extension;
    }

    // ------------------- LOOKUP UTILITIES -------------------

    /**
     * Resolves an {@link ExportType} from a numeric menu ID.
     *
     * <p><strong>Usage:</strong></p>
     * Used by the Controller to safely translate raw integer input from the View into a
     * domain-specific Enum constant.
     *
     * @param id The numeric ID entered by the user.
     * @return An {@link Optional} containing the matching type, or {@code empty()} if the ID is invalid.
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
