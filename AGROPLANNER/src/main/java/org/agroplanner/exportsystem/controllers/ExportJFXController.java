package org.agroplanner.exportsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.exportsystem.model.ExportType;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.shared.exceptions.ExportException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.Optional;

/**
 * JavaFX Controller for the Export Wizard.
 * <p>
 * Handles user interaction for saving simulation results to external files.
 * Includes security checks for Guest users and file overwrite protection.
 * </p>
 */
public class ExportJFXController {

    // --- DEPENDENCIES ---
    private ExportService service;
    private Individual solution;
    private Domain domain;
    private PlantInventory inventory;
    private Runnable onCloseCallback;

    // --- UI STATE ---
    private ToggleGroup formatToggleGroup;

    // --- FXML INJECTIONS ---
    @FXML private VBox formatContainer;
    @FXML private TextField filenameField;
    @FXML private TextField resultPathField; // New field to show full path
    @FXML private Label feedbackLabel;
    @FXML private Button exportButton;
    @FXML private VBox guestLockPanel;

    /**
     * Initializes the controller with context data.
     */
    public void init(ExportService service, Individual solution, Domain domain, PlantInventory inventory, boolean isDemoMode, Runnable onClose) {
        this.service = service;
        this.solution = solution;
        this.domain = domain;
        this.inventory = inventory;
        this.onCloseCallback = onClose;

        setupFormats();

        // Security Gate for Demo Mode
        if (isDemoMode) {
            disableInterfaceForGuest();
        }
    }

    private void setupFormats() {
        formatToggleGroup = new ToggleGroup();

        for (ExportType type : service.getAvailableExportTypes()) {
            RadioButton rb = new RadioButton(type.toString() + " (" + type.getExtension() + ")");
            rb.setUserData(type); // Store enum directly
            rb.setToggleGroup(formatToggleGroup);
            rb.setStyle("-fx-padding: 5;");
            rb.setTooltip(new Tooltip(type.getExportInfo()));

            formatContainer.getChildren().add(rb);
        }

        // Select first by default
        if (!formatToggleGroup.getToggles().isEmpty()) {
            formatToggleGroup.getToggles().get(0).setSelected(true);
        }
    }

    private void disableInterfaceForGuest() {
        exportButton.setDisable(true);
        filenameField.setDisable(true);
        formatContainer.setDisable(true);
        feedbackLabel.setVisible(false);
        guestLockPanel.setVisible(true);
    }

    @FXML
    private void handleExportAction() {
        feedbackLabel.setText("");
        resultPathField.clear();
        resultPathField.setVisible(false);

        // 1. Validate Input
        String filename = filenameField.getText().trim();
        Toggle selectedToggle = formatToggleGroup.getSelectedToggle();

        if (selectedToggle == null) {
            showError("Please select a format.");
            return;
        }

        if (filename.isEmpty()) {
            showError("Filename is required.");
            return;
        }

        ExportType selectedType = (ExportType) selectedToggle.getUserData();

        // 2. Overwrite Protection
        if (service.checkFileExists(selectedType, filename)) {
            boolean overwrite = showOverwriteDialog(filename + selectedType.getExtension());
            if (!overwrite) return; // User cancelled
        }

        // 3. Execute Export
        try {
            String savedPath = service.performExport(solution, domain, inventory, selectedType, filename);

            // Success UI Update
            feedbackLabel.setStyle("-fx-text-fill: green;");
            feedbackLabel.setText("Export Successful!");

            // Show the full path in a copyable field
            resultPathField.setVisible(true);
            resultPathField.setText(savedPath);

            // Clear input to prevent accidental double-save
            filenameField.clear();

        } catch (InvalidInputException | ExportException e) {
            showError("Error: " + e.getMessage());
        } catch (Exception e) {
            showError("Critical Error: " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseAction() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    private void showError(String msg) {
        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;");
        feedbackLabel.setText(msg);
    }

    private boolean showOverwriteDialog(String fullFilename) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("File Conflict");
        alert.setHeaderText("File '" + fullFilename + "' already exists.");
        alert.setContentText("Do you want to overwrite it?");

        ButtonType buttonTypeOverwrite = new ButtonType("Overwrite");
        ButtonType buttonTypeCancel = new ButtonType("Cancel", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOverwrite, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOverwrite;
    }
}