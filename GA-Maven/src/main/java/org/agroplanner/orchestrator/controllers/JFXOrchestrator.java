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

import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Orchestrator Runtime per l'ambiente JavaFX.
 * Gestisce il ciclo di vita delle finestre e la transizione tra le scene (Router).
 */
public class JFXOrchestrator extends Application {

    // --- STATIC CONTEXT (Per passare dati dal SystemOrchestrator) ---
    private static AgroPersistenceFactory factory;
    private static DomainService domainService;
    private static ExportService exportService;

    // --- STATE ---
    private Stage primaryStage;
    private User currentUser;
    private boolean isDemoMode;

    /**
     * Entry point statico chiamato dal SystemOrchestrator.
     */
    public static void launchApp(AgroPersistenceFactory f, DomainService d, ExportService e) {
        factory = f;
        domainService = d;
        exportService = e;
        launch(); // Avvia il thread JavaFX
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        this.isDemoMode = factory.isVolatile();

        primaryStage.setTitle("AgroPlanner System v3.0");

        // Gestione chiusura corretta
        primaryStage.setOnCloseRequest(e -> {
            Platform.exit();
            System.exit(0);
        });

        if (isDemoMode) {
            // Login automatico guest
            this.currentUser = User.createGuestUser();
            goToDashboard();
        } else {
            goToLogin();
        }

        primaryStage.show();
    }

    // ==========================================
    // NAVIGATION: MAIN SCREENS
    // ==========================================

    public void logout() {
        this.currentUser = null;
        if (isDemoMode) {
            Platform.exit(); // In demo mode logout chiude l'app
        } else {
            goToLogin();
        }
    }

    private void goToLogin() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/access/views/AccessView.fxml"));
            Parent root = loader.load();

            AccessJFXController controller = loader.getController();
            // Dependency Injection & Callback
            controller.init(new AccessService(factory.getUserDAO()), authenticatedUser -> {
                this.currentUser = authenticatedUser;
                goToDashboard();
            });

            primaryStage.setScene(new Scene(root));
            primaryStage.centerOnScreen();
        } catch (IOException e) {
            e.printStackTrace();
            showFatalError("Impossibile caricare Login View", e);
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
            showFatalError("Impossibile caricare Dashboard", e);
        }
    }

    // ==========================================
    // FLOW: OPTIMIZATION WIZARD
    // ==========================================

    public void startOptimizationFlow() {
        goToDomainDefinition();
    }

    // STEP 1: Domain
    private void goToDomainDefinition() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/domainsystem/views/DomainView.fxml"));
            Parent root = loader.load();

            DomainJFXController controller = loader.getController();
            controller.init(domainService, domainDef -> {
                // Qui abbiamo la definizione creata dall'utente.
                Domain domain = domainService.createDomain(domainDef.getType(), domainDef.getParameters());

                // PASSIAMO ENTRAMBI AL PROSSIMO STEP
                goToInventory(domain, domainDef);
            });

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Errore caricamento Domain View", e);
        }
    }

    // STEP 2: Inventory
    private void goToInventory(Domain domain, DomainDefinition def) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/inventory/views/InventoryView.fxml"));
            Parent root = loader.load();

            InventoryJFXController controller = loader.getController();
            double maxRadius = domainService.calculateMaxValidRadius(domain);

            controller.init(currentUser, maxRadius, factory.getPlantDAO(),
                    // Callback Conferma:
                    // Grazie alle "Closure" di Java, 'def' è ancora visibile qui dentro!
                    inventory -> goToEvolution(domain, inventory, def, null),

                    // Callback Annulla:
                    () -> goToDashboard()
            );

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Errore caricamento Inventory View", e);
        }
    }

    // STEP 3: Evolution
    private void goToEvolution(Domain domain, PlantInventory inventory, DomainDefinition def, Individual loadedSolution) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/gasystem/views/EvolutionView.fxml"));
            Parent root = loader.load();
            EvolutionJFXController controller = loader.getController();

            EvolutionService evoService = (loadedSolution == null) ? new EvolutionService(domain, inventory) : null;

            controller.init(
                    evoService,
                    domain,
                    inventory,
                    def,
                    solution -> goToExport(solution, domain, inventory),
                    factory,
                    currentUser,
                    isDemoMode,
                    loadedSolution,
                    () -> goToDashboard() // <--- ULTIMO PARAMETRO: TORNA ALLA DASHBOARD
            );

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            showFatalError("Errore caricamento Evolution View", e);
        }
    }

    // STEP 4: Export
    private void goToExport(Individual solution, Domain domain, PlantInventory inventory) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/exportsystem/views/ExportView.fxml"));
            Parent root = loader.load();

            ExportJFXController controller = loader.getController();

            // Passiamo l'ultimo parametro: () -> goToDashboard()
            controller.init(exportService, solution, domain, inventory, isDemoMode, () -> goToDashboard());

            primaryStage.setScene(new Scene(root));

        } catch (IOException e) {
            showFatalError("Errore Export View", e);
        }
    }

    // ==========================================
    // FLOW: LOAD SESSION
    // ==========================================

    public void startLoadFlow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/persistence/views/LoadSessionView.fxml"));
            Parent root = loader.load();

            // Creiamo un nuovo Stage (Modale)
            Stage loadStage = new Stage();
            loadStage.initModality(Modality.APPLICATION_MODAL);
            loadStage.setTitle("Carica Sessione");

            PersistenceJFXController controller = loader.getController();

            controller.initLoad(loadStage, factory.getSolutionDAO(), currentUser, loadedSession -> {
                // CALLBACK: Sessione caricata con successo dal DB
                try {
                    // A. Recuperiamo i dati grezzi
                    Individual solution = loadedSession.getSolution();
                    var def = loadedSession.getDomainDefinition();

                    // B. Ricostruiamo il DOMINIO (Terreno)
                    Domain domain = domainService.createDomain(def.getType(), def.getParameters());

                    // C. Ricostruiamo l'INVENTARIO (Piante)
                    // 1. Estraiamo tutti gli ID delle varietà usate nella soluzione (senza duplicati)
                    Set<Integer> varietyIds = solution.getChromosomes().stream()
                            .map(Point::getVarietyId)
                            .collect(Collectors.toSet());

                    // 2. Chiediamo al DAO di darci le schede tecniche di questi ID
                    List<PlantVarietySheet> usedVarieties = factory.getPlantDAO().findAllByIds(varietyIds);

                    // 3. Usiamo il metodo statico per ricostruire l'oggetto Inventory completo
                    PlantInventory reconstructedInventory = PlantInventory.fromSolution(solution, usedVarieties);

                    // D. Navighiamo verso la visualizzazione (Evolution View in modalità Read-Only)
                    goToEvolution(domain, reconstructedInventory, def, solution);

                } catch (Exception e) {
                    showFatalError("Errore durante la ricostruzione della sessione", e);
                }
            });

            loadStage.setScene(new Scene(root));
            loadStage.showAndWait();

        } catch (IOException e) {
            showFatalError("Errore caricamento Load View", e);
        }
    }

    // ==========================================
    // FLOW: CATALOG
    // ==========================================

    public void startCatalogFlow() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/org/agroplanner/inventory/views/InventoryView.fxml"));
            Parent root = loader.load();

            InventoryJFXController controller = loader.getController();

            // CHIAMA IL NUOVO INIT
            controller.initCatalogMode(currentUser, factory.getPlantDAO(), () -> {
                goToDashboard(); // Callback per tornare indietro
            });

            primaryStage.setScene(new Scene(root));
        } catch (IOException e) {
            e.printStackTrace(); // O usa il tuo showFatalError
        }
    }

    private void showFatalError(String msg, Exception e) {
        System.err.println(msg);
        e.printStackTrace();
    }
}