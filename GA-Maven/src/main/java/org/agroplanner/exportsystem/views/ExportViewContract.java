package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;

/**
 * Defines the abstract contract for the Presentation Layer of the Data Export subsystem.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> View Interface (Dependency Inversion). It decouples the {@link org.agroplanner.exportsystem.controllers.ExportConsoleController}
 * from the specific input/output mechanism (CLI, Swing, Web).</li>
 * <li><strong>Role:</strong> Manages the "Export Wizard" interaction flow, including format selection,
 * filename entry, and critical decision-making (collision resolution).</li>
 * </ul>
 */
public interface ExportViewContract {

    /**
     * Renders the list of supported export formats to the user.
     * @param types The list of available {@link ExportType}s from the model.
     */
    void showAvailableExports(List<ExportType> types);

    /**
     * Captures the user's preferred output format.
     *
     * <p><strong>Flow Control:</strong></p>
     * Returns an {@link Optional} to represent the "Exit/Cancel" scenario gracefully.
     * If the user chooses to abort the wizard, this returns {@code empty()}, allowing the Controller
     * to terminate the loop cleanly without using exceptions or null checks.
     *
     * @param availableTypes The list of valid options.
     * @return The selected type, or {@code empty()} if cancelled.
     */
    Optional<ExportType> askForExportType(List<ExportType> availableTypes);

    /**
     * Captures the desired filename for the export.
     *
     * <p><strong>UX Contract:</strong></p>
     * The view must inform the user that the file extension should be omitted,
     * as it is handled automatically by the backend Strategy based on the selected format.
     *
     * @return The raw filename string entered by the user.
     */
    String askForFilename();

    /**
     * Displays visual confirmation of the successful persistence operation.
     *
     * @param filePath The absolute file system path of the generated artifact.
     */
    void showSuccessMessage(String filePath);

    /**
     * Renders error feedback to the user.
     * Used for both validation errors (e.g., bad characters) and system exceptions (e.g., IO failure).
     *
     * @param message The descriptive error text.
     */
    void showErrorMessage(String message);

    /**
     * Handles file name collision scenarios (Data Safety).
     *
     * <p><strong>Protocol:</strong></p>
     * Triggered when the Controller detects that the target file already exists.
     * The View must present the user with a choice:
     * <ul>
     * <li><strong>True (Overwrite):</strong> The user explicitly authorizes data destruction/replacement.</li>
     * <li><strong>False (Rename/Cancel):</strong> The user wants to preserve the existing file.</li>
     * </ul>
     *
     * @param filename The name of the conflicting file.
     * @return {@code true} to proceed with overwrite, {@code false} to abort/rename.
     */
    boolean askOverwriteOrRename(String filename);
}
