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
 * Updated to include full biological metadata (Variety Name).
 */
public class PdfExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ExportType.PDF.getExtension();
    }

    /**
     * Composes and writes the PDF document structure.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, PlantInventory inventory, Path path) throws IOException {

        // 1. Infrastructure Setup (Low-Level)
        PdfWriter writer = new PdfWriter(path.toString());
        PdfDocument pdf = new PdfDocument(writer);

        // 2. Document Composition (High-Level)
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
                String line = String.format(Locale.US, " - %s %s: %d units (r=%.2fm)",
                        item.getType().getLabel(),
                        item.getType().name(),
                        item.getQuantity(),
                        item.getRadius());

                document.add(new Paragraph(line)
                        .setFontSize(10)
                        .setMarginLeft(10)
                );
            }

            // --- SECTION C: RESULTS (KPIs) ---
            document.add(new Paragraph(String.format(Locale.US, "Total Plants Placed: %d", individual.getDimension()))
                    .setMarginTop(5));

            document.add(new Paragraph(String.format(Locale.US, "Final Fitness Score: %.6f", individual.getFitness()))
                    .setBold()
                    .setFontSize(12)
                    .setFontColor(com.itextpdf.kernel.colors.ColorConstants.BLUE)
                    .setMarginBottom(15));

            // --- SECTION D: DATA TABLE (AGGIORNATA) ---
            document.add(new Paragraph("Solution Coordinates")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // Table Setup: 6 COLUMNS
            // Ratios: ID(1) : Variety(4) : Type(2) : X(2) : Y(2) : Radius(2)
            // Totale parti: 13. Variety ha molto spazio per il testo lungo.
            float[] columnWidths = {1, 4, 2, 2, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Headers
            addHeaderCell(table, "ID");
            addHeaderCell(table, "Variety Name"); // NEW COLUMN
            addHeaderCell(table, "Type");
            addHeaderCell(table, "X(m)");
            addHeaderCell(table, "Y(m)");
            addHeaderCell(table, "Radius(m)");

            // Table Population
            int index = 0;
            for (Point p : individual.getChromosomes()) {
                // 1. ID
                table.addCell(createCellRight(String.valueOf(index++)));

                // 2. Variety (Allineato a sinistra perché è testo)
                String variety = (p.getVarietyName() != null) ? p.getVarietyName() : "Unknown";
                table.addCell(createCellLeft(variety));

                // 3. Type (Icon + Enum)
                String typeStr = p.getType().getLabel() + " " + p.getType().name();
                table.addCell(createCellCenter(typeStr));

                // 4. Coords (Numeri a destra)
                table.addCell(createCellRight(String.format(Locale.US, "%.4f", p.getX())));
                table.addCell(createCellRight(String.format(Locale.US, "%.4f", p.getY())));
                table.addCell(createCellRight(String.format(Locale.US, "%.2f", p.getRadius())));
            }

            // Finalize
            document.add(table);
        }
    }

    // ------------------- PRIVATE HELPERS (Layout DRY) -------------------

    private void addHeaderCell(Table table, String text) {
        table.addHeaderCell(new Cell()
                .add(new Paragraph(text).setBold().setTextAlignment(TextAlignment.CENTER))
                .setBackgroundColor(com.itextpdf.kernel.colors.ColorConstants.LIGHT_GRAY)); // Header grigio
    }

    private Cell createCellRight(String text) {
        return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.RIGHT).setFontSize(9));
    }

    private Cell createCellLeft(String text) {
        return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.LEFT).setFontSize(9));
    }

    private Cell createCellCenter(String text) {
        return new Cell().add(new Paragraph(text).setTextAlignment(TextAlignment.CENTER).setFontSize(9));
    }
}
