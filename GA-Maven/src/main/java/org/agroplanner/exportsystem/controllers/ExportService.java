package org.agroplanner.exportsystem.controllers;

import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.exportsystem.model.ExporterFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

/**
 * <p><strong>Business Logic for the Export Subsystem.</strong></p>
 *
 * <p>This service orchestrates the saving of results to disk. It acts as a middleware between the UI Controller
 * and the specific export implementations, ensuring:</p>
 * <ul>
 * <li><strong>Input Sanitation:</strong> Validating filenames against system constraints.</li>
 * <li><strong>Strategy Dispatch:</strong> Selecting the correct format via the Factory.</li>
 * <li><strong>Error Handling:</strong> Translating low-level I/O errors into domain-specific exceptions.</li>
 * </ul>
 */
public class ExportService {

    private final ExporterFactory factory;

    /**
     * Initializes the service by retrieving the Factory instance.
     */
    public ExportService() {
        this.factory = ExporterFactory.getInstance();
    }

    /**
     * Retrieves the list of supported export formats.
     * @return A list of all available {@link ExportType}s.
     */
    public List<ExportType> getAvailableTypes() {
        return Arrays.asList(ExportType.values());
    }

    /**
     * Executes the export process with robust validation and error handling.
     *
     * @param solution The data to save.
     * @param domain   The context.
     * @param type     The selected format (CSV, Excel, etc.).
     * @param filename The target filename (user input).
     * @return The absolute path of the saved file.
     * @throws InvalidInputException If the filename is invalid or malicious.
     * @throws ExportException       If the writing process fails (e.g., disk full, permission denied).
     */
    public String performExport(Individual solution, Domain domain, PlantInventory inventory, ExportType type, String filename) {

        // --- DEEP PROTECTION: Input Sanitation ---
        // Even if the View performs checks, the Service layer must defend itself.
        if (filename == null || filename.isBlank()) {
            throw new InvalidInputException("Filename cannot be empty or null.");
        }

        // Security Check: Prevent directory traversal or illegal characters.
        // Regex matches strictly forbidden characters on Windows/Linux (< > : " / \ | ? *).
        if (filename.matches(".*[<>:\"/\\\\|?*].*")) {
            throw new InvalidInputException("Filename contains illegal characters (< > : \" / \\ | ? *).");
        }

        if (solution == null) {
            throw new InvalidInputException("Cannot export a null solution.");
        }
        if (inventory == null) {
            // Se l'inventario è null, gli exporter falliranno nel loop dei metadati
            throw new InvalidInputException("PlantInventory cannot be null.");
        }
        if (inventory.getTotalPopulationSize() == 0) {
            // Opzionale: impedire export di report vuoti
            throw new InvalidInputException("Inventory is empty, nothing to report.");
        }

        // --- EXECUTION & ERROR WRAPPING ---
        try {
            // Get the specific strategy from the factory
            BaseExporter exporter = factory.createExporter(type);

            // Execute the template method.
            // If export() throws IOException, we catch it here.
            return exporter.export(solution, domain, inventory, filename);

        } catch (IOException e) {
            // --- EXCEPTION TRANSLATION ---
            // Pattern: Wrap the low-level technical exception (IOException) into a high-level
            // domain exception (ExportException). This decouples the caller from java.io details.
            throw new ExportException("Failed to save file to disk. Check permissions or disk space.", e);

        } catch (Exception e) {
            // Safety Net: Catch unexpected bugs inside specific exporters.
            throw new ExportException("Unexpected error during export process: " + e.getMessage(), e);
        }
    }

    /**
     * Checks if a file with the given name and type already exists in the export directory.
     * Used by the Controller to trigger the Overwrite/Rename dialog.
     *
     * @param type     The export format (determines the extension).
     * @param filename The user-provided filename (without extension).
     * @return true if the file exists, false otherwise.
     */
    public boolean checkFileExists(ExportType type, String filename) {
        if (filename == null || filename.isBlank()) return false;

        // 1. Prendi l'estensione direttamente dall'Enum (veloce e pulito)
        String extension = type.getExtension();

        // 2. Normalizza il nome
        if (!filename.toLowerCase().endsWith(extension)) {
            filename += extension;
        }

        // 3. Usa la costante statica di BaseExporter per la cartella
        Path targetPath = Paths.get(BaseExporter.EXPORT_FOLDER, filename);

        return Files.exists(targetPath);
    }

}
