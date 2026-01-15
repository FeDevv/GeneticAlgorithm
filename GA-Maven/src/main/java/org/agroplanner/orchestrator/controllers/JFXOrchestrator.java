package org.agroplanner.orchestrator.controllers;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.agroplanner.access.controllers.AccessJFXController;
import org.agroplanner.access.controllers.AccessService;
import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.controllers.DomainJFXController;
import org.agroplanner.domainsystem.controllers.DomainService;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.exportsystem.controllers.ExportJFXController;
import org.agroplanner.exportsystem.controllers.ExportService;
import org.agroplanner.gasystem.controllers.EvolutionJFXController;
import org.agroplanner.gasystem.controllers.EvolutionService;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.controller.InventoryJFXController;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantVarietySheet;
import org.agroplanner.persistence.controllers.PersistenceJFXController;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.shared.exceptions.UIInitializationException;

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * JavaFX Runtime Orchestrator.
 * <p>
 * Acts as the application Router and Dependency Injection container for the GUI mode.
 * It manages the primary Stage and transitions between different Views (Scenes).
 * </p>
 */
public class JFXOrchestrator extends Application {

    // --- STATIC CONTEXT (Passed from Composition Root) ---
    private static AgroPersistenceFactory factory;
    private static DomainService domainService;
    private static ExportService exportService;

    // --- SESSION STATE ---
    private Stage primaryStage;
    private User currentUser;
    private boolean isDemoMode;

    /**
     * Static entry point invoked by SystemOrchestrator.
     */
    public static void launchApp(AgroPersistenceFactory f, DomainService d, ExportService e) {
        factory = f;
        domainService = d;
        exportService = e;
        launch(); // Starts JavaFX Thread
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.isDemoMode = factory.isVolatile();

        primaryStage.setTitle("AgroPlanner System v3.0");

        // Graceful Shutdown
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        if (isDemoMode) {
            // Auto-login as Guest
            this.currentUser = User.createGuestUser();
            goToDashboard();
        } else {
            goToLogin();
        }

        primaryStage.show();
    }

    // ==========================================
    // NAVIGATION: CORE
    // ==========================================

    public void logout() {
        this.currentUser = null;
        if (isDemoMode) {
            Platform.exit(); // Demo mode exit closes the app
        } else {
            goToLogin();
        }
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/access/views/AccessView.fxml"));
            Parent root = loader.load();

            AccessJFXController controller = loader.getController();
            // Inject Service & Success Callback
            controller.init(new AccessService(factory.getUserDAO()), authenticatedUser -> {
                this.currentUser = authenticatedUser;
                goToDashboard();
            });

            primaryStage.setScene(new Scene(root));
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showFatalError("Failed to load Login View", e);
        }
    }

    private void goToDashboard() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/orchestrator/views/DashboardView.fxml"));
            Parent root = loader.load();

            DashboardJFXController controller = loader.getController();
            controller.init(this, currentUser);

            primaryStage.setScene(new Scene(root));
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            showFatalError("Failed to load Dashboard", e);
        }
    }

    // ==========================================
    // FLOW: OPTIMIZATION WIZARD
    // ==========================================

    public void startOptimizationFlow() {
        goToDomainDefinition();
    }

    // STEP 1: Domain Definition
    private void goToDomainDefinition() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/domainsystem/views/DomainView.fxml"));
            Parent root = loader.load();

            DomainJFXController controller = loader.getController();
            controller.init(domainService, domainDef -> {
                // Instantiate Domain Model
                Domain domain = domainService.createDomain(domainDef.getType(), domainDef.getParameters());
                // Proceed to next step
                goToInventory(domain, domainDef);
            });

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Error loading Domain View", e);
        }
    }

    // STEP 2: Inventory Selection
    private void goToInventory(Domain domain, DomainDefinition def) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/inventory/views/InventoryView.fxml"));
            Parent root = loader.load();

            InventoryJFXController controller = loader.getController();
            double maxRadius = domainService.calculateMaxValidRadius(domain);

            controller.init(currentUser, maxRadius, factory.getPlantDAO(),
                    // On Confirm: Go to Evolution
                    inventory -> goToEvolution(domain, inventory, def, null),
                    // On Cancel: Back to Dashboard
                    this::goToDashboard
            );

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Error loading Inventory View", e);
        }
    }

    // STEP 3: Evolutionary Simulation
    private void goToEvolution(Domain domain, PlantInventory inventory, DomainDefinition def, Individual loadedSolution) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/gasystem/views/EvolutionView.fxml"));
            Parent root = loader.load();
            EvolutionJFXController controller = loader.getController();

            // Only create service if running a new simulation
            EvolutionService evoService = (loadedSolution == null) ? new EvolutionService(domain, inventory) : null;

            controller.init(
                    evoService,
                    domain,
                    def,
                    // On Export Requested
                    solution -> goToExport(solution, domain, inventory),
                    factory,
                    currentUser,
                    isDemoMode,
                    loadedSolution,
                    // On Exit
                    this::goToDashboard
            );

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Error loading Evolution View", e);
        }
    }

    // STEP 4: Export Wizard
    private void goToExport(Individual solution, Domain domain, PlantInventory inventory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/exportsystem/views/ExportView.fxml"));
            Parent root = loader.load();

            ExportJFXController controller = loader.getController();
            controller.init(exportService, solution, domain, inventory, isDemoMode, this::goToDashboard);

            primaryStage.setScene(new Scene(root));

        } catch (IOException e) {
            showFatalError("Error loading Export View", e);
        }
    }

    // ==========================================
    // FLOW: LOAD SESSION
    // ==========================================

    public void startLoadFlow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/persistence/views/LoadSessionView.fxml"));
            Parent root = loader.load();

            // Create Modal Stage
            Stage loadStage = new Stage();
            loadStage.initModality(Modality.APPLICATION_MODAL);
            loadStage.setTitle("Load Session");

            PersistenceJFXController controller = loader.getController();

            controller.initLoad(loadStage, factory.getSolutionDAO(), currentUser, loadedSession -> {
                // Session Loaded Callback
                try {
                    // Reconstruct Data Models
                    Individual solution = loadedSession.getSolution();
                    var def = loadedSession.getDomainDefinition();
                    Domain domain = domainService.createDomain(def.getType(), def.getParameters());

                    // Reconstruct Inventory from stored IDs
                    Set<Integer> varietyIds = solution.getChromosomes().stream()
                            .map(Point::getVarietyId)
                            .collect(Collectors.toSet());
                    List<PlantVarietySheet> usedVarieties = factory.getPlantDAO().findAllByIds(varietyIds);
                    PlantInventory reconstructedInventory = PlantInventory.fromSolution(solution, usedVarieties);

                    // Navigate to View Mode
                    goToEvolution(domain, reconstructedInventory, def, solution);

                } catch (Exception e) {
                    showFatalError("Failed to reconstruct session data", e);
                }
            });

            loadStage.setScene(new Scene(root));
            loadStage.showAndWait();

        } catch (IOException e) {
            showFatalError("Error loading Load View", e);
        }
    }

    // ==========================================
    // FLOW: CATALOG MANAGEMENT
    // ==========================================

    public void startCatalogFlow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/inventory/views/InventoryView.fxml"));
            Parent root = loader.load();

            InventoryJFXController controller = loader.getController();
            // Init in Catalog Mode
            controller.initCatalogMode(currentUser, factory.getPlantDAO(), this::goToDashboard);

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Error loading Catalog View", e);
        }
    }

    private void showFatalError(String msg, Exception e) {
        throw new UIInitializationException("CRITICAL UI ERROR: " + msg, e);
    }
}