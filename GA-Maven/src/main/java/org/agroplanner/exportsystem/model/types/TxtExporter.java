package org.agroplanner.exportsystem.model.types;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;

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
/**
 * Concrete implementation of the Export Strategy targeting <strong>Plain Text (.txt)</strong>.
 */
public class TxtExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.TXT.getExtension();
    }

    /**
     * Generates a formatted textual summary of the optimization session.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

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
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2fm)",
                        entry.getType().getLabel(),
                        entry.getType().name(),
                        entry.getQuantity(),
                        entry.getRadius());

                writer.write(line);
                writer.newLine();
            }
            writer.newLine();

            // --- SECTION 3: METRICS (Summary) ---
            writer.write("--- Results Summary ---");
            writer.newLine();
            writer.write("Total Plants Placed: " + inventory.getTotalPopulationSize());
            writer.newLine();
            writer.write(String.format(Locale.US, "Final Optimization Score (Fitness): %.6f", individual.getFitness()));
            writer.newLine();
            writer.newLine();

            // --- SECTION 4: DATA PAYLOAD ---
            writer.write("--- Plants (Coordinates) ---");
            writer.newLine();
            writer.write("Format: [ID] VARIETY NAME (TYPE) | X(m) | Y(m) | Radius(m)");
            writer.newLine();
            writer.write("---------------------------------------------------------------");
            writer.newLine();

            int index = 0;
            for (Point p : individual.getChromosomes()) {

                String variety = (p.getVarietyName() != null) ? p.getVarietyName() : "Unknown";
                String type = p.getType().name();
                String icon = p.getType().getLabel();

                String line = String.format(Locale.US, "[%03d] %s %-20s (%-9s) | X: %8.4f | Y: %8.4f | R: %.2f",
                        index++,
                        icon,
                        truncate(variety, 20),
                        type,
                        p.getX(),
                        p.getY(),
                        p.getRadius());

                writer.write(line);
                writer.newLine();
            }

            // Footer
            writer.write("---------------------------------------------------------------");
            writer.newLine();
            writer.write("End of Report.");
        }
    }

    private String truncate(String s, int len) {
        if (s == null) return "";
        if (s.length() > len) {
            return s.substring(0, len - 2) + "..";
        }
        return s;
    }
}
