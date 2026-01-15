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
import javafx.scene.control.ProgressIndicator;
import org.agroplanner.persistence.controllers.PersistenceJFXController;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;

import java.awt.geom.Rectangle2D;
import java.util.function.Consumer;

/**
 * JavaFX Controller for the Evolutionary Simulation.
 * <p>
 * Handles the running of the Genetic Algorithm (via background Tasks),
 * real-time visualization on Canvas, and persistence operations.
 * </p>
 */
public class EvolutionJFXController {

    // --- DEPENDENCIES ---
    private EvolutionService service;
    private Domain domain;
    private DomainDefinition domainDefinition;
    private AgroPersistenceFactory factory;
    private User currentUser;

    // Callbacks
    private Consumer<Individual> onEvolutionComplete;
    private Runnable onExitCallback;

    // --- STATE ---
    private Individual bestSolution;
    private boolean isDemoMode;
    private static final int MAX_ATTEMPTS = 3;

    // --- FXML INJECTIONS ---
    @FXML private TextArea consoleArea;
    @FXML private ProgressBar progressBar;
    @FXML private Label fitnessLabel;

    // Buttons
    @FXML private Button startButton;
    @FXML private Button saveButton;
    @FXML private Button exportButton;

    // Detail Table
    @FXML private TableView<PointViewModel> pointsTable;
    @FXML private TableColumn<PointViewModel, String> colPtName;
    @FXML private TableColumn<PointViewModel, Number> colPtX;
    @FXML private TableColumn<PointViewModel, Number> colPtY;

    // Visualization Area
    @FXML private Label placeholderLabel;
    @FXML private StackPane canvasContainer;
    @FXML private Canvas plantCanvas;

    /**
     * Initializes the controller. This method handles two scenarios:
     * 1. New Simulation: Sets up the UI for running the algorithm.
     * 2. Loaded Session: Sets up the UI for viewing an existing solution (Read-Only).
     */
    public void init(EvolutionService service, Domain domain, DomainDefinition domainDef,
                     Consumer<Individual> onExportRequested, AgroPersistenceFactory factory,
                     User user, boolean isDemoMode, Individual loadedSolution, Runnable onExit) {

        this.service = service;
        this.domain = domain;
        this.domainDefinition = domainDef;
        this.onEvolutionComplete = onExportRequested;
        this.factory = factory;
        this.currentUser = user;
        this.isDemoMode = isDemoMode;
        this.onExitCallback = onExit;

        // Configure Table Columns
        setupTable();

        // Bind Canvas size to Container size for responsiveness
        plantCanvas.widthProperty().bind(canvasContainer.widthProperty());
        plantCanvas.heightProperty().bind(canvasContainer.heightProperty());

        // Redraw on resize
        canvasContainer.widthProperty().addListener(o -> redrawCurrentSolution());
        canvasContainer.heightProperty().addListener(o -> redrawCurrentSolution());

        // DUAL MODE LOGIC
        if (loadedSolution != null) {
            // --- VIEW MODE (Loaded from DB) ---
            this.bestSolution = loadedSolution;
            updateUIWithSolution(bestSolution);

            // Hide simulation controls
            startButton.setVisible(false);
            progressBar.setVisible(false);
            saveButton.setVisible(false);

            // Enable Export immediately
            exportButton.setDisable(false);

        } else {
            // --- CALCULATION MODE (New Session) ---
            startButton.setVisible(true);
            saveButton.setVisible(true);
            saveButton.setDisable(true); // Disabled until run
            exportButton.setDisable(true);
            placeholderLabel.setVisible(true);
        }
    }

    private void setupTable() {
        colPtName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().name));
        colPtX.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().x));
        colPtY.setCellValueFactory(cell -> new SimpleDoubleProperty(cell.getValue().y));

        // Format numbers to 2 decimals
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
     * Updates the entire UI (Graph + Table + Labels) with a given solution.
     */
    private void updateUIWithSolution(Individual solution) {
        // 1. Draw Canvas
        drawSimplePoints(solution);

        // 2. Update Labels
        placeholderLabel.setVisible(false);
        fitnessLabel.setText(String.format("%.6f", solution.getFitness()));

        // 3. Populate Table
        ObservableList<PointViewModel> list = FXCollections.observableArrayList();
        for (Point p : solution.getChromosomes()) {
            list.add(new PointViewModel(p.getVarietyName(), p.getX(), p.getY()));
        }
        pointsTable.setItems(list);
    }


    @FXML
    private void handleRunEvolution() {
        // Lock UI
        startButton.setDisable(true);
        saveButton.setDisable(true);
        exportButton.setDisable(true);
        progressBar.setProgress(ProgressIndicator.INDETERMINATE_PROGRESS);
        consoleArea.clear();

        // Start Background Task
        EvolutionTask task = new EvolutionTask();

        // Redirect task messages to Console TextArea
        task.messageProperty().addListener((obs, oldMsg, newMsg) ->
            consoleArea.appendText(newMsg + "\n")
        );

        task.setOnSucceeded(e -> {
            this.bestSolution = task.getValue();
            progressBar.setProgress(1.0);

            startButton.setDisable(false);
            startButton.setText("Restart");

            // Update UI
            updateUIWithSolution(bestSolution);
            consoleArea.appendText("✅ Simulation completed successfully.\n");

            // Enable post-processing actions
            saveButton.setDisable(false);
            exportButton.setDisable(true);

        });

        task.setOnFailed(e -> {
            progressBar.setProgress(0);
            startButton.setDisable(false);
            consoleArea.appendText("⛔ ERROR: " + task.getException().getMessage() + "\n");

            Alert alert = new Alert(Alert.AlertType.ERROR, "Simulation Failed: " + task.getException().getMessage());
            alert.showAndWait();
        });

        new Thread(task).start();
    }

    @FXML
    private void handleSaveAction() {
        if (bestSolution == null) return;

        // Use the static utility from Persistence Controller
        PersistenceJFXController.runSaveWorkflow(
                factory.getSolutionDAO(),
                bestSolution,
                currentUser,
                isDemoMode,
                this.domainDefinition,
                () -> {
                    // UI Callback on success
                    saveButton.setDisable(true);
                    saveButton.setText("Saved ✅");
                    exportButton.setDisable(false);
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
    // ASYNC TASK
    // =========================================================

    private class EvolutionTask extends Task<Individual> {
        @Override
        protected Individual call() throws Exception {
            updateMessage("🧬 Initializing Genetic Algorithm...");

            int currentAttempt = 0;
            Individual lastResult = null;

            do {
                currentAttempt++;
                long start = System.currentTimeMillis();
                updateMessage(String.format("🔄 Attempt %d of %d running...", currentAttempt, MAX_ATTEMPTS));

                // BLOCKING CALL to Service
                lastResult = service.executeEvolutionCycle();
                long duration = System.currentTimeMillis() - start;

                if (service.isValidSolution(lastResult)) {
                    updateMessage(String.format("✅ Valid solution found in %.2fs", duration / 1000.0));
                    return lastResult;
                }

                updateMessage(String.format("⚠️ Converged on invalid solution (Score: %.4f). Retrying...", lastResult.getFitness()));

            } while (currentAttempt < MAX_ATTEMPTS);

            throw new MaxAttemptsExceededException("Could not find a valid solution after " + MAX_ATTEMPTS + " attempts.");
        }
    }

    // =========================================================
    // GRAPHICS
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
        double domainW = bounds.getWidth();
        double domainH = bounds.getHeight();

        // Calculate Scale to fit domain in canvas with margin
        double margin = 30;
        double availW = w - (margin * 2);
        double availH = h - (margin * 2);
        double scale = Math.min(availW / domainW, availH / domainH);

        // Calculate Offset to center the drawing
        double offsetX = margin + (availW - (domainW * scale)) / 2.0;
        double offsetY = margin + (availH - (domainH * scale)) / 2.0;

        // Draw Domain Boundary
        gc.setStroke(Color.web("#e0e0e0"));
        gc.setLineWidth(1.0);
        gc.strokeRect(offsetX, offsetY, domainW * scale, domainH * scale);

        // Draw Points
        double dotRadius = 3.0;
        for (Point p : solution.getChromosomes()) {
            // Normalize point coordinates relative to domain origin
            double normalizedX = p.getX() - bounds.getX();
            double normalizedY = p.getY() - bounds.getY();

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
    // INNER CLASS FOR TABLE VIEW MODEL
    // =========================================================
    public static class PointViewModel {
        String name;
        double x;
        double y;
        public PointViewModel(String name, double x, double y) {
            this.name = name; this.x = x; this.y = y;
        }
        public String getName() { return name; }
        public double getX() { return x; }
        public double getY() { return y; }
    }
}