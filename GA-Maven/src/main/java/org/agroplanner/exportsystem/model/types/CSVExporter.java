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
 * <p><strong>Concrete Exporter: CSV (Comma Separated Values).</strong></p>
 *
 * <p>This strategy exports the solution in a plain text tabular format.
 * It leverages the <strong>Apache Commons CSV</strong> library for robust record handling.</p>
 *
 * <p><strong>File Structure:</strong>
 * <ul>
 * <li><strong>Header Section:</strong> Lines starting with '#' containing metadata (Domain, Fitness, etc.).</li>
 * <li><strong>Data Section:</strong> Standard CSV columns (ID, X, Y).</li>
 * </ul>
 * </p>
 */
public class CSVExporter extends BaseExporter {

    /** Standard column headers for the data section. */
    private static final String[] HEADERS = {"ID", "TYPE", "LABEL", "X", "Y", "RADIUS"};

    @Override
    protected String getExtension() {
        return ExportType.CSV.getExtension();
    }

    /**
     * Writes the CSV file combining raw metadata writing and structured data writing.
     *
     * @param individual The solution to export.
     * @param domain     The domain context.
     * @param path       The target file path.
     * @throws IOException If writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // Configure the CSV Format (Standard comma separation, defined headers).
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        // Open a buffered writer. We use this directly for metadata, then wrap it in the CSVPrinter.
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- METADATA HEADER (Manual Writing) ---
            // Implementation Choice:
            // We write metadata as comments ('#'). This allows humans to read context about the solution
            // (e.g., "What fitness did this have?") while allowing most CSV parsers to skip these lines
            // and read the data table correctly.

            writer.write("# Domain Info: " + domain.toString());
            writer.newLine();

            // Write the "Shopping List" (What was requested)
            writer.write("# Inventory Configuration:");
            writer.newLine();

            for (InventoryEntry entry : inventory.getEntries()) {
                try {
                    // Esempio output: # - TOMATO (🍅): 50 units, r=1.50
                    writer.write(String.format(Locale.US, "# - %s (%s): %d units, r=%.2f",
                            entry.getType().name(),      // Nome (ex key)
                            entry.getType().getLabel(),  // Emoji (ex key)
                            entry.getQuantity(),         // Quantità
                            entry.getRadius()));         // Raggio specificato per questa riga

                    writer.newLine();

                } catch (IOException e) {
                    throw new RuntimeException("Error writing header", e);
                }
            }

            writer.write("# Total Plants: " + individual.getDimension());
            writer.newLine();

            writer.write(String.format(Locale.US, "# Fitness: %.6f", individual.getFitness()));
            writer.newLine();

            // Visual separator before data
            writer.newLine();
            // ----------------------------------

            // --- DATA TABLE (Library Writing) ---
            // We attach the CSVPrinter to the *same* underlying writer to continue the file.
            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                List<Point> points = individual.getChromosomes();
                int index = 0;

                for (Point point : points) {
                    printer.printRecord(
                            index,
                            point.getType().name(),    // Per parsing (TOMATO)
                            point.getType().getLabel(), // Per umani (🍅)
                            point.getX(),
                            point.getY(),
                            point.getRadius()
                    );
                    index++;
                }
            }
        }
    }
}
