package org.agroplanner.orchestrator.controllers;

import org.agroplanner.boot.controllers.BootConsoleController;
import org.agroplanner.boot.model.AgroConfiguration;
import org.agroplanner.boot.view.ConsoleBootView;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.persistence.controllers.PersistenceService;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.shared.exceptions.DataPersistenceException;
import org.agroplanner.exportsystem.controllers.ExportService;

import java.util.Scanner;


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
            System.err.println("FATAL ERROR: Persistence Initialization failed -> " + e.getMessage());
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
            System.err.println("FATAL STARTUP ERROR (Storage Init): " + e.getMessage());
            return;
        }

        //System.out.println(" [KERNEL] System Engine Loaded: " + config.getPersistenceType());

        // 5. RUNTIME DISPATCH
        if (config.isGuiActive()) {
            System.out.println(" [KERNEL] Launching JavaFX Environment...");
            // Passiamo le dipendenze staticamente prima di lanciare
            JFXOrchestrator.launchApp(factory, domainService, exportService);
        } else {
            new CLIOrchestrator(factory, domainService, exportService).run();
        }

        // Boot scanner is not closed here because CLIOrchestrator manages its own System.in stream
        // or reuses the stream. In CLI apps, closing System.in is usually final.
    }
}
