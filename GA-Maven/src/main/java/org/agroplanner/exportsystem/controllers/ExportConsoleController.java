package org.agroplanner.exportsystem.controllers;

import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.exportsystem.views.ExportViewContract;

import java.util.Optional;

/**
 * Controller component managing the interactive "Export Wizard" session.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Wizard Controller. It guides the user through a multi-step process
 * (Format Selection &rarr; Filename Entry &rarr; Overwrite Check &rarr; Persist), ensuring data integrity at each step.</li>
 * <li><strong>Responsibility:</strong> Orchestrates the interaction between the User (View) and the File System (Service).
 * It acts as an error barrier, catching exceptions locally to allow retries without losing the calculated solution.</li>
 * </ul>
 */
public class ExportConsoleController {

    private final ExportViewContract view;
    private final ExportService service;

    /**
     * Initializes the controller with required dependencies.
     *
     * @param view    The UI handler for prompts and feedback.
     * @param service The business logic handler for file operations.
     */
    public ExportConsoleController(ExportViewContract view, ExportService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Launches the interactive export session.
     *
     * <p><strong>Workflow (Recovery Loop):</strong></p>
     * Implements a stateful loop that persists until either a successful export occurs or the user explicitly cancels.
     * <ol>
     * <li><strong>Format Selection:</strong> Prompt user for file type (CSV, Excel, etc.).</li>
     * <li><strong>Filename Entry:</strong> Prompt user for target name.</li>
     * <li><strong>Collision Detection:</strong> Check if file exists. If so, ask user to Overwrite or Rename.</li>
     * <li><strong>Execution:</strong> Attempt write operation via Service.</li>
     * <li><strong>Error Handling:</strong> Catch exceptions (Invalid Name, Disk Error) and display feedback,
     * allowing the user to try again immediately.</li>
     * </ol>
     *
     * @param solution  The computed solution to save.
     * @param domain    The geometric context.
     * @param inventory The biological context (metadata).
     */
    public void runExportWizard(Individual solution, Domain domain, PlantInventory inventory) {

        // --- INTERACTION LOOP ---
        // Keeps the user inside the wizard until success or cancellation.
        while (true) {

            // STEP 1: Format Selection
            view.showAvailableExports(service.getAvailableExportTypes());
            Optional<ExportType> selectedType = view.askForExportType(service.getAvailableExportTypes());

            // Exit Condition: User chose Cancel
            if (selectedType.isEmpty()) return;

            // STEP 2: Filename Entry
            String filename = view.askForFilename();

            // STEP 3: Collision Detection (Overwrite Protection)
            // Pre-check if the target file already exists to prevent accidental data loss.
            boolean fileExists = service.checkFileExists(selectedType.get(), filename);

            if (fileExists) {
                // Interactive Resolution: Ask user decision
                boolean overwrite = view.askOverwriteOrRename(filename);

                if (!overwrite) {
                    // User chose "Rename". Restart loop to ask for filename again.
                    continue;
                }
                // User chose "Overwrite". Proceed to save.
            }

            // STEP 4: Execution & Recovery
            try {
                // Delegate heavy lifting to Service layer.
                // We pass the full INVENTORY so the exporter can write rich metadata headers.
                String savedPath = service.performExport(solution, domain, inventory, selectedType.get(), filename);

                // Success State: Show feedback and break the loop.
                view.showSuccessMessage(savedPath);
                return; // Exit loop on success

            } catch (InvalidInputException e) {
                // Recoverable User Error (e.g., filename contains invalid chars like '?').
                // Show error and loop again to let user correct the input.
                view.showErrorMessage("Input Error: " + e.getMessage());

            } catch (ExportException e) {
                // Recoverable System Error (e.g., Disk Full, Permission Denied).
                // Show error and loop again (user might choose a different drive or format).
                view.showErrorMessage("Export Error: " + e.getMessage());

            } catch (Exception e) {
                // Safety Net for unexpected runtime bugs.
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
            }
        }
    }
}
