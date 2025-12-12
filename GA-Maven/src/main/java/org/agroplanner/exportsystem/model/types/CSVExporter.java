package org.agroplanner.exportsystem.model.types;

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
    private static final String[] HEADERS = {"ID", "X", "Y"};

    @Override
    protected String getExtension() {
        return ".csv";
    }

    /**
     * Writes the CSV file combining raw metadata writing and structured data writing.
     *
     * @param individual The solution to export.
     * @param domain     The domain context.
     * @param radius     The point radius.
     * @param path       The target file path.
     * @throws IOException If writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

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

            // Used Locale.US to ensure decimal points (10.5) are used instead of commas (10,5).
            // Using commas for decimals breaks the CSV format structure.
            writer.write(String.format(Locale.US, "# Target Distance : %.2f", radius));
            writer.newLine();

            writer.write("# Total Points: " + individual.getDimension());
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
                    // The library handles escaping, separators, and line breaks automatically.
                    printer.printRecord(index, point.getX(), point.getY());
                    index++;
                }
            }
        }
    }
}
