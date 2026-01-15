package org.agroplanner.orchestrator.controllers;

import org.agroplanner.access.controllers.AccessConsoleController;
import org.agroplanner.access.controllers.AccessService;
import org.agroplanner.access.model.User;
import org.agroplanner.access.views.AccessViewInterface;
import org.agroplanner.access.views.ConsoleAccessView;
import org.agroplanner.domainsystem.controllers.DomainConsoleController;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.views.ConsoleDomainView;
import org.agroplanner.exportsystem.controllers.ExportConsoleController;
import org.agroplanner.exportsystem.controllers.ExportService;
import org.agroplanner.exportsystem.views.ConsoleExportView;
import org.agroplanner.gasystem.controllers.EvolutionConsoleController;
import org.agroplanner.gasystem.controllers.EvolutionService;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.views.ConsoleEvolutionView;
import org.agroplanner.inventory.controller.InventoryConsoleController;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantVarietySheet;
import org.agroplanner.inventory.views.ConsoleInventoryView;
import org.agroplanner.orchestrator.views.ConsoleSystemView;
import org.agroplanner.orchestrator.views.SystemViewContract;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.persistence.controllers.PersistenceConsoleController;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.persistence.views.PersistenceConsoleView;
import org.agroplanner.shared.exceptions.EvolutionTimeoutException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Collectors;

/**
 * Runtime Orchestrator for the Command Line Interface environment.
 * <p>
 * Handles the main application loop, user session management, and sub-module navigation
 * within the terminal context.
 * </p>
 */
public class CLIOrchestrator {

    // --- INFRASTRUCTURE ---
    private final Scanner scanner;
    private final SystemViewContract systemView;
    private final AgroPersistenceFactory factory;
    private final boolean isDemoMode;

    // --- SERVICES ---
    private final DomainService domainService;
    private final ExportService exportService;

    // --- SESSION STATE ---
    private User currentUser;

    /**
     * Initializes the CLI environment with ready-to-use dependencies.
     */
    public CLIOrchestrator(AgroPersistenceFactory factory, DomainService domainService, ExportService exportService) {
        this.factory = factory;
        this.domainService = domainService;
        this.exportService = exportService;
        this.isDemoMode = factory.isVolatile();

        // CLI Specifics
        this.scanner = new Scanner(System.in);
        this.systemView = new ConsoleSystemView(scanner);
    }

    public void run() {
        systemView.showWelcomeMessage();
        boolean appRunning = true;

        while (appRunning) {
            try {
                // 1. Auth phase
                if (!ensureUserIsLoggedIn()) {
                    appRunning = false;
                    continue;
                }

                // 2. OP phase
                runDashboardLoop();

                // 3. specific Exit for demo
                if (isDemoMode && currentUser == null) {
                    appRunning = false;
                }

            } catch (Exception e) {
                handleGlobalError(e);
            }
        }

        systemView.showExitMessage();
        scanner.close();
    }

    // HELPER METHODS

    /**
     * Handles the login or guest user creation logic.
     * Returns true if the user is logged in and ready, false if they want to log out.
     */
    private boolean ensureUserIsLoggedIn() {
        if (currentUser != null) return true;

        if (isDemoMode) {
            systemView.showDemoModeActive();
            this.currentUser = factory.getUserDAO().findByUsername("guest_demo");
            if (this.currentUser == null) {
                this.currentUser = User.createGuestUser();
            }
            return true;
        } else {
            this.currentUser = runAccessModule();
            return this.currentUser != null;
        }
    }

    /**
     * Dashboard Loop and switch case
     */
    private void runDashboardLoop() {
        boolean insideDashboard = true;

        while (insideDashboard && currentUser != null) {
            int choice = systemView.askMainDashboardChoice(currentUser);

            switch (choice) {
                case 1:
                    runOptimizationSession();
                    break;
                case 2:
                    runLoadSequence();
                    break;
                case 3:
                    runCatalogManagement();
                    break;
                case 0:
                    performLogout();
                    insideDashboard = false;
                    break;
                default:
                    systemView.showUnknownCommand();
            }
        }
    }

    private void handleGlobalError(Exception e) {
        String errorMsg = (e.getMessage() != null) ? e.getMessage() : "Unknown Internal Error";
        systemView.showSessionAborted("Critical System Failure: " + errorMsg);
        currentUser = null;
    }

    // --- SUB-MODULES ---

    private User runAccessModule() {
        AccessService accessService = new AccessService(factory.getUserDAO());
        AccessViewInterface accessView = new ConsoleAccessView(scanner);
        return new AccessConsoleController(accessView, accessService).run();
    }

    private void performLogout() {
        currentUser = null;
        systemView.showLogoutMessage();
        if (isDemoMode) systemView.showDemoSessionEnded();
        DBConnection.getInstance().shutdown();
    }

    private void runCatalogManagement() {
        InventoryConsoleController controller = new InventoryConsoleController(new ConsoleInventoryView(scanner), factory.getPlantDAO());
        controller.runCatalogManager(currentUser);
    }

    private void runOptimizationSession() {
        boolean sessionActive = true;

        while (sessionActive) {
            try {
                systemView.showNewSessionMessage();

                // 1. DOMAIN
                DomainConsoleController domainController = new DomainConsoleController(new ConsoleDomainView(scanner), domainService);
                Optional<DomainDefinition> domainDefOpt = domainController.runDomainCreationWizard();
                if (domainDefOpt.isEmpty()) return;

                DomainDefinition domainDef = domainDefOpt.get();
                Domain problemDomain = domainService.createDomain(domainDef.getType(), domainDef.getParameters());

                // 2. INVENTORY
                double maxPhysRadius = domainService.calculateMaxValidRadius(problemDomain);
                InventoryConsoleController inventoryController = new InventoryConsoleController(new ConsoleInventoryView(scanner), factory.getPlantDAO());
                PlantInventory inventory = inventoryController.runInventoryWizard(maxPhysRadius);

                // 3. EVOLUTION
                EvolutionService evolutionService = new EvolutionService(problemDomain, inventory);
                EvolutionConsoleController evolutionController = new EvolutionConsoleController(new ConsoleEvolutionView(scanner), evolutionService);
                Individual bestSolution = evolutionController.runEvolution();
                evolutionController.handleResultDisplay(bestSolution);

                // 4. PERSISTENCE
                PersistenceConsoleController persistenceController = new PersistenceConsoleController(factory.getSolutionDAO(), new PersistenceConsoleView(scanner));
                persistenceController.runSaveSession(bestSolution, currentUser, isDemoMode, domainDef);

                // 5. EXPORT
                ExportConsoleController exportController = new ExportConsoleController(new ConsoleExportView(scanner), exportService);
                exportController.runExportWizard(bestSolution, problemDomain, inventory, isDemoMode);

                sessionActive = systemView.askForNewSession();

            } catch (MaxAttemptsExceededException | InvalidInputException | EvolutionTimeoutException e) {
                systemView.showSessionAborted(e.getMessage());
                sessionActive = false;
            }
        }
    }

    private void runLoadSequence() {
        PersistenceConsoleController persistenceController = new PersistenceConsoleController(factory.getSolutionDAO(), new PersistenceConsoleView(scanner));
        Optional<LoadedSession> sessionOpt = persistenceController.runLoadSelection(currentUser);

        if (sessionOpt.isEmpty()) return;

        LoadedSession session = sessionOpt.get();
        Individual solution = session.getSolution();

        // Reconstruct Data for Visualization
        java.util.Set<Integer> distinctIds = solution.getChromosomes().stream()
                .map(org.agroplanner.gasystem.model.Point::getVarietyId)
                .collect(Collectors.toSet());
        List<PlantVarietySheet> usedVarieties = factory.getPlantDAO().findAllByIds(distinctIds);

        systemView.showBootstrapInfo("Viewing Solution (Read-Only Mode)");

        new InventoryConsoleController(new ConsoleInventoryView(scanner), factory.getPlantDAO()).showSolutionManifest(usedVarieties);
        new EvolutionConsoleController(new ConsoleEvolutionView(scanner), null).handleResultDisplay(solution);

        exportBridgeLogic(session, solution, usedVarieties);
        systemView.waitForUserConfirmation();
    }

    private void exportBridgeLogic(LoadedSession session, Individual solution, List<PlantVarietySheet> usedVarieties) {
        if (systemView.askIfExportWanted()) {
            try {
                DomainDefinition def = session.getDomainDefinition();
                Domain reconstructedDomain = domainService.createDomain(def.getType(), def.getParameters());
                PlantInventory reconstructedInventory = PlantInventory.fromSolution(solution, usedVarieties);

                ExportConsoleController exportController = new ExportConsoleController(new ConsoleExportView(scanner), exportService);
                exportController.runExportWizard(solution, reconstructedDomain, reconstructedInventory, isDemoMode);
            } catch (Exception e) {
                systemView.showExportError(e.getMessage());
            }
        }
    }
}
