package org.agroplanner.exportsystem.model.types;

import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
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
        return ExportType.TXT.getExtension();
    }

    /**
     * Writes the formatted text report.
     *
     * @param individual The solution data.
     * @param domain     The domain context.
     * @param path       The destination path.
     * @throws IOException If writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- REPORT HEADER ---
            writer.write("========================================");
            writer.newLine();
            writer.write("            AGROPLANNER REPORT");
            writer.newLine();
            writer.write("         Multi-Culture Solution");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            // --- CONFIGURATION SECTION ---
            writer.write("--- Configuration ---");
            writer.newLine();
            writer.write("Domain: " + domain.toString());
            writer.newLine();

            writer.write("Requested Inventory:");
            writer.newLine();

            // Print the manifest
            // Iteriamo sulla lista degli inserimenti
            for (InventoryEntry entry : inventory.getEntries()) {
                // e.g. " - 🍅 TOMATO: 50 units (r=1.50)"
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2fm)",
                        entry.getType().getLabel(),  // Emoji
                        entry.getType().name(),      // Nome
                        entry.getQuantity(),         // Quantità
                        entry.getRadius());          // Raggio

                writer.write(line);
                writer.newLine();
            }

            writer.newLine();

            // --- RESULTS SECTION ---
            writer.write("--- Results ---");
            writer.newLine();
            writer.write("Total Plants: " + inventory.getTotalPopulationSize());
            writer.newLine();
            writer.write(String.format(Locale.US, "Final Fitness: %.6f", individual.getFitness()));
            writer.newLine();
            writer.newLine();

            // --- DATA LIST SECTION ---
            writer.write("--- Chromosomes (Coordinates) ---");
            writer.newLine();
            writer.write("Format: [ID] TYPE | X(m) | Y(m) | Radius(m)");
            writer.newLine();

            int index = 0;
            for (Point p : individual.getChromosomes()) {
                // Row Format: [0] 🍅 TOMATO | X: 10.5000 | Y: 5.2300 | R: 1.50
                String line = String.format(Locale.US, "[%d] %s %s | X: %.4f | Y: %.4f | R: %.2f",
                        index++,
                        p.getType().getLabel(), // Emoji
                        p.getType().name(),     // Name
                        p.getX(),
                        p.getY(),
                        p.getRadius());

                writer.write(line);
                writer.newLine();
            }

            // Footer
            writer.write("----------------------------");
            writer.newLine();
            writer.write("End of Report.");
        }
    }
}
