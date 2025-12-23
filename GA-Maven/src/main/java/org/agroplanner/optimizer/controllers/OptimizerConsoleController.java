package org.agroplanner.optimizer.controllers;

import org.agroplanner.domainsystem.controllers.DomainConsoleController;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.domainsystem.views.ConsoleDomainView;
import org.agroplanner.inventory.view.ConsoleInventoryView;
import org.agroplanner.inventory.controller.InventoryConsoleController;
import org.agroplanner.inventory.model.PlantInventory;
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
 * The <strong>UC Orchestrator</strong> (Main Controller).
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Mediator. This class coordinates the interaction between four independent subsystems:
 * <ol>
 * <li><strong>Domain System:</strong> Geometry definition.</li>
 * <li><strong>Inventory System:</strong> Biological requirements.</li>
 * <li><strong>GA System:</strong> Optimization engine.</li>
 * <li><strong>Export System:</strong> Result persistence.</li>
 * </ol>
 * None of these subsystems know about each other; they communicate exclusively through this controller, ensuring loose coupling.
 * </li>
 * <li><strong>Resiliency (Global Safety Net):</strong> Implements a high-level {@code try-catch} loop that wraps
 * the entire user session. This guarantees that unhandled exceptions (like Timeouts or Input Errors)
 * abort the current session gracefully rather than crashing the JVM.</li>
 * <li><strong>Resource Ownership:</strong> Manages the lifecycle of shared infrastructure, specifically the
 * {@link Scanner} attached to {@code System.in}, preventing "Stream Closed" errors common in CLI apps.</li>
 * </ul>
 */
public class OptimizerConsoleController {

    // ------------------- SHARED RESOURCES -------------------

    private final Scanner scanner;
    private final OptimizerViewContract optView;

    // ------------------- STATELESS SERVICES -------------------
    // Pre-instantiated services that don't hold session state.

    private final DomainService domainService;
    private final ExportService exportService;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Bootstraps the application controller.
     * <p>Initializes the shared input stream and the top-level view.</p>
     */
    public OptimizerConsoleController() {
        // Resource Management:
        // We open System.in ONCE here and pass it down to all sub-controllers.
        // This is crucial because closing a Scanner wrapper also closes the underlying stream.
        this.scanner = new Scanner(System.in);

        // Main View
        this.optView = new ConsoleOptimizerView(scanner);

        // Core Services Setup
        this.domainService = new DomainService();
        this.exportService = new ExportService();
    }

    // ------------------- MAIN LOOP -------------------

    /**
     * Starts the main application loop.
     *
     * <p><strong>Lifecycle:</strong></p>
     * Keeps the application alive across multiple problem-solving sessions until the user explicitly requests exit.
     */
    public void run() {
        optView.showWelcomeMessage();

        boolean isRunning = true;

        // --- SESSION LOOP ---
        while (isRunning) {

            // GLOBAL SAFETY NET
            // Protects the loop from crashing. Any error resets the flow to the beginning of the loop.
            try {

                optView.showNewSessionMessage();

                // ====================================================
                // PHASE 1: DOMAIN DEFINITION (Geometry)
                // ====================================================

                // Manual Dependency Injection (Wiring):
                // We inject the View and Service into the Controller.
                DomainConsoleController domainController = new DomainConsoleController(
                        new ConsoleDomainView(scanner),
                        domainService
                );

                // Execution:
                Optional<Domain> domainOpt = domainController.runDomainCreationWizard();

                // Exit Guard: User cancelled domain creation
                if (domainOpt.isEmpty()) {
                    optView.showExitMessage();
                    isRunning = false; // Breaks the while loop -> Application Shutdown
                    continue;
                }

                Domain problemDomain = domainOpt.get();


                // ====================================================
                // PHASE 2: INVENTORY CONFIGURATION (Biology)
                // ====================================================

                // Logic Bridge: Domain Service calculates constraints for the Inventory.
                double maxPhysRadius = domainService.calculateMaxValidRadius(problemDomain);

                InventoryConsoleController inventoryController = new InventoryConsoleController(
                        new ConsoleInventoryView(scanner)
                );

                // Execution:
                PlantInventory inventory = inventoryController.runInventoryWizard(maxPhysRadius);

                // ====================================================
                // PHASE 3: EVOLUTION (Optimization)
                // ====================================================

                // ⚠️ DEEP PROTECTION: Service Instantiation
                // This constructor validates that the Inventory physically fits in the Domain.
                // Throws InvalidInputException if constraints are violated.
                EvolutionService evolutionService = new EvolutionService(
                        problemDomain,
                        inventory
                );

                EvolutionConsoleController evolutionController = new EvolutionConsoleController(
                        new ConsoleEvolutionView(), // Pure output view, no scanner needed here
                        evolutionService
                );

                // Execution (Heavy Computation):
                Individual bestSolution = evolutionController.runEvolution();

                // Immediate Feedback:
                optView.showSolutionValue(bestSolution.getFitness());
                if (optView.askIfPrintChromosome()) {
                    optView.printSolutionDetails(bestSolution.toString());
                }

                // ====================================================
                // PHASE 4: EXPORT
                // ====================================================

                ExportConsoleController exportController = new ExportConsoleController(
                        new ConsoleExportView(scanner),
                        exportService
                );

                // Execution:
                exportController.runExportWizard(bestSolution, problemDomain, inventory);

                // ====================================================
                // PHASE 5: TEARDOWN / RESTART
                // ====================================================

                boolean restart = optView.askForNewSession();

                if (!restart) {
                    optView.showExitMessage();
                    isRunning = false;
                }

            } catch (MaxAttemptsExceededException e) {
                // Recovery: Algorithm divergence.
                optView.showSessionAborted(e.getMessage());
                // Loop continues...

            } catch (InvalidInputException e) {
                // Recovery: Logical configuration error (e.g. Plants too big).
                optView.showSessionAborted("Configuration Error: " + e.getMessage());
                // Loop continues...

            } catch (EvolutionTimeoutException e) {
                // Recovery: Computation timeout.
                optView.showSessionAborted("⏱️ TIME LIMIT REACHED: " + e.getMessage());
                // Loop continues...

            } catch (Exception e) {
                // Recovery: Unexpected Runtime Exception (The generic catch-all).
                optView.showSessionAborted("Critical System Error: " + e);
                //e.printStackTrace(); // Helpful for debugging
                // Loop continues...
            }
        }

        // Cleanup
        scanner.close();
    }
}