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
 * Concrete implementation of the Export Strategy targeting <strong>Comma Separated Values (CSV)</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Hybrid Serialization:</strong> Combines raw text writing for metadata with a robust CSV engine
 * (Apache Commons CSV) for tabular data. This ensures both flexibility and strict format compliance (escaping, quoting).</li>
 * <li><strong>Interoperability:</strong> The output format is designed to be "Polyglot Friendly".
 * Metadata lines start with {@code #}, a standard comment marker recognized by data analysis tools
 * (like Python's {@code pandas}, R, or Gnuplot), allowing them to parse the data table while ignoring the context.</li>
 * <li><strong>Data Lineage:</strong> Includes the original {@link PlantInventory} and Domain details in the header,
 * making the file self-documenting (traceable).</li>
 * </ul>
 */
public class CSVExporter extends BaseExporter {

    /**
     * The schema definition for the data section.
     */
    private static final String[] HEADERS = {"ID", "TYPE", "LABEL", "X", "Y", "RADIUS"};

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getExtension() {
        return ExportType.CSV.getExtension();
    }

    /**
     * Executes the dual-phase writing process (Metadata Header + Data Body).
     *
     * @param individual The solution genotype to serialize.
     * @param domain     The constraint context.
     * @param inventory  The biological context (what was requested).
     * @param path       The target file location.
     * @throws IOException If file access fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // Configuration: Standard CSV format with specific headers.
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        // Resource Management:
        // We open a BufferedWriter first to handle the unstructured metadata section.
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- PHASE 1: METADATA HEADER (Unstructured) ---
            // Writing context information as comments (#).

            writer.write("# --------------------------------------------------");
            writer.newLine();
            writer.write("# AGROPLANNER EXPORT - RAW DATA");
            writer.newLine();
            writer.write("# Domain: " + domain.toString());
            writer.newLine();

            // Traceability: Log the requested inventory vs the result
            writer.write("# Inventory Configuration:");
            writer.newLine();
            for (InventoryEntry entry : inventory.getEntries()) {
                // # - TOMATO (🍅): 50 units, r=1.50
                writer.write(String.format(Locale.US, "# - %s (%s): %d units, r=%.2fm",
                        entry.getType().name(),      // Nome
                        entry.getType().getLabel(),  // Emoji
                        entry.getQuantity(),         // Quantità
                        entry.getRadius()));         // Raggio specificato per questa riga

                writer.newLine();
            }

            // Solution Metrics
            writer.write("# Total Plants Placed: " + individual.getDimension());
            writer.newLine();
            writer.write(String.format(Locale.US, "# Final Fitness: %.6f", individual.getFitness()));
            writer.newLine();
            writer.write("# --------------------------------------------------");
            writer.newLine(); // Blank line separation

            // --- PHASE 2: DATA TABLE (Structured) ---
            // We wrap the existing writer in a CSVPrinter.
            // Note: Closing the printer will close the writer, which is handled by the try-with-resources.
            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                List<Point> points = individual.getChromosomes();
                int index = 0;

                for (Point point : points) {
                    printer.printRecord(
                            index,
                            point.getType().name(),     // Machine-readable ID (e.g., TOMATO)
                            point.getType().getLabel(), // Human-readable Icon (e.g., 🍅)
                            point.getX(),               // Coordinate X
                            point.getY(),               // Coordinate Y
                            point.getRadius()           // Radius used
                    );
                    index++;
                }
            }
        }
    }
}
