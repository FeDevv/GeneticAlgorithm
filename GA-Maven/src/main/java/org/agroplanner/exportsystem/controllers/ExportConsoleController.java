package org.agroplanner.exportsystem.controllers;

import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.exportsystem.views.ExportViewContract;

import java.util.Optional;

/**
 * <p><strong>UI Controller for the Export Subsystem.</strong></p>
 *
 * <p>This controller manages the user interaction flow for saving results (The "Export Wizard").
 * It acts as an intermediary between the View (User Input) and the Service (File I/O),
 * providing error recovery loops.</p>
 */
public class ExportConsoleController {

    private final ExportViewContract view;
    private final ExportService service;

    /**
     * Initializes the controller.
     *
     * @param view    The abstraction of the UI.
     * @param service The business logic handler.
     */
    public ExportConsoleController(ExportViewContract view, ExportService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Launches the interactive Export Wizard.
     *
     * <p><strong>Flow:</strong>
     * <ol>
     * <li>Ask user for Export Format (CSV, Excel, etc.).</li>
     * <li>Ask user for Filename.</li>
     * <li>Delegate saving to the Service.</li>
     * <li><strong>Error Handling:</strong> If an error occurs (e.g., invalid name, disk full),
     * show an error message and restart the loop, allowing the user to retry without losing progress.</li>
     * </ol>
     * </p>
     *
     * @param solution The solution data to export.
     * @param domain   The context data.
     * @param radius   The dimension data.
     */
    public void runExportWizard(Individual solution, Domain domain, double radius) {

        // --- RETRY LOOP (Wizard Style) ---
        // Keeps the user inside the export flow until success or explicit cancellation.
        while (true) {

            // STEP 1: Select Format
            Optional<ExportType> selectedType = view.askForExportType(service.getAvailableTypes());

            // If user cancels (returns Empty), exit the wizard.
            if (selectedType.isEmpty()) return;

            // STEP 2: Input Filename
            String filename = view.askForFilename();

            // STEP 3: Execution & Feedback
            try {
                // Delegate to Service (Deep Protection & IO Handling happen there)
                String savedPath = service.performExport(solution, domain, radius, selectedType.get(), filename);

                // Success Feedback
                view.showSuccessMessage(savedPath);
                return; // Exit loop on success

            } catch (InvalidInputException e) {
                // Recoverable User Error (e.g., bad characters in filename).
                view.showErrorMessage("Invalid Filename: " + e.getMessage());
                // Loop continues -> User can try entering a new name.

            } catch (ExportException e) {
                // System/IO Error (e.g., Disk Full, Permission Denied).
                view.showErrorMessage("Export Error: " + e.getMessage());
                // Loop continues -> User can try a different path or format.

            } catch (Exception e) {
                // Unexpected Bug (Safety Net).
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
            }
        }
    }
}
