package org.example.services.export.types;

import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;
import org.example.services.export.BaseExporter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;

public class TxtExporter extends BaseExporter {

    @Override
    protected String getExtension() {
        return ".txt";
    }

    @Override
    protected void performExport(Individual individual, Domain domain, double radius, Path path) throws IOException {

        try (BufferedWriter writer = Files.newBufferedWriter(path)) {

            // Intestazione artistica o formale
            writer.write("========================================");
            writer.newLine();
            writer.write("      GENETIC ALGORITHM REPORT");
            writer.newLine();
            writer.write("========================================");
            writer.newLine();
            writer.newLine();

            // Sezione Configurazione
            writer.write("--- Configuration ---");
            writer.newLine();
            writer.write("Domain: " + domain.toString());
            writer.newLine();
            // Usiamo String.format con Locale.US per il punto decimale
            writer.write(String.format(Locale.US, "Target Distance: %.2f", radius));
            writer.newLine();
            writer.newLine();

            // Sezione Risultati
            writer.write("--- Results ---");
            writer.newLine();
            writer.write(String.format(Locale.US, "Fitness: %.6f", individual.getFitness()));
            writer.newLine();
            writer.write("Total Points: " + individual.getDimension());
            writer.newLine();
            writer.newLine();

            // Lista Punti
            writer.write("--- Chromosomes (Points) ---");
            writer.newLine();

            int index = 0;
            for (Point p : individual.getChromosomes()) {
                // Formattiamo ogni riga come:  [0] X: 10.50 | Y: 5.23
                writer.write(String.format(Locale.US, "[%d] X: %.4f | Y: %.4f", index++, p.getX(), p.getY()));
                writer.newLine();
            }

            writer.write("----------------------------");
            writer.newLine();
            writer.write("End of Report.");
        }
    }
}
