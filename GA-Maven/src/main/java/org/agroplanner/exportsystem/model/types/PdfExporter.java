package org.agroplanner.exportsystem.model.types;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.agroplanner.gasystem.model.Individual;
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
        return ".pdf";
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
     * @param radius     The point dimension.
     * @param path       The destination path.
     * @throws IOException If file creation fails.
     */
    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // PDF Infrastructure Initialization
        // PdfWriter opens the file stream at the specified path.
        PdfWriter writer = new PdfWriter(path.toString());

        // PdfDocument initializes the PDF standard structure.
        PdfDocument pdf = new PdfDocument(writer);

        // Document is the high-level canvas.
        // The try-with-resources ensures 'document.close()' is called, which finalizes the PDF and flushes the file.
        try (Document document = new Document(pdf)) {

            // --- REPORT HEADER ---
            document.add(new Paragraph("Terrain Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Generated with AGROPLANNER")
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20)); // Visual spacer

            // --- METADATA SECTION ---
            document.add(new Paragraph("Details")
                    .setFontSize(14)
                    .setBold());

            document.add(new Paragraph("Domain: " + domain.toString()));
            // Locale.US ensures "10.50" instead of "10,50" for consistency.
            document.add(new Paragraph(String.format(Locale.US, "Target Distance: %.2f", radius)));
            document.add(new Paragraph("Total Points: " + individual.getChromosomes().size()));

            // Highlight the Fitness Score
            document.add(new Paragraph(String.format(Locale.US, "Best Fitness: %.6f", individual.getFitness()))
                    .setBold()
                    .setFontSize(12)
                    .setMarginBottom(15));

            // --- DATA TABLE SECTION ---
            document.add(new Paragraph("Chromosomes Coordinates")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // Table Configuration:
            // 3 Columns with relative widths 1:2:2 (ID is narrower than coords).
            // UnitValue.createPercentArray automatically calculates the internal ratios.
            float[] columnWidths = {1, 2, 2};
            Table table = new Table(UnitValue.createPercentArray(columnWidths));

            // Force the table to span the full page width (margins excluded).
            table.setWidth(UnitValue.createPercentValue(100));

            // Table Header
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("X").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Y").setBold()));

            // Data Population
            int index = 0;
            for (Point p : individual.getChromosomes()) {
                table.addCell(String.valueOf(index++));
                table.addCell(String.format(Locale.US, "%.4f", p.getX()));
                table.addCell(String.format(Locale.US, "%.4f", p.getY()));
            }

            // Render the table into the document flow
            document.add(table);
        }
    }
}
