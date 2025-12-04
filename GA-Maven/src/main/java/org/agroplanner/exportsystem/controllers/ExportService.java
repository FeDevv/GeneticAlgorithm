package org.agroplanner.exportsystem.controllers;

import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.exportsystem.model.ExporterFactory;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

public class ExportService {

    private final ExporterFactory factory;

    public ExportService() {
        this.factory = ExporterFactory.getInstance();
    }

    public List<ExportType> getAvailableTypes() {
        return Arrays.asList(ExportType.values());
    }

    /**
     * Esegue l'export gestendo deep validation e wrapping delle eccezioni I/O.
     */
    public String performExport(Individual solution, Domain domain, double radius, ExportType type, String filename) {

        // --- 1. DEEP PROTECTION: Validazione Input ---
        // Anche se la View ha controllato, noi ricontrolliamo qui.
        if (filename == null || filename.isBlank()) {
            throw new InvalidInputException("Filename cannot be empty or null.");
        }

        // Controllo caratteri illegali (opzionale, ma buona pratica deep)
        if (filename.matches(".*[<>:\"/\\\\|?*].*")) {
            throw new InvalidInputException("Filename contains illegal characters (< > : \" / \\ | ? *).");
        }

        // --- 2. ESECUZIONE E GESTIONE ERRORI I/O ---
        try {
            BaseExporter exporter = factory.createExporter(type);

            // Se export() lancia IOException, noi la catturiamo qui
            return exporter.export(solution, domain, radius, filename);

        } catch (IOException e) {
            // --- 3. EXCEPTION TRANSLATION ---
            // Trasformiamo l'errore tecnico (IOException) in errore di dominio (ExportException)
            throw new ExportException("Failed to save file to disk. Check permissions or disk space.", e);

        } catch (Exception e) {
            // Catch-all per errori imprevisti dentro i singoli exporter
            throw new ExportException("Unexpected error during export process: " + e.getMessage(), e);
        }
    }
}
