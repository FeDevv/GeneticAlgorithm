package org.agroplanner.orchestrator.controllers;

import org.agroplanner.boot.controllers.BootConsoleController;
import org.agroplanner.boot.model.AgroConfiguration;
import org.agroplanner.boot.view.ConsoleBootView;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.exportsystem.controllers.ExportService;
import org.agroplanner.persistence.controllers.PersistenceService;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.util.Scanner;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * The <strong>Composition Root</strong>.
 * <p>
 * This class is solely responsible for:
 * <ol>
 * <li>Running the Boot sequence (Configuration).</li>
 * <li>Initializing the Infrastructure (Persistence Layer).</li>
 * <li>Dispatching control to the appropriate Runtime Orchestrator (CLI or GUI).</li>
 * </ol>
 * </p>
 */
public class SystemOrchestrator {
    private static final Logger LOGGER = Logger.getLogger(SystemOrchestrator.class.getName());

    public void run() {
        // 1. BOOT SEQUENCE
        // We use a temporary Scanner just for the boot phase
        Scanner bootScanner = new Scanner(System.in);
        BootConsoleController bootController = new BootConsoleController(new ConsoleBootView(bootScanner));
        AgroConfiguration config = bootController.runBootSequence();

        // 2. INFRASTRUCTURE INITIALIZATION
        PersistenceService persistenceService = PersistenceService.getInstance();
        try {
            persistenceService.initialize(config);
        } catch (Exception e) {
            LOGGER.log(Level.SEVERE, "FATAL ERROR: Persistence Initialization failed", e);
            return;
        }

        // 3. RESOURCE WIRING
        AgroPersistenceFactory factory = persistenceService.getFactory();

        // Shared Services (Stateless)
        DomainService domainService = new DomainService();
        ExportService exportService = new ExportService();

        // 4. STORAGE CHECK
        try {
            factory.getUserDAO().initStorage();
            factory.getPlantDAO().initStorage();
            factory.getSolutionDAO().initStorage();
        } catch (DataPersistenceException e) {
            LOGGER.log(Level.SEVERE, "FATAL STARTUP ERROR (Storage Init)", e);
            return;
        }

        // 5. RUNTIME DISPATCH
        if (config.isGuiActive()) {
            LOGGER.info("[KERNEL] Launching JavaFX Environment...");
            JFXOrchestrator.launchApp(factory, domainService, exportService);
        } else {
            new CLIOrchestrator(factory, domainService, exportService).run();
        }

    }
}
