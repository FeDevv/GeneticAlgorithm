package org.agroplanner.exportsystem.model.types;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.InventoryEntry;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.BaseExporter;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

/**
 * <p><strong>Concrete Exporter: PDF (Portable Document Format).</strong></p>
 *
 * <p>This strategy generates a read-only, printable report of the solution.
 * It utilizes the <strong>iText</strong> library to construct a high-quality document containing
 * formatted text and tabular data.</p>
 *
 * <p><strong>Use Case:</strong> Ideal for final presentation, archiving, or sharing results with non-technical stakeholders.</p>
 */
public class PdfExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.PDF.getExtension();
    }

    /**
     * Generates the PDF document.
     *
     * <p><strong>Architecture (iText):</strong>
     * <ol>
     * <li>{@code PdfWriter}: Low-level component that writes bytes to the file stream.</li>
     * <li>{@code PdfDocument}: Manages the PDF internal structure (pages, objects).</li>
     * <li>{@code Document}: High-level layout engine that handles paragraphs, tables, and flow.</li>
     * </ol>
     * </p>
     *
     * @param individual The solution data.
     * @param domain     The domain context.
     * @param path       The destination path.
     * @throws IOException If file creation fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // PDF Infrastructure
        PdfWriter writer = new PdfWriter(path.toString());
        PdfDocument pdf = new PdfDocument(writer);

        try (Document document = new Document(pdf)) {

            // --- REPORT HEADER ---
            document.add(new Paragraph("Optimization Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("AgroPlanner v2.0 - Multi-Culture Engine")
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // --- CONTEXT & METADATA ---
            document.add(new Paragraph("Configuration Details")
                    .setFontSize(14)
                    .setBold());

            document.add(new Paragraph("Domain: " + domain.toString()).setFontSize(11));

            // Inventory Manifest Section
            document.add(new Paragraph("Requested Inventory:").setBold().setFontSize(11).setMarginTop(5));

            for (InventoryEntry item : inventory.getEntries()) {

                // E.g.: " - 🍅 TOMATO: 50 units (r=1.50)"
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2f)",
                        item.getType().getLabel(), // Emoji (recuperato da getType)
                        item.getType().name(),     // Nome (recuperato da getType)
                        item.getQuantity(),        // Quantità diretta dell'entry
                        item.getRadius());         // Raggio specifico dell'entry

                document.add(new Paragraph(line).setFontSize(10).setMarginLeft(10));
            }

            document.add(new Paragraph(String.format(Locale.US, "Total Plants Placed: %d", individual.getDimension()))
                    .setMarginTop(5));

            // Fitness Score Highlight
            document.add(new Paragraph(String.format(Locale.US, "Final Fitness Score: %.6f", individual.getFitness()))
                    .setBold()
                    .setFontSize(12)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE) // Visual pop
                    .setMarginBottom(15));

            // --- DATA TABLE SECTION ---
            document.add(new Paragraph("Solution Coordinates")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // Table Configuration: 5 Columns
            // ID (1), Type (3), X (2), Y (2), Radius (2) -> Width Ratios
            float[] columnWidths = {1, 3, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Headers
            addHeaderCell(table, "ID");
            addHeaderCell(table, "Type");
            addHeaderCell(table, "X");
            addHeaderCell(table, "Y");
            addHeaderCell(table, "Radius");

            // Data Population
            int index = 0;
            for (Point p : individual.getChromosomes()) {
                table.addCell(createCell(String.valueOf(index++)));

                // Concatenate Emoji + Name: "🍅 TOMATO"
                String typeStr = p.getType().getLabel() + " " + p.getType().name();
                table.addCell(createCell(typeStr));

                table.addCell(createCell(String.format(Locale.US, "%.4f", p.getX())));
                table.addCell(createCell(String.format(Locale.US, "%.4f", p.getY())));
                table.addCell(createCell(String.format(Locale.US, "%.2f", p.getRadius())));
            }

            document.add(table);
        }
    }

    // Helper to keep code clean
    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setBold().setTextAlignment(TextAlignment.CENTER)));
    }

    // Helper for centering data cells
    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
    }
}
