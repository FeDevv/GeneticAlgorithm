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
     */
    public void runExportWizard(Individual solution, Domain domain, PlantInventory inventory) {

        // --- RETRY LOOP (Wizard Style) ---
        // Keeps the user inside the export flow until success or explicit cancellation.
        while (true) {

            // STEP 1: Select Format
            view.showAvailableExports(service.getAvailableExportTypes());
            Optional<ExportType> selectedType = view.askForExportType(service.getAvailableExportTypes());

            // If user cancels (returns Empty), exit the wizard.
            if (selectedType.isEmpty()) return;

            // STEP 2: Input Filename
            String filename = view.askForFilename();

            // --- NEW STEP: OVERWRITE CHECK ---
            // Prima di provare a salvare, chiediamo al Service se il file c'è già.
            // Nota: checkFileExists userà l'exporter corretto per capire l'estensione (.csv, .xlsx)
            boolean fileExists = service.checkFileExists(selectedType.get(), filename);

            if (fileExists) {
                // Se esiste, la View chiede cosa fare
                boolean overwrite = view.askOverwriteOrRename(filename);

                if (!overwrite) {
                    // Se l'utente sceglie "Rename" (false), saltiamo il resto del loop.
                    // Il while ricomincerà da capo chiedendo il formato e il nome.
                    continue;
                }
                // Se sceglie "Overwrite" (true), il codice prosegue verso STEP 3
                // e il file verrà sovrascritto naturalmente.
            }

            // STEP 3: Execution & Feedback
            try {
                // We pass the full INVENTORY so the exporter can write metadata (e.g. "Requested: 50 Tomatoes").
                // Delegate to Service (Deep Protection & IO Handling happen there)
                String savedPath = service.performExport(solution, domain, inventory, selectedType.get(), filename);

                // Success Feedback
                view.showSuccessMessage(savedPath);
                return; // Exit loop on success

            } catch (InvalidInputException e) {
                // Recoverable User Error (e.g., bad characters in filename).
                view.showErrorMessage("Input Error: " + e.getMessage());
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
