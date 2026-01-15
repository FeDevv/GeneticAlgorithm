package org.agroplanner.persistence.controllers;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.SolutionMetadata;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;

/**
 * JavaFX Controller for Persistence Operations (Save & Load).
 * <p>
 * Handles the "Load Solution" window (TableView) via standard FXML binding,
 * and provides a static utility for the "Save Solution" workflow via lightweight Dialogs.
 * </p>
 */
public class PersistenceJFXController {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    // --- DEPENDENCIES ---
    private SolutionDAOContract dao;
    private User currentUser;
    private Consumer<LoadedSession> onLoadSuccess; // Callback to return loaded data
    private Stage stage;

    private static final String RedColorString = "-fx-text-fill: red;";
    private static final String BlackColorString = "-fx-text-fill: black;";

    // --- FXML INJECTIONS (Load View) ---
    @FXML private TableView<SolutionMetadata> solutionsTable;
    @FXML private TableColumn<SolutionMetadata, String> titleColumn;
    @FXML private TableColumn<SolutionMetadata, String> dateColumn;
    @FXML private TableColumn<SolutionMetadata, String> scoreColumn;
    @FXML private Label statusLabel;
    @FXML private Button loadButton;

    /**
     * Initializes the Load Window state and injects dependencies.
     *
     * @param stage     The generic stage hosting this scene.
     * @param dao       The data access object for retrieving solutions.
     * @param user      The currently logged-in user.
     * @param onSuccess Callback to execute when a session is successfully loaded.
     */
    public void initLoad(Stage stage, SolutionDAOContract dao, User user, Consumer<LoadedSession> onSuccess) {
        this.stage = stage;
        this.dao = dao;
        this.currentUser = user;
        this.onLoadSuccess = onSuccess;

        configureTableColumns();
        fetchAndDisplayData();
    }

    // ==========================================
    // LOADING LOGIC (Instance Methods)
    // ==========================================

    private void configureTableColumns() {
        titleColumn.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));

        dateColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getCreationDate().format(DATE_FORMATTER)));

        scoreColumn.setCellValueFactory(cell ->
                new SimpleStringProperty(String.format("%.4f", cell.getValue().getFitness())));

        // Enable "Load" button only when a row is selected
        solutionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                loadButton.setDisable(newVal == null)
        );
    }

    private void fetchAndDisplayData() {
        try {
            List<SolutionMetadata> data = dao.findByUser(currentUser);
            if (data.isEmpty()) {
                statusLabel.setText("No saved solutions found.");
            } else {
                solutionsTable.setItems(FXCollections.observableArrayList(data));
            }
        } catch (Exception e) {
            statusLabel.setStyle(RedColorString);
            statusLabel.setText("Database Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoadConfirm() {
        SolutionMetadata selected = solutionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            statusLabel.setStyle(BlackColorString);
            statusLabel.setText("Loading data...");

            Optional<LoadedSession> sessionOpt = dao.loadSolution(selected.getId());

            if (sessionOpt.isPresent()) {
                if (onLoadSuccess != null) {
                    onLoadSuccess.accept(sessionOpt.get());
                }
                stage.close();
            } else {
                statusLabel.setStyle(RedColorString);
                statusLabel.setText("Error: Solution not found or corrupted.");
            }
        } catch (DataPersistenceException e) {
            statusLabel.setStyle(RedColorString);
            statusLabel.setText("Load Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }


    // ==========================================
    // SAVING LOGIC (Static Utility)
    // ==========================================

    /**
     * Executes the "Save Solution" workflow using JavaFX Dialogs.
     * <p>
     * Note: This is a static utility method to avoid creating a full FXML view
     * just for a simple title input dialog.
     * </p>
     *
     * @param dao        The DAO contract.
     * @param solution   The individual/solution to save.
     * @param user       The owner of the solution.
     * @param isDemoMode Flag to check restricted access.
     * @param domainDef  The domain context.
     * @param onSuccess  Runnable to execute after successful save.
     */
    public static void runSaveWorkflow(SolutionDAOContract dao, Individual solution, User user, boolean isDemoMode, DomainDefinition domainDef, Runnable onSuccess) {

        // 1. Security Check
        if (isDemoMode) {
            showAlert(Alert.AlertType.INFORMATION, "Save Disabled", "Guest Mode restriction. Please login to save.");
            return;
        }

        // 2. Input Dialog
        TextInputDialog dialog = new TextInputDialog("My Solution");
        dialog.setTitle("Save Solution");
        dialog.setHeaderText("Persist current session");
        dialog.setContentText("Enter a unique title:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            if (title.trim().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Invalid Input", "Title cannot be empty.");
                return;
            }

            try {
                boolean success = dao.saveSolution(solution, user, title.trim(), domainDef);
                if (success) {
                    showAlert(Alert.AlertType.INFORMATION, "Success", "Solution saved successfully!");
                    if (onSuccess != null) onSuccess.run();
                } else {
                    showAlert(Alert.AlertType.ERROR, "Failure", "Database rejected the operation.");
                }
            } catch (DataPersistenceException e) {
                showAlert(Alert.AlertType.ERROR, "System Error", e.getMessage());
            }
        });
    }

    // --- HELPER METHODS ---

    private static void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}