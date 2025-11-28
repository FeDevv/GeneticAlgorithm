package org.example.controllers;

import org.example.model.domains.Domain;
import org.example.services.export.BaseExporter;
import org.example.services.export.ExportType;
import org.example.services.export.ExporterFactory;
import org.example.model.Individual;
import org.example.views.ExportConsoleView;

import java.io.IOException;
import java.util.Arrays;
import java.util.Optional;

public class ExportController {

    private final ExportConsoleView view;
    private final ExporterFactory factory;

    public ExportController(ExportConsoleView view) {
        this.view = view;
        this.factory = new ExporterFactory();
    }

    /**
     * Gestisce il flusso di esportazione con un ciclo di retry.
     * Continua a mostrare il menu finché l'utente non esce (0) o l'export ha successo.
     */
    public void handleExport(Individual bestSolution, Domain domain, double radius) {

        while (true) {
            // 1. Mostra menu
            view.showMenu(Arrays.asList(ExportType.values()));

            // 2. Leggi scelta
            int choice = view.readMenuChoice();

            // CASO USCITA: L'utente sceglie 0
            if (choice == 0) {
                // Possiamo stampare un messaggio di chiusura o semplicemente uscire
                return;
            }

            // 3. Validazione ID Menu
            Optional<ExportType> selectedType = ExportType.fromMenuId(choice);

            if (selectedType.isEmpty()) {
                // ID non valido: Mostra errore e RICOMINCIA il ciclo
                view.showError("Invalid selection ID. Please try again.");
                continue;
            }

            // ID Valido -> Procediamo
            ExportType type = selectedType.get();
            String filename = view.readFilename();

            try {
                // 4. Creazione ed Esecuzione
                BaseExporter exporter = factory.createExporter(type);

                // Nota: export ritorna il path se ha successo, altrimenti lancia eccezione
                String savedPath = exporter.export(bestSolution, domain, radius, filename);

                // 5. Successo -> Mostra messaggio e ESCE dal metodo (return)
                view.showSuccess(savedPath);
                return;

            } catch (IOException e) {
                // Errore di scrittura (es. disco pieno, permessi):
                // Mostra errore e il ciclo 'while' permetterà di riprovare (magari cambiando nome file)
                view.showError("File saving failed: " + e.getMessage());

            } catch (IllegalArgumentException e) {
                // Errore interno o di configurazione
                view.showError("Configuration error: " + e.getMessage());

            } catch (Exception e) {
                // Errore imprevisto
                view.showError("Unexpected error: " + e.getMessage());
            }
            // Qui il ciclo ricomincia (loop back) permettendo all'utente di correggere l'errore
        }
    }
}
