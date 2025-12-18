package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;

/**
 * <p><strong>View Contract for the Export Subsystem.</strong></p>
 *
 * <p>This interface defines the interactions required to perform a file export.
 * It abstracts the specific UI implementation (Console, GUI, etc.) from the Controller logic.</p>
 */
public interface ExportViewContract {

    void showAvailableExports(List<ExportType> types);

    /**
     * Prompts the user to select an export format from a list of available options.
     *
     * @param availableTypes The list of supported {@link ExportType}s to display.
     * @return An {@link Optional} containing the selected type, or {@code Optional.empty()}
     * if the user chooses to cancel/skip the export process.
     */
    Optional<ExportType> askForExportType(List<ExportType> availableTypes);

    /**
     * Prompts the user to enter a name for the destination file.
     * <p>
     * <strong>Note:</strong> The user is expected to enter only the name (e.g., "results").
     * The file extension is automatically handled by the specific Exporter.
     * </p>
     *
     * @return The string entered by the user.
     */
    String askForFilename();

    /**
     * Displays a success notification, informing the user where the file was saved.
     *
     * @param filePath The absolute path of the generated file.
     */
    void showSuccessMessage(String filePath);

    /**
     * Displays an error message to the user.
     * <p>
     * Used for both validation errors (e.g., invalid characters in filename)
     * and system errors (e.g., disk full, permission denied).
     * </p>
     *
     * @param message The error details to display.
     */
    void showErrorMessage(String message);

    boolean askOverwriteOrRename(String filename);
}
