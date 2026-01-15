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
 * Controller JavaFX per il Wizard di Esportazione.
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

    // --- FXML FIELDS ---
    @FXML private VBox formatContainer;
    @FXML private TextField filenameField;
    @FXML private Label feedbackLabel;
    @FXML private Button exportButton;
    @FXML private VBox guestLockPanel;

    /**
     * Inizializzazione chiamata dall'Orchestrator.
     */
    public void init(ExportService service, Individual solution, Domain domain, PlantInventory inventory, boolean isDemoMode, Runnable onClose) {
        this.service = service;
        this.solution = solution;
        this.domain = domain;
        this.inventory = inventory;
        this.onCloseCallback = onClose;

        setupFormats();

        // Security Gate per Demo Mode
        if (isDemoMode) {
            disableInterfaceForGuest();
        }
    }

    private void setupFormats() {
        formatToggleGroup = new ToggleGroup();

        for (ExportType type : service.getAvailableExportTypes()) {
            RadioButton rb = new RadioButton(type.toString() + " (" + type.getExtension() + ")");
            rb.setUserData(type); // Salviamo l'enum nel bottone
            rb.setToggleGroup(formatToggleGroup);
            rb.setStyle("-fx-padding: 5;");

            // Tooltip con descrizione
            rb.setTooltip(new Tooltip(type.getExportInfo()));

            formatContainer.getChildren().add(rb);
        }

        // Seleziona il primo di default
        if (!formatToggleGroup.getToggles().isEmpty()) {
            formatToggleGroup.getToggles().get(0).setSelected(true);
        }
    }

    private void disableInterfaceForGuest() {
        // Overlay di blocco o disabilitazione controlli
        exportButton.setDisable(true);
        filenameField.setDisable(true);
        formatContainer.setDisable(true);

        feedbackLabel.setVisible(false);
        guestLockPanel.setVisible(true); // Mostra il messaggio di blocco
        // Spostiamo il pannello sopra (StackPane sarebbe meglio nel FXML, ma VBox va bene se gestito così)
        // Nota: Nel FXML ho messo guestLockPanel in fondo, qui lo rendo visibile.
        // Idealmente si userebbe uno StackPane per coprire tutto, ma seguiamo la semplicità richiesta.
    }

    @FXML
    private void handleExportAction() {
        feedbackLabel.setText("");
        feedbackLabel.setStyle("-fx-text-fill: #d32f2f;"); // Reset rosso

        // 1. Validazione Input
        String filename = filenameField.getText().trim();
        Toggle selectedToggle = formatToggleGroup.getSelectedToggle();

        if (selectedToggle == null) {
            feedbackLabel.setText("Seleziona un formato.");
            return;
        }

        if (filename.isEmpty()) {
            feedbackLabel.setText("Inserisci un nome per il file.");
            return;
        }

        ExportType selectedType = (ExportType) selectedToggle.getUserData();

        // 2. Controllo Esistenza File (Overwrite Protection)
        if (service.checkFileExists(selectedType, filename)) {
            boolean overwrite = showOverwriteDialog(filename + selectedType.getExtension());
            if (!overwrite) {
                return; // L'utente ha annullato
            }
        }

        // 3. Esecuzione Export
        try {
            String savedPath = service.performExport(solution, domain, inventory, selectedType, filename);

            // Successo
            feedbackLabel.setStyle("-fx-text-fill: green;");
            feedbackLabel.setText("Salvato con successo in:\n" + savedPath);

            // Pulisci campo per evitare doppi salvataggi accidentali
            filenameField.clear();

        } catch (InvalidInputException | ExportException e) {
            feedbackLabel.setText("Errore: " + e.getMessage());
        } catch (Exception e) {
            feedbackLabel.setText("Errore Critico: " + e.getMessage());
        }
    }

    @FXML
    private void handleCloseAction() {
        if (onCloseCallback != null) {
            onCloseCallback.run();
        }
    }

    /**
     * Dialogo di conferma sovrascrittura (replica askOverwriteOrRename della CLI View).
     */
    private boolean showOverwriteDialog(String fullFilename) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Conflitto File");
        alert.setHeaderText("Il file '" + fullFilename + "' esiste già.");
        alert.setContentText("Vuoi sovrascriverlo?");

        ButtonType buttonTypeOverwrite = new ButtonType("Sovrascrivi");
        ButtonType buttonTypeCancel = new ButtonType("Annulla", ButtonBar.ButtonData.CANCEL_CLOSE);

        alert.getButtonTypes().setAll(buttonTypeOverwrite, buttonTypeCancel);

        Optional<ButtonType> result = alert.showAndWait();
        return result.isPresent() && result.get() == buttonTypeOverwrite;
    }
}
