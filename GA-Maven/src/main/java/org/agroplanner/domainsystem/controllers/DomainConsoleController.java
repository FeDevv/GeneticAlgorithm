package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.domainsystem.views.DomainViewContract;

import java.util.Map;
import java.util.Optional;

public class DomainConsoleController {

    private final DomainViewContract view;
    private final DomainService service;

    public DomainConsoleController(DomainViewContract view, DomainService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Esegue il wizard di creazione dominio su Console.
     * Contiene il ciclo 'while' perché l'interazione console è sincrona.
     */
    public Optional<Domain> runDomainCreationWizard() { //🧙‍♂️
        while (true) {
            // 1. Chiedi il tipo (la view gestisce il menu)
            Optional<DomainType> selectedType = view.askForDomainType(service.getAvailableDomainTypes());

            // User ha scelto 0 (Exit)
            if (selectedType.isEmpty()) {
                return Optional.empty();
            }

            // 2. Chiedi i parametri
            DomainType type = selectedType.get();
            Map<String, Double> params = view.askForParameters(type);

            // 3. Tenta la creazione tramite il Service
            try {
                Domain domain = service.createDomain(type, params);

                view.showSuccessMessage();
                return Optional.of(domain);

            } catch (DomainConstraintException e) {
                // CASO A: Errore di Logica del Dominio (es. "Inner radius must be < Outer")
                // Questo è un errore "previsto" dovuto a input utente errato.
                // Mostriamo direttamente il messaggio dell'eccezione perché lo abbiamo scritto noi per essere chiaro.
                view.showErrorMessage(e.getMessage());

            } catch (InvalidInputException e) {
                // CASO B: Altri errori di input (es. parametri mancanti, valori nulli)
                view.showErrorMessage("Input Error: " + e.getMessage());

            } catch (Exception e) {
                // CASO C: Crash imprevisto (NullPointerException, ecc.)
                // Questo non dovrebbe succedere. Se succede, è un bug.
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
                // Opzionale: e.printStackTrace(); // Scommenta per debuggare
            }
        }
    }
}
