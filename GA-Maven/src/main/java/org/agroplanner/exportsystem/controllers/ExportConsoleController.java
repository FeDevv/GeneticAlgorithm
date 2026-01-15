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
 * <p>
 * This class orchestrates the export workflow, mediating between the user (View)
 * and the file system operations (Service), ensuring data integrity and handling
 * overwrite conflicts gracefully.
 * </p>
 */
public class ExportConsoleController {

    private final ExportViewContract view;
    private final ExportService service;

    /**
     * Initializes the controller with the necessary view and service dependencies.
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
     * <p>
     * Implements a stateful <strong>Recovery Loop</strong>: user input errors or system failures
     * trigger a retry mechanism, allowing the user to correct parameters (e.g., filename)
     * without losing the current session context.
     * </p>
     *
     * @param solution   The computed solution (Genotype/Phenotype) to save.
     * @param domain     The geometric context of the simulation.
     * @param inventory  The biological metadata (Inventory).
     * @param isDemoMode Flag indicating if the user is in restricted Guest mode.
     */
    public void runExportWizard(Individual solution, Domain domain, PlantInventory inventory, boolean isDemoMode) {

        // 1. SECURITY GATE
        if (isDemoMode) {
            view.showGuestExportRestricted();
            return;
        }

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
                // passing the full INVENTORY so the exporter can write rich metadata headers.
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
