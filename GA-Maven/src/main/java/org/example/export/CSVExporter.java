package org.example.export;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.example.model.Individual;
import org.example.model.Point;

import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class CSVExporter extends BaseExporter{

    private static final String[] HEADERS = { "Indice", "X", "Y" };

    @Override
    protected String getExtension() {
        return ".csv";
    }

    @Override
    protected void performExport(Individual individual, Path path) throws IOException {

        // Configurazione formato CSV
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(HEADERS)
                .build();

        // Scrittura effettiva
        // Nota: usiamo path.toFile() perché FileWriter vuole un File, non un Path
        try (FileWriter writer = new FileWriter(path.toFile());
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            List<Point> points = individual.getChromosomes();
            int i = 0;
            for (Point p : points) {
                printer.printRecord(i++, p.getX(), p.getY());
            }
        }
        // Non serve il catch qui; L'eccezione viene lanciata al padre che la gestisce.
    }
}
