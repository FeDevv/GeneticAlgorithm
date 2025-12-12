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

/**
 * <p><strong>The Main UC Orchestrator.</strong></p>
 *
 * <p>This controller manages the high-level lifecycle of the UC.
 * It acts as a <strong>Mediator</strong> between the three main subsystems:</p>
 * <ol>
 * <li><strong>Domain Subsystem:</strong> Definition of the geometric problem.</li>
 * <li><strong>Genetic Subsystem:</strong> Search for the optimal solution.</li>
 * <li><strong>Export Subsystem:</strong> Persistence of results.</li>
 * </ol>
 *
 * <p><strong>Resiliency Pattern:</strong> It implements a "Global Safety Net" (try-catch loop)
 * ensuring that exceptions in one session do not crash the entire JVM, allowing the user
 * to restart immediately.</p>
 */
public class OptimizerConsoleController {

    // ------------------- SHARED RESOURCES -------------------

    private final Scanner scanner;
    private final OptimizerViewContract appView;

    // ------------------- STATELESS SERVICES -------------------

    private final DomainService domainService;
    private final ExportService exportService;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the main controller and shared infrastructure.
     */
    public OptimizerConsoleController() {
        // Shared Input Source (System.in)
        // We open it once here and pass it down to all sub-controllers.
        this.scanner = new Scanner(System.in);

        // Main View
        this.appView = new ConsoleOptimizerView(scanner);

        // Service Initialization
        // These are stateless factories/calculators, so we instantiate them once.
        this.domainService = new DomainService();
        this.exportService = new ExportService();
    }

    // ------------------- MAIN LOOP -------------------

    /**
     * Starts the UC loop.
     */
    public void run() {
        appView.showWelcomeMessage();

        // --- SESSION LOOP ---
        // Keeps the UC alive across multiple problem-solving sessions.
        while (true) {

            // GLOBAL SAFETY NET:
            // Wraps the entire logic of a single session. If anything goes wrong inside
            // (Domain creation, Evolution, Export), the exception bubbles up here,
            // is logged, and the loop restarts cleanly.
            try {

                appView.showNewSessionMessage();

                // ====================================================
                // PHASE 1: DOMAIN DEFINITION (Subsystem A)
                // ====================================================

                DomainConsoleController domainController = new DomainConsoleController(
                        new ConsoleDomainView(scanner),
                        domainService
                );

                // Run the Domain Wizard
                Optional<Domain> domainOpt = domainController.runDomainCreationWizard();

                // Handle Exit Request
                if (domainOpt.isEmpty()) {
                    appView.showExitMessage();
                    break; // Breaks the while loop -> Application Shutdown
                }

                Domain problemDomain = domainOpt.get();


                // ====================================================
                // PHASE 2: PARAMETER CONFIGURATION
                // ====================================================

                // Logic: Calculate strict constraints based on the chosen domain.
                double maxRadius = domainService.calculateMaxValidRadius(problemDomain);

                // UI: Collect user inputs
                int individualSize = appView.askForIndividualSize();
                double pointRadius = appView.askForPointRadius(maxRadius);

                // ====================================================
                // PHASE 3: EVOLUTION (Subsystem B)
                // ====================================================

                // ⚠️ DEEP PROTECTION CHECKPOINT:
                // Instantiating the EvolutionService triggers strict validation logic.
                // If parameters (size, radius) are incoherent, an InvalidInputException is thrown here.
                EvolutionService evolutionService = new EvolutionService(
                        problemDomain,
                        individualSize,
                        pointRadius
                );

                EvolutionConsoleController evolutionController = new EvolutionConsoleController(
                        new ConsoleEvolutionView(), // View is stateless/simple here
                        evolutionService
                );

                // Run the Evolution Engine (Blocking call)
                // Can throw MaxAttemptsExceededException or EvolutionTimeoutException
                Individual bestSolution = evolutionController.runEvolution();

                // Show Results
                appView.showSolutionValue(bestSolution.getFitness());
                if (appView.askIfPrintChromosome()) {
                    appView.printSolutionDetails(bestSolution.toString());
                }

                // ====================================================
                // PHASE 4: EXPORT (Subsystem C)
                // ====================================================

                ExportConsoleController exportController = new ExportConsoleController(
                        new ConsoleExportView(scanner),
                        exportService
                );

                exportController.runExportWizard(bestSolution, problemDomain, pointRadius);

                // ====================================================
                // ERROR HANDLING (Session Recovery)
                // ====================================================

            } catch (MaxAttemptsExceededException e) {
                // Scenario: Algorithm failed to converge after N retries.
                appView.showSessionAborted(e.getMessage());
                // Loop continues -> New Session

            } catch (InvalidInputException e) {
                // Scenario: User entered valid numbers, but invalid logic (e.g. radius > domain size).
                // Caught here thanks to Deep Protection in Service constructors.
                appView.showSessionAborted("Configuration Error: " + e.getMessage());
                // Loop continues -> New Session

            } catch (EvolutionTimeoutException e) {
                // Scenario: The calculation took too long.
                appView.showSessionAborted("⏱️ TIME LIMIT REACHED: " + e.getMessage());
                // Loop continues -> New Session

            } catch (Exception e) {
                // Scenario: Unexpected Crash (NPE, OOM, etc.)
                appView.showSessionAborted("Critical System Error: " + e);
                //e.printStackTrace(); // Helpful for debugging
                // Loop continues -> New Session
            }
        }

        // Cleanup
        scanner.close();
    }
}