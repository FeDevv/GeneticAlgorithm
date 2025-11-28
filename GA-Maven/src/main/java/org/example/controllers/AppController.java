package org.example.controllers;

import org.example.model.Individual;
import org.example.model.domains.Domain;
import org.example.views.AppConsoleView;
import org.example.views.DomainConsoleView;
import org.example.views.EvolutionConsoleView;
import org.example.views.ExportConsoleView;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.Scanner;

/**
 * Controller principale (Overseer).
 * Responsabile dell'inizializzazione dell'applicazione e del coordinamento
 * tra i sottosistemi (Dominio, Configurazione, Evoluzione, Export).
 */
public class AppController {

    private final Scanner scanner;
    private final AppConsoleView appView;

    public AppController() {
        // Inizializza le risorse condivise
        this.scanner = new Scanner(System.in);
        this.appView = new AppConsoleView(scanner);
    }

    /**
     * Metodo principale che avvia l'intero flusso dell'applicazione.
     */
    public void run() {
        // Messaggio di benvenuto (mostrato solo una volta all'avvio)
        appView.showWelcomeMessage();

        // --- INIZIO CICLO PRINCIPALE ---
        while (true) {

            appView.showNewSessionMessage();

            // --- FASE 1: SELEZIONE E CREAZIONE DOMINIO ---
            DomainConsoleView domainView = new DomainConsoleView(scanner);
            DomainController domainController = new DomainController(domainView);

            // Questo è il punto di uscita: se l'utente sceglie "0", createDomain ritorna Empty
            Optional<Domain> domainOptional = domainController.createDomain();

            if (domainOptional.isEmpty()) {
                // L'utente ha chiesto di uscire
                appView.exitOnDemand(); // Stampa i saluti
                break; // <--- ESCE DAL WHILE (IL PROGRAMMA TERMINA)
            }

            Domain problemDomain = domainOptional.get();

            // --- FASE 2: CONFIGURAZIONE PARAMETRI ---
            int individualSize = appView.readIndividualSize();

            double maxRadiusLimit = calculateMaxRadius(problemDomain);
            double pointRadius = appView.readPointRadius(maxRadiusLimit);

            // --- FASE 3: MOTORE EVOLUTIVO ---
            appView.showEvolutionStart();

            EvolutionEngine engine = new EvolutionEngine(
                    new EvolutionConsoleView(),
                    problemDomain,
                    individualSize,
                    pointRadius
            );

            Individual bestSolution = engine.runEvolutionEngine();

            // Output risultato finale
            appView.showSolutionValue(bestSolution.getFitness());

            if (appView.askYesNo("Do you wish to print the individual chromosomes to console?")) {
                appView.printSolutionDetails(bestSolution.toString());
            }

            // --- FASE 4: EXPORT ---
            ExportConsoleView exportView = new ExportConsoleView(scanner);
            ExportController exportController = new ExportController(exportView);

            exportController.handleExport(bestSolution, problemDomain, pointRadius);

        }

        // chiusura risorse
        scanner.close();
    }

    /**
     * Calcola il raggio massimo consentito basandosi sul Bounding Box del dominio.
     * * NOTA ARCHITETTURALE:
     * Questo metodo risiede nel Controller (e non nel model Domain) perché rappresenta
     * un vincolo di validazione dell'input utente ("Business Logic" dell'applicazione),
     * derivato dalle proprietà del dominio, piuttosto che una proprietà intrinseca del dominio stesso.
     */
    private double calculateMaxRadius(Domain domain) {
        Rectangle2D boundingBox = domain.getBoundingBox();
        double boxWidth = boundingBox.getWidth();
        double boxHeight = boundingBox.getHeight();
        return Math.min(boxWidth, boxHeight) / 2.0;
    }
}
