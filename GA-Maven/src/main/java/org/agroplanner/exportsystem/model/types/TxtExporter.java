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
 * Concrete implementation of the Export Strategy targeting <strong>Plain Text (.txt)</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Goal:</strong> Human Readability. This format optimizes for immediate visual inspection via standard
 * text editors (Notepad, Vim) or CLI tools (cat, less). It sacrifices compactness for layout clarity.</li>
 * <li><strong>Narrative Structure:</strong> The report follows a logical flow:
 * <ol>
 * <li><strong>Header:</strong> Branding and Title.</li>
 * <li><strong>Context (Input):</strong> What did you ask for? (Domain + Inventory).</li>
 * <li><strong>Metrics (Output):</strong> How good is the result? (Fitness + Counts).</li>
 * <li><strong>Detail (Data):</strong> The actual coordinate list.</li>
 * </ol>
 * </li>
 * </ul>
 */
public class TxtExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.TXT.getExtension();
    }

    /**
     * Generates a formatted textual summary of the optimization session.
     *
     * @param individual The solution phenotype.
     * @param domain     The problem constraints.
     * @param inventory  The biological inputs.
     * @param path       The output destination.
     * @throws IOException If file writing fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // Use buffered writing for performance
        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- SECTION 1: VISUAL HEADER ---
            writer.write("========================================");
            writer.newLine();
            writer.write("            AGROPLANNER REPORT");
            writer.newLine();
            writer.write("         Multi-Culture Solution");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            // --- SECTION 2: CONTEXT (Configuration) ---
            writer.write("--- Configuration ---");
            writer.newLine();
            writer.write("Domain Geometry: " + domain.toString());
            writer.newLine();

            writer.write("Requested Inventory:");
            writer.newLine();

            // Print the manifest (The "Recipe")
            for (InventoryEntry entry : inventory.getEntries()) {
                // e.g. " - 🍅 TOMATO: 50 units (r=1.50m)"
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2fm)",
                        entry.getType().getLabel(),  // Visual Icon
                        entry.getType().name(),      // Code Name
                        entry.getQuantity(),         // Count
                        entry.getRadius());          // Size

                writer.write(line);
                writer.newLine();
            }
            writer.newLine();

            // --- SECTION 3: METRICS (Summary) ---
            writer.write("--- Results Summary ---");
            writer.newLine();
            writer.write("Total Plants Placed: " + inventory.getTotalPopulationSize());
            writer.newLine();
            // High precision for fitness analysis
            writer.write(String.format(Locale.US, "Final Optimization Score (Fitness): %.6f", individual.getFitness()));
            writer.newLine();
            writer.newLine();

            // --- SECTION 4: DATA PAYLOAD ---
            writer.write("--- Plants (Coordinates) ---");
            writer.newLine();
            writer.write("Format: [ID] TYPE | X(m) | Y(m) | Radius(m)");
            writer.newLine();

            int index = 0;
            for (Point p : individual.getChromosomes()) {
                // Row Format: [0] 🍅 TOMATO | X: 10.5000 | Y: 5.2300 | R: 1.50
                String line = String.format(Locale.US, "[%03d] %s %-10s | X: %8.4f | Y: %8.4f | R: %.2f",
                        index++,
                        p.getType().getLabel(),
                        p.getType().name(),
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
