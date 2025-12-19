package org.agroplanner.exportsystem.model;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.inventory.model.PlantInventory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * <p><strong>Abstract Strategy for Data Export.</strong></p>
 *
 * <p>This class acts as the skeleton for all export algorithms, implementing the <strong>Template Method Pattern</strong>.
 * It centralizes common infrastructure logic (directory management, file extension handling, path resolution)
 * while delegating the specific format writing to concrete subclasses.</p>
 */
public abstract class BaseExporter {

    /**
     * The default root directory for all generated files.
     */
    public static final String EXPORT_FOLDER = "exports";

    /**
     * Orchestrates the export process.
     * <p>
     * This method is {@code final} to prevent subclasses from altering the structural logic of the export flow.
     * It ensures that:
     * <ol>
     * <li>The filename has the correct extension.</li>
     * <li>The target directory exists (creating it if necessary).</li>
     * <li>The actual writing is delegated to the specific implementation.</li>
     * </ol>
     * </p>
     *
     * @param individual The solution data to export.
     * @param domain     The problem domain context.
     * @param filename   The user-specified filename (with or without extension).
     * @return The absolute path of the generated file as a String (useful for UI feedback).
     * @throws IOException If filesystem operations (creation, writing) fail.
     */
    public final String export(Individual individual, Domain domain, PlantInventory inventory, String filename) throws IOException {
        // 1. Risolviamo il path usando la logica centralizzata
        Path path = resolveFilePath(filename);

        // 2. Creazione cartelle (rimane uguale)
        if (Files.notExists(path.getParent())) {
            Files.createDirectories(path.getParent());
        }

        // 3. Scrittura
        performExport(individual, domain, inventory, path);
        return path.toAbsolutePath().toString();
    }

    /**
     * NUOVO METODO: Calcola il percorso finale senza scrivere il file.
     * Utile per i controlli di esistenza pre-export.
     */
    public Path resolveFilePath(String filename) {
        String extension = getExtension();
        // Normalizzazione estensione
        if (!filename.toLowerCase().endsWith(extension)) {
            filename += extension;
        }
        // Risoluzione path
        return Paths.get(EXPORT_FOLDER, filename);
    }

    // ------------------- ABSTRACT HOOKS -------------------

    /**
     * Defines the file extension associated with the specific format.
     * @return The extension string including the dot (e.g., ".csv", ".xlsx").
     */
    protected abstract String getExtension();

    /**
     * Executes the actual writing of data to the file.
     * <p>
     * Concrete implementations (CSV, Excel, etc.) must implement this method to translate
     * the domain objects into bytes/characters.
     * </p>
     *
     * @param individual The data source.
     * @param domain     The context.
     * @param path       The fully resolved target path (guaranteed to have a valid parent directory).
     * @throws IOException If the low-level writing operation fails.
     */
    protected abstract void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException;
}
