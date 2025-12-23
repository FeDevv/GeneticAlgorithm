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
 * Concrete implementation of the Export Strategy targeting <strong>Portable Document Format (PDF)</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Library:</strong> Utilizes <strong>iText 7</strong> for high-level document composition.
 * This abstracts away the low-level PDF syntax (dictionaries, streams), allowing for a declarative layout approach.</li>
 * <li><strong>Use Case:</strong> Generates an immutable, "Print-Ready" report suitable for stakeholders.
 * Unlike Excel/JSON (which are for analysis/integration), this format focuses on <strong>Visual Presentation</strong>.</li>
 * <li><strong>Visual Hierarchy:</strong> Employs font sizing, semantic weighting (Bold/Italic), and color coding
 * to guide the reader's eye to key metrics (e.g., Fitness Score).</li>
 * </ul>
 */
public class PdfExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.PDF.getExtension();
    }

    /**
     * Composes and writes the PDF document structure.
     *
     * <p><strong>Layout Strategy:</strong></p>
     * <ol>
     * <li><strong>Header:</strong> Branding and Title.</li>
     * <li><strong>Metadata Block:</strong> Input parameters (Domain, Inventory).</li>
     * <li><strong>KPI Block:</strong> Key Performance Indicators (Total Count, Fitness). Highlighted for visibility.</li>
     * <li><strong>Data Grid:</strong> A structured 5-column table listing the solution coordinates.</li>
     * </ol>
     *
     * @param individual The solution phenotype.
     * @param domain     The problem constraints.
     * @param inventory  The biological inputs.
     * @param path       The target file path.
     * @throws IOException If the file stream cannot be opened.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // 1. Infrastructure Setup (Low-Level)
        PdfWriter writer = new PdfWriter(path.toString());
        PdfDocument pdf = new PdfDocument(writer);

        // 2. Document Composition (High-Level)
        // The 'try-with-resources' ensures the document is closed and flushed properly.
        try (Document document = new Document(pdf)) {

            // --- SECTION A: HEADER ---
            document.add(new Paragraph("Optimization Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("AgroPlanner v2.0 - Multi-Culture Engine")
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));

            // --- SECTION B: CONTEXT & CONFIGURATION ---
            document.add(new Paragraph("Configuration Details")
                    .setFontSize(14)
                    .setBold());

            document.add(new Paragraph("Domain Geometry: " + domain.toString()).setFontSize(11));

            // Inventory Manifest
            document.add(new Paragraph("Requested Inventory:").setBold().setFontSize(11).setMarginTop(5));

            for (InventoryEntry item : inventory.getEntries()) {

                // Formatting: " - 🍅 TOMATO: 50 units (r=1.50m)"
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2fm)",
                        item.getType().getLabel(),
                        item.getType().name(),
                        item.getQuantity(),
                        item.getRadius());

                document.add(new Paragraph(line)
                        .setFontSize(10)
                        .setMarginLeft(10)  // Indentation for list effect
                );
            }

            // --- SECTION C: RESULTS (KPIs) ---
            document.add(new Paragraph(String.format(Locale.US, "Total Plants Placed: %d", individual.getDimension()))
                    .setMarginTop(5));

            // Visual Pop: Highlight Fitness in Blue
            document.add(new Paragraph(String.format(Locale.US, "Final Fitness Score: %.6f", individual.getFitness()))
                    .setBold()
                    .setFontSize(12)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE)
                    .setMarginBottom(15));

            // --- SECTION D: DATA TABLE ---
            document.add(new Paragraph("Solution Coordinates")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // Table Setup: Responsive Column Widths (Percentages)
            // Ratios: ID(1) : Type(3) : X(2) : Y(2) : Radius(2)
            float[] columnWidths = {1, 3, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Headers
            addHeaderCell(table, "ID");
            addHeaderCell(table, "Type");
            addHeaderCell(table, "X(m)");
            addHeaderCell(table, "Y(m)");
            addHeaderCell(table, "Radius(m)");

            // Table Population
            int index = 0;
            for (Point p : individual.getChromosomes()) {
                table.addCell(createCell(String.valueOf(index++)));

                // Composite String: "🍅 TOMATO"
                String typeStr = p.getType().getLabel() + " " + p.getType().name();
                table.addCell(createCell(typeStr));

                table.addCell(createCell(String.format(Locale.US, "%.4f", p.getX())));
                table.addCell(createCell(String.format(Locale.US, "%.4f", p.getY())));
                table.addCell(createCell(String.format(Locale.US, "%.2f", p.getRadius())));
            }

            // Finalize: Add table to layout flow
            document.add(table);
        }
    }

    // ------------------- PRIVATE HELPERS (Layout DRY) -------------------

    /**
     * Creates a styled header cell with bold text and center alignment.
     */
    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell().add(new Paragraph(text).setBold().setTextAlignment(TextAlignment.CENTER)));
    }

    /**
     * Creates a standard data cell with right alignment (optimal for numbers).
     */
    private Cell createCell(String text) {
        return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
    }
}
