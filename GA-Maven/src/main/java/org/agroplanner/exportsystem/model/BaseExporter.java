package org.agroplanner.exportsystem.model;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.inventory.model.PlantInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Abstract base class defining the skeleton of an export operation.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Template Method. The {@link #export} method defines the invariant workflow
 * (validation -> preparation -> execution), while specific writing logic is deferred to subclasses
 * via the abstract {@link #performExport} hook.</li>
 * <li><strong>Responsibility:</strong> Centralizes infrastructure concerns such as Directory Management
 * (ensuring folders exist) and Path Normalization (handling extensions), adhering to the <em>Don't Repeat Yourself (DRY)</em> principle.</li>
 * </ul>
 */
public abstract class BaseExporter {

    /**
     * The fixed root directory (Sandbox) for all generated reports.
     */
    public static final String EXPORT_FOLDER = "exports";

    /**
     * Orchestrates the complete export lifecycle (The Template Method).
     *
     * <p><strong>Design Contract:</strong></p>
     * This method is declared {@code final} to strictly enforce the execution order. Subclasses cannot override
     * the safety checks (e.g., directory creation) but must provide the implementation for the writing phase.
     *
     *
     * @param individual The solution data (Genotype/Phenotype).
     * @param domain     The geometric context (Boundaries).
     * @param inventory  The biological context (Plant metadata).
     * @param filename   The raw target filename requested by the user.
     * @return The absolute file system path of the generated artifact (for UI feedback).
     * @throws IOException If any I/O error occurs (FileSystem permission, disk full, etc.).
     */
    public final String export(Individual individual, Domain domain, PlantInventory inventory, String filename) throws IOException {
        // 1. Path Resolution & Normalization
        // Ensures the filename has the correct extension and points to the right folder.
        Path path = resolveFilePath(filename);

        // 2. Environment Preparation (Defensive IO)
        // Automatically creates the directory tree if it doesn't exist.
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 3. Execution (The Hook)
        // Delegates the format-specific writing logic to the concrete implementation.
        performExport(individual, domain, inventory, path);

        return path.toAbsolutePath().toString();
    }

    /**
     * Computes the final target path without performing I/O.
     *
     * <p><strong>Utility:</strong></p>
     * Exposed publicly to allow Controllers/Services to perform "Pre-Flight Checks"
     * (e.g., checking if the file already exists before attempting to write).
     *
     * @param filename The user input.
     * @return A sanitized {@link Path} object with the correct extension appended.
     */
    public Path resolveFilePath(String filename) {
        String extension = getExtension();

        // Extension Normalization:
        // If the user forgot the extension (e.g., "report"), we append it ("report.csv").
        // We use case-insensitive check to be user-friendly.
        if (!filename.toLowerCase().endsWith(extension)) {
            filename += extension;
        }

        // Path Construction:
        // Joins the root folder with the sanitized filename.
        return Paths.get(EXPORT_FOLDER, filename);
    }

    // ------------------- ABSTRACT HOOKS -------------------

    /**
     * Template Hook: Provides the specific file extension for the implementing format.
     * @return The suffix including the dot (e.g., {@code .json}).
     */
    protected abstract String getExtension();

    /**
     * Template Hook: Executes the format-specific serialization logic.
     *
     * @param individual The data to write.
     * @param domain     The domain context.
     * @param inventory  The inventory context.
     * @param path       The fully validated and prepared target path.
     * @throws IOException If the writing process fails.
     */
    protected abstract void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException;
}
