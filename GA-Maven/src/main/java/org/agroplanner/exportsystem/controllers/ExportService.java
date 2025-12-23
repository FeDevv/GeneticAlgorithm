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
 * Service component responsible for the business logic of data persistence.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Facade / Service Layer. It sits between the UI Controller and the complex
 * Factory/Strategy logic, providing a simplified API for exporting data.</li>
 * <li><strong>Responsibility:</strong> Handles Input Sanitization (Security), Lifecycle Management,
 * and Error Handling (Exception Translation).</li>
 * <li><strong>Statelessness:</strong> This class contains no mutable state, making it thread-safe and
 * cheap to instantiate (default constructor).</li>
 * </ul>
 */
public class ExportService {

    /**
     * Retrieves the catalog of supported export formats.
     * @return A list of all available {@link ExportType}s.
     */
    public List<ExportType> getAvailableExportTypes() {
        return Arrays.asList(ExportType.values());
    }

    /**
     * Orchestrates the export workflow with rigorous validation and error handling.
     *
     * <p><strong>Security Strategy (Input Sanitization):</strong></p>
     * Implements "Deep Protection" by validating the filename against a whitelist/blacklist regex.
     * This prevents:
     * <ul>
     * <li><strong>Filesystem Corruption:</strong> Using reserved characters (e.g., {@code :}, {@code *}, {@code ?}).</li>
     * <li><strong>Directory Traversal:</strong> Using {@code ../} or {@code /} to write outside the sandbox (though strict regex catches this).</li>
     * </ul>
     *
     * <p><strong>Exception Translation:</strong></p>
     * Catches low-level {@link IOException} (Checked) and re-throws high-level {@link ExportException} (Unchecked).
     * This keeps the Controller clean from {@code try-catch} blocks related to disk I/O details.
     *
     * @param solution  The genotype/phenotype data to persist.
     * @param domain    The geometric context.
     * @param inventory The biological context.
     * @param type      The desired output format.
     * @param filename  The user-provided target name.
     * @return The absolute path of the generated artifact.
     * @throws InvalidInputException If inputs are null, empty, or contain illegal characters.
     * @throws ExportException       If the underlying write operation fails (Disk full, Permission denied).
     */
    public String performExport(Individual solution, Domain domain, PlantInventory inventory, ExportType type, String filename) {

        // --- PHASE 1: DEEP PROTECTION (Validation) ---
        // Even if the View performs checks, the Service layer acts as the final gatekeeper.
        if (filename == null || filename.isBlank()) {
            throw new InvalidInputException("Filename cannot be empty or null.");
        }

        // Security Regex: Blocks common illegal filesystem characters (< > : " / \ | ? *).
        if (filename.matches(".*[<>:\"/\\\\|?*].*")) {
            throw new InvalidInputException("Filename contains illegal characters (< > : \" / \\ | ? *).");
        }

        if (solution == null) {
            throw new InvalidInputException("Cannot export a null solution.");
        }
        if (inventory == null) {
            throw new InvalidInputException("PlantInventory cannot be null.");
        }
        if (inventory.getTotalPopulationSize() == 0) {
            throw new InvalidInputException("Inventory is empty, nothing to report.");
        }

        // --- PHASE 2: EXECUTION & ERROR WRAPPING ---
        try {
            // 1. Factory Dispatch: Get the correct strategy
            BaseExporter exporter = ExporterFactory.getInstance().createExporter(type);

            // 2. Template Method Execution: Delegate logic to the exporter
            return exporter.export(solution, domain, inventory, filename);

        } catch (IOException e) {
            // --- EXCEPTION TRANSLATION ---
            // Convert technical failure (IO) into domain failure (ExportException).
            throw new ExportException("Failed to save file to disk. Check permissions or disk space.", e);

        } catch (Exception e) {
            // Safety Net: Catch runtime bugs inside specific exporter implementations.
            throw new ExportException("Unexpected error during export process: " + e.getMessage(), e);
        }
    }

    /**
     * Pre-checks file existence to enable "Overwrite Confirmation" logic in the UI.
     *
     * <p><strong>Logic:</strong></p>
     * Replicates the path resolution logic of {@link BaseExporter} to check the exact target location
     * without actually opening/creating the file.
     *
     * @param type     The export format (determines the extension).
     * @param filename The raw user input.
     * @return {@code true} if a file collision is detected, {@code false} otherwise.
     */
    public boolean checkFileExists(ExportType type, String filename) {
        if (filename == null || filename.isBlank()) return false;

        // 1. Resolve Extension
        String extension = type.getExtension();

        // 2. Normalize Filename
        if (!filename.toLowerCase().endsWith(extension)) {
            filename += extension;
        }

        // 3. Construct Path (using the shared constant)
        Path targetPath = Paths.get(BaseExporter.EXPORT_FOLDER, filename);

        // 4. Check Existence
        return Files.exists(targetPath);
    }

}
