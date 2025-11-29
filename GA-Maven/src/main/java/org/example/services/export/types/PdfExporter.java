package org.example.services.export.types;


import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Cell;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;
import org.example.services.export.BaseExporter;


import java.io.IOException;
import java.nio.file.Path;
import java.util.Locale;

public class PdfExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ".pdf";
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        // 1. Inizializzazione PDF (Writer -> PdfDocument -> Document)
        // PdfWriter scrive fisicamente sul file
        PdfWriter writer = new PdfWriter(path.toString());

        // PdfDocument gestisce la struttura interna del PDF
        PdfDocument pdf = new PdfDocument(writer);

        // Document è l'interfaccia di alto livello per aggiungere paragrafi, tabelle, ecc.
        try (Document document = new Document(pdf)) {

            // --- TITOLO ---
            document.add(new Paragraph("Terrain Report")
                    .setFontSize(18)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER));

            document.add(new Paragraph("Generated with AGROPLANNER")
                    .setFontSize(10)
                    .setItalic()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20)); // Spazio dopo il titolo

            // --- SEZIONE METADATI ---
            document.add(new Paragraph("Details")
                    .setFontSize(14)
                    .setBold());

            document.add(new Paragraph("Domain: " + domain.toString()));
            document.add(new Paragraph(String.format(Locale.US, "Target Distance: %.2f", radius)));
            document.add(new Paragraph("Total Points: " + individual.getChromosomes().size()));

            // Fitness in grassetto e più grande
            document.add(new Paragraph(String.format(Locale.US, "Best Fitness: %.6f", individual.getFitness()))
                    .setBold()
                    .setFontSize(12)
                    .setMarginBottom(15));

            // --- TABELLA DATI ---
            document.add(new Paragraph("Chromosomes Coordinates")
                    .setFontSize(14)
                    .setBold()
                    .setMarginBottom(5));

            // Creiamo una tabella con 3 colonne (ID, X, Y) che occupa il 100% della larghezza
            float[] columnWidths = {1, 2, 2}; // Proporzioni colonne
            Table table = new Table(UnitValue.createPercentArray(columnWidths));
            table.setWidth(UnitValue.createPercentValue(100));

            // Header Tabella
            table.addHeaderCell(new Cell().add(new Paragraph("ID").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("X").setBold()));
            table.addHeaderCell(new Cell().add(new Paragraph("Y").setBold()));

            // Riempimento Dati
            int index = 0;
            for (Point p : individual.getChromosomes()) {
                table.addCell(String.valueOf(index++));
                table.addCell(String.format(Locale.US, "%.4f", p.getX()));
                table.addCell(String.format(Locale.US, "%.4f", p.getY()));
            }

            // Aggiungiamo la tabella al documento
            document.add(table);

            // Il try-with-resources chiuderà automaticamente 'document', salvando il file.
        }
    }
}
