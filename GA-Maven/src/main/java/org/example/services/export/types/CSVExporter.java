package org.example.services.export.types;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;
import org.example.services.export.BaseExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;

public class CSVExporter extends BaseExporter {

    private static final String[] HEADERS = {"ID", "X", "Y"};

    @Override
    protected String getExtension() {
        return ".csv";
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // --- SCRITTURA METADATI DOMINIO ---
            // Scriviamo direttamente nel buffer prima di attivare il CSVPrinter
            writer.write("# Domain Info: " + domain.toString());
            writer.newLine();

            writer.write(String.format(Locale.US, "# Target Distance : %.2f", radius));
            writer.newLine();

            writer.write("# Total Points: " + individual.getDimension());
            writer.newLine();

            writer.write(String.format(Locale.US, "# Fitness: %.6f", individual.getFitness()));
            writer.newLine();
            // Aggiungi una riga vuota per pulizia
            writer.newLine();
            // ----------------------------------

            // Ora attacchiamo il CSVPrinter allo stesso writer
            try (CSVPrinter printer = new CSVPrinter(writer, format)) {
                List<Point> points = individual.getChromosomes();
                int index = 0;

                for (Point point : points) {
                    printer.printRecord(index, point.getX(), point.getY());
                    index++;
                }
            }
        }
    }
}
