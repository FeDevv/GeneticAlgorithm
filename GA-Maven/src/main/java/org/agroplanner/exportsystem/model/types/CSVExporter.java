package org.agroplanner.exportsystem.model.types;

import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

/**
 * Concrete implementation of the Export Strategy targeting <strong>Comma-Separated Values (.csv)</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Goal:</strong> Hybrid Readability. Designed to be primarily imported into spreadsheets (Excel),
 * providing both the raw data and the context metadata in a single file.</li>
 * <li><strong>Structure:</strong>
 * <ol>
 * <li><strong>Metadata Block:</strong> Rows starting with specific keys (Domain, Fitness, etc.) to give context.</li>
 * <li><strong>Separator:</strong> An empty row to visually detach headers from data.</li>
 * <li><strong>Data Table:</strong> The actual solution dataset.</li>
 * </ol>
 * </li>
 * <li><strong>Localization Strategy:</strong> Uses <strong>Semicolon (;)</strong> as delimiter and dots (.) for decimals
 * to ensure perfect compatibility with European Excel versions.</li>
 * </ul>
 */
public class CSVExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.CSV.getExtension();
    }

    /**
     * Serializes the solution into a structured CSV with a metadata header.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- SECTION 1: METADATA BLOCK ---
            // Scriviamo Etichetta;Valore così Excel li mette in Colonna A e Colonna B

            writer.write("METADATA_KEY;VALUE");
            writer.newLine();

            // 1. Domain
            writer.write("Domain Geometry;" + domain.toString());
            writer.newLine();

            // 2. Fitness (Formatto US per avere il punto)
            writer.write(String.format(Locale.US, "Final Fitness Score;%.6f", individual.getFitness()));
            writer.newLine();

            // 3. Count
            writer.write("Total Plants Placed;" + individual.getDimension());
            writer.newLine();

            // 4. Timestamp (Optional but useful)
            writer.write("Export Timestamp;" + java.time.LocalDateTime.now());
            writer.newLine();

            // --- SEPARATOR ---
            // Lasciamo una riga vuota per staccare visivamente i dati
            writer.newLine();

            // --- SECTION 2: DATA TABLE HEADER ---
            writer.write("ID;VARIETY_ID;VARIETY_NAME;PLANT_TYPE;COORD_X;COORD_Y;RADIUS");
            writer.newLine();

            // --- SECTION 3: DATA ROWS ---
            int id = 0;
            for (Point p : individual.getChromosomes()) {

                // Data Preparation
                String varietyName = (p.getVarietyName() != null) ? p.getVarietyName() : "Unknown";

                // Sanitization: Rimuoviamo il separatore ';' dal nome se presente
                varietyName = varietyName.replace(";", " ");

                // Formatting
                String line = String.format(Locale.US, "%d;%d;%s;%s;%.4f;%.4f;%.4f",
                        id++,
                        p.getVarietyId(),
                        varietyName,
                        p.getType().name(),
                        p.getX(),
                        p.getY(),
                        p.getRadius()
                );

                writer.write(line);
                writer.newLine();
            }
        }
    }
}
