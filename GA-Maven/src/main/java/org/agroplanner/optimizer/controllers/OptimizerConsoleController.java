package org.agroplanner.optimizer.controllers;

import org.agroplanner.domainsystem.controllers.DomainConsoleController;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.domainsystem.views.ConsoleDomainView;
import org.agroplanner.shared.exceptions.EvolutionTimeoutException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;
import org.agroplanner.exportsystem.controllers.ExportConsoleController;
import org.agroplanner.exportsystem.controllers.ExportService;
import org.agroplanner.exportsystem.views.ConsoleExportView;
import org.agroplanner.gasystem.controllers.EvolutionConsoleController;
import org.agroplanner.gasystem.controllers.EvolutionService;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.views.ConsoleEvolutionView;
import org.agroplanner.optimizer.views.OptimizerViewContract;
import org.agroplanner.optimizer.views.ConsoleOptimizerView;

import java.util.Optional;
import java.util.Scanner;

public class OptimizerConsoleController {

    private final Scanner scanner;
    private final OptimizerViewContract appView;

    // Servizi Logici "Stateless" (Factory wrapper)
    private final DomainService domainService;
    private final ExportService exportService;

    public OptimizerConsoleController() {
        // Inizializzazione risorse condivise
        this.scanner = new Scanner(System.in);
        // Colleghiamo la View Concreta
        this.appView = new ConsoleOptimizerView(scanner);

        // Inizializzazione Servizi Logici Generali
        this.domainService = new DomainService();
        this.exportService = new ExportService();
    }

    public void run() {
        appView.showWelcomeMessage();

        while (true) {
            // GLOBAL SAFETY NET: Il try avvolge TUTTA la logica della sessione
            try {

                appView.showNewSessionMessage();

                // ====================================================
                // FASE 1: Creazione Dominio (Sottosistema 1)
                // ====================================================

                DomainConsoleController domainController = new DomainConsoleController(
                        new ConsoleDomainView(scanner),
                        domainService
                );

                Optional<Domain> domainOpt = domainController.runDomainCreationWizard();

                if (domainOpt.isEmpty()) {
                    appView.showExitMessage();
                    break; // USCITA DALL'APP (Esce dal while, e quindi anche dal try)
                }

                Domain problemDomain = domainOpt.get();


                // ====================================================
                // FASE 2: Configurazione Parametri
                // ====================================================

                double maxRadius = domainService.calculateMaxValidRadius(problemDomain);

                int individualSize = appView.askForIndividualSize();
                double pointRadius = appView.askForPointRadius(maxRadius);

                // ====================================================
                // FASE 3: Evoluzione (Sottosistema 2)
                // ====================================================

                // ⚠️ PUNTO CRITICO: Qui scatta la Deep Protection.
                // Se i parametri sono incoerenti, il costruttore lancia InvalidInputException/DomainConstraintException.
                // Grazie al try globale, ora le catturiamo!
                EvolutionService evolutionService = new EvolutionService(
                        problemDomain,
                        individualSize,
                        pointRadius
                );

                EvolutionConsoleController evolutionController = new EvolutionConsoleController(
                        new ConsoleEvolutionView(),
                        evolutionService
                );

                // Avvio il flusso evolutivo (può lanciare MaxAttemptsExceededException)
                Individual bestSolution = evolutionController.runEvolution();

                // Mostro i risultati finali
                appView.showSolutionValue(bestSolution.getFitness());
                if (appView.askIfPrintChromosome()) {
                    appView.printSolutionDetails(bestSolution.toString());
                }

                // ====================================================
                // FASE 4: Export (Sottosistema 3)
                // ====================================================

                ExportConsoleController exportController = new ExportConsoleController(
                        new ConsoleExportView(scanner),
                        exportService
                );

                exportController.runExportWizard(bestSolution, problemDomain, pointRadius);

                // --- GESTIONE ERRORI DELLA SESSIONE ---

            } catch (MaxAttemptsExceededException e) {
                // Caso 1: L'evoluzione non ha trovato soluzioni valide
                appView.showSessionAborted(e.getMessage());
                // Il ciclo while ricomincia -> Nuova sessione

            } catch (InvalidInputException e) {
                // Caso 2: Errore di Configurazione (Deep Protection ha bloccato parametri assurdi)
                appView.showSessionAborted("Configuration Error: " + e.getMessage());
                // Il ciclo while ricomincia -> Nuova sessione

            } catch (EvolutionTimeoutException e) { // <--- NUOVO CATCH
                appView.showSessionAborted("⏱️ TIME LIMIT REACHED: " + e.getMessage());

            } catch (Exception e) {
                // Caso 3: Errore Critico Imprevisto (es. OutOfMemory, NullPointer)
                // Impedisce al programma di chiudersi bruscamente
                appView.showSessionAborted("Critical System Error: " + e.toString());
                e.printStackTrace(); // Utile per capire cosa è successo dai log
                // Il ciclo while ricomincia -> Nuova sessione
            }
        }

        scanner.close();
    }
}