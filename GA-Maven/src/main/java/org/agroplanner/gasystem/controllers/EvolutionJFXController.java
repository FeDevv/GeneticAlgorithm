package org.agroplanner.gasystem.controllers;

import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.persistence.controllers.PersistenceJFXController;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;

import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

/**
 * Controller JavaFX per il sistema evolutivo.
 * Gestisce sia la simulazione (Task asincroni) che la visualizzazione di sessioni caricate.
 */
public class EvolutionJFXController {

    // --- DEPENDENCIES ---
    private EvolutionService service;
    private Domain domain;
    private DomainDefinition domainDefinition;
    private Consumer<Individual> onEvolutionComplete;
    private AgroPersistenceFactory factory;
    private User currentUser;
    private boolean isDemoMode;
    private PlantInventory currentInventory;
    private Runnable onExitCallback;

    // --- STATE ---
    private Individual bestSolution;
    private static final int MAX_ATTEMPTS = 3;

    // --- FXML ---
    @FXML private TextArea consoleArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label fitnessLabel;
    @FXML private Button startButton;
    @FXML private Button saveButton;
    @FXML private Button exportButton;

    // Tabella Dettagli (Nuova)
    @FXML private TableView<PointViewModel> pointsTable;
    @FXML private TableColumn<PointViewModel, String> colPtName;
    @FXML private TableColumn<PointViewModel, Number> colPtX;
    @FXML private TableColumn<PointViewModel, Number> colPtY;

    @FXML private Label placeholderLabel;
    @FXML private StackPane canvasContainer;
    @FXML private Canvas plantCanvas;

    /**
     * Metodo di inizializzazione principale.
     */
    public void init(EvolutionService service, Domain domain, PlantInventory inventory, DomainDefinition domainDef, Consumer<Individual> onExportRequested,
                     AgroPersistenceFactory factory, User user, boolean isDemoMode, Individual loadedSolution, Runnable onExit) {
        this.service = service;
        this.domain = domain;
        this.currentInventory = inventory;
        this.domainDefinition = domainDef;
        this.onEvolutionComplete = onExportRequested;

        this.factory = factory;
        this.currentUser = user;
        this.isDemoMode = isDemoMode;
        this.onExitCallback = onExit;

        // Configura le colonne della tabella
        setupTable();

        // Rende il canvas responsive
        plantCanvas.widthProperty().bind(canvasContainer.widthProperty());
        plantCanvas.heightProperty().bind(canvasContainer.heightProperty());

        // Ridisegna se la finestra viene ridimensionata
        canvasContainer.widthProperty().addListener(o -> redrawCurrentSolution());
        canvasContainer.heightProperty().addListener(o -> redrawCurrentSolution());

        // LOGICA DUAL MODE
        if (loadedSolution != null) {
            // --- MODALITÀ VISUALIZZAZIONE (Caricato da DB) ---
            this.bestSolution = loadedSolution;

            // 1. Aggiorna Grafico, Tabella e UI
            updateUIWithSolution(bestSolution);

            // 2. Nascondi controlli di simulazione e salvataggio
            startButton.setVisible(false);
            progressBar.setVisible(false);
            saveButton.setVisible(false);

            // 3. Abilita subito Export
            exportButton.setDisable(false);

        } else {
            // --- MODALITÀ CALCOLO (Nuova sessione) ---
            startButton.setVisible(true);
            saveButton.setVisible(true);
            saveButton.setDisable(true);
            exportButton.setDisable(true);
            placeholderLabel.setVisible(true);
        }
    }

    private void setupTable() {
        colPtName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name));
        colPtX.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().x));
        colPtY.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().y));

        // Formattazione numeri a 2 decimali
        colPtX.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item.doubleValue()));
            }
        });
        colPtY.setCellFactory(tc -> new javafx.scene.control.TableCell<>() {
            @Override protected void updateItem(Number item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : String.format("%.2f", item.doubleValue()));
            }
        });
    }

    /**
     * Aggiorna tutta l'interfaccia (Grafico + Tabella) con una soluzione data.
     */
    private void updateUIWithSolution(Individual solution) {
        // 1. Disegna i punti
        drawSimplePoints(solution);

        // 2. Aggiorna Label e nasconde il placeholder
        placeholderLabel.setVisible(false);
        fitnessLabel.setText(String.format("%.6f", solution.getFitness()));

        // 3. Popola la tabella
        ObservableList<PointViewModel> list = FXCollections.observableArrayList();
        for (Point p : solution.getChromosomes()) {
            list.add(new PointViewModel(p.getVarietyName(), p.getX(), p.getY()));
        }
        pointsTable.setItems(list);
    }


    @FXML
    private void handleRunEvolution() {
        startButton.setDisable(true);
        saveButton.setDisable(true);
        exportButton.setDisable(true);
        progressBar.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        consoleArea.clear();

        EvolutionTask task = new EvolutionTask();

        task.messageProperty().addListener((obs, oldMsg, newMsg) -> {
            consoleArea.appendText(newMsg + "\n");
        });

        task.setOnSucceeded(e -> {
            this.bestSolution = task.getValue();
            progressBar.setProgress(1.0);
            startButton.setDisable(false);
            startButton.setText("Riavvia");

            // Aggiorna UI completa (Grafico + Tabella)
            updateUIWithSolution(bestSolution);
            consoleArea.appendText("✅ Simulazione completata con successo.\n");

            saveButton.setDisable(false);
            exportButton.setDisable(true);
        });

        task.setOnFailed(e -> {
            progressBar.setProgress(0);
            startButton.setDisable(false);
            consoleArea.appendText("⛔ ERRORE: " + task.getException().getMessage() + "\n");
            Alert alert = new Alert(Alert.AlertType.ERROR, "Simulazione Fallita: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSaveAction() {
        if (bestSolution == null) return;

        PersistenceJFXController.runSaveWorkflow(
                factory.getSolutionDAO(),
                bestSolution,
                currentUser,
                isDemoMode,
                this.domainDefinition,
                () -> {
                    exportButton.setDisable(false);
                    saveButton.setDisable(true);
                    saveButton.setText("Salvato ✅");
                }
        );
    }

    @FXML
    private void handleExportAction() {
        if (onEvolutionComplete != null && bestSolution != null) {
            onEvolutionComplete.accept(bestSolution);
        }
    }

    @FXML
    private void handleExitAction() {
        if (onExitCallback != null) {
            onExitCallback.run();
        }
    }

    // =========================================================
    // LOGICA ASINCRONA (Task)
    // =========================================================

    private class EvolutionTask extends Task<Individual> {
        @Override
        protected Individual call() throws Exception {
            updateMessage("🧬 Inizializzazione Algoritmo Genetico...");

            int currentAttempt = 0;
            Individual lastResult = null;

            do {
                currentAttempt++;
                long start = System.currentTimeMillis();
                updateMessage(String.format("🔄 Tentativo %d di %d in corso...", currentAttempt, MAX_ATTEMPTS));

                lastResult = service.executeEvolutionCycle();
                long duration = System.currentTimeMillis() - start;

                if (service.isValidSolution(lastResult)) {
                    updateMessage(String.format("✅ Soluzione valida trovata in %.2fs", duration / 1000.0));
                    return lastResult;
                }

                updateMessage(String.format("⚠️ Convergenza su soluzione invalida (Score: %.4f). Riprovo...", lastResult.getFitness()));

            } while (currentAttempt < MAX_ATTEMPTS);

            throw new MaxAttemptsExceededException("Impossibile trovare una soluzione valida dopo " + MAX_ATTEMPTS + " tentativi.");
        }
    }

    // =========================================================
    // VISUALIZZAZIONE GRAFICA
    // =========================================================

    private void redrawCurrentSolution() {
        if (bestSolution != null) {
            drawSimplePoints(bestSolution);
        }
    }

    private void drawSimplePoints(Individual solution) {
        GraphicsContext gc = plantCanvas.getGraphicsContext2D();
        double w = plantCanvas.getWidth();
        double h = plantCanvas.getHeight();

        gc.clearRect(0, 0, w, h);
        if (w < 10 || h < 10) return;

        Rectangle2D bounds = domain.getBoundingBox();
        double domainX = bounds.getX();
        double domainY = bounds.getY();
        double domainW = bounds.getWidth();
        double domainH = bounds.getHeight();

        double margin = 30;
        double availW = w - (margin * 2);
        double availH = h - (margin * 2);

        double scale = Math.min(availW / domainW, availH / domainH);

        double offsetX = margin + (availW - (domainW * scale)) / 2.0;
        double offsetY = margin + (availH - (domainH * scale)) / 2.0;

        // Bordo Dominio
        gc.setStroke(Color.web("#e0e0e0"));
        gc.setLineWidth(1.0);
        gc.strokeRect(offsetX, offsetY, domainW * scale, domainH * scale);

        // Punti
        double dotRadius = 3.0;
        for (Point p : solution.getChromosomes()) {
            double normalizedX = p.getX() - domainX;
            double normalizedY = p.getY() - domainY;

            double screenX = offsetX + (normalizedX * scale);
            double screenY = offsetY + (normalizedY * scale);

            gc.setFill(Color.FORESTGREEN);
            gc.fillOval(screenX - dotRadius, screenY - dotRadius, dotRadius * 2, dotRadius * 2);

            gc.setStroke(Color.web("#1b5e20"));
            gc.setLineWidth(0.5);
            gc.strokeOval(screenX - dotRadius, screenY - dotRadius, dotRadius * 2, dotRadius * 2);
        }
    }

    // =========================================================
    // INNER CLASS PER LA TABELLA
    // =========================================================
    public static class PointViewModel {
        String name;
        double x;
        double y;
        public PointViewModel(String name, double x, double y) {
            this.name = name; this.x = x; this.y = y;
        }
    }
}