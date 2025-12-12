package org.agroplanner.exportsystem.model.types;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

/**
 * <p><strong>Concrete Exporter: Plain Text (.txt).</strong></p>
 *
 * <p>This strategy generates a simple, human-readable report.
 * Unlike CSV (optimized for data parsing) or Excel (optimized for spreadsheets),
 * this format is designed to be opened immediately in any basic text editor for quick inspection.</p>
 */
public class TxtExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ".txt";
    }

    /**
     * Writes the formatted text report.
     *
     * @param individual The solution data.
     * @param domain     The domain context.
     * @param radius     The point dimension.
     * @param path       The destination path.
     * @throws IOException If writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // Use BufferedWriter for efficient character writing.
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- REPORT HEADER ---
            writer.write("========================================");
            writer.newLine();
            writer.write("            TERRAIN REPORT");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            // --- CONFIGURATION SECTION ---
            writer.write("--- Configuration ---");
            writer.newLine();
            writer.write("Domain: " + domain.toString());
            writer.newLine();

            // Format: "Target Distance: 10.50" (Using US Locale for dot separator)
            writer.write(String.format(Locale.US, "Target Distance: %.2f", radius));
            writer.newLine();
            writer.newLine();

            // --- RESULTS SECTION ---
            writer.write("--- Results ---");
            writer.newLine();
            writer.write(String.format(Locale.US, "Fitness: %.6f", individual.getFitness()));
            writer.newLine();
            writer.write("Total Points: " + individual.getDimension());
            writer.newLine();
            writer.newLine();

            // --- DATA LIST SECTION ---
            writer.write("--- Chromosomes (Points) ---");
            writer.newLine();

            int index = 0;
            for (Point p : individual.getChromosomes()) {
                // Row Format: [0] X: 10.5000 | Y: 5.2300
                writer.write(String.format(Locale.US, "[%d] X: %.4f | Y: %.4f", index++, p.getX(), p.getY()));
                writer.newLine();
            }

            // Footer
            writer.write("----------------------------");
            writer.newLine();
            writer.write("End of Report.");
        }
    }
}
