package org.agroplanner.exportsystem.controllers;

import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.exportsystem.views.ExportViewContract;

import java.util.Optional;

public class ExportConsoleController {

    private final ExportViewContract view;
    private final ExportService service;

    public ExportConsoleController(ExportViewContract view, ExportService service) {
        this.view = view;
        this.service = service;
    }

    public void runExportWizard(Individual solution, Domain domain, double radius) {
        while (true) {
            // 1. Chiedi formato
            Optional<ExportType> selectedType = view.askForExportType(service.getAvailableTypes());
            if (selectedType.isEmpty()) return;

            // 2. Chiedi nome file
            String filename = view.askForFilename();

            // 3. Esegui
            try {
                String savedPath = service.performExport(solution, domain, radius, selectedType.get(), filename);
                view.showSuccessMessage(savedPath);
                return; // Successo, esci dal wizard

            } catch (InvalidInputException e) {
                // Errore dell'utente (nome file non valido lato service)
                view.showErrorMessage("Invalid Filename: " + e.getMessage());
                // Il ciclo while continua -> Riprova

            } catch (ExportException e) {
                // Errore del sistema (Disco pieno, permessi, ecc.)
                view.showErrorMessage("Export Error: " + e.getMessage());
                // Il ciclo while continua -> Riprova (magari cambiando percorso o nome)

            } catch (Exception e) {
                // Bug imprevisto
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
            }
        }
    }
}
