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
 * Controller JavaFX per le operazioni di persistenza (Save & Load).
 */
public class PersistenceJFXController {

    // --- DEPENDENCIES ---
    private SolutionDAOContract dao;
    private User currentUser;
    private Consumer<LoadedSession> onLoadSuccess; // Callback per restituire i dati caricati
    private Stage stage; // Riferimento alla finestra per poterla chiudere

    // --- FXML FIELDS (Load View) ---
    @FXML private TableView<SolutionMetadata> solutionsTable;
    @FXML private TableColumn<SolutionMetadata, String> colTitle;
    @FXML private TableColumn<SolutionMetadata, String> colDate;
    @FXML private TableColumn<SolutionMetadata, String> colScore;
    @FXML private Label statusLabel;
    @FXML private Button loadButton;

    /**
     * Inizializzazione per la Finestra di CARICAMENTO.
     */
    public void initLoad(Stage stage, SolutionDAOContract dao, User user, Consumer<LoadedSession> onSuccess) {
        this.stage = stage;
        this.dao = dao;
        this.currentUser = user;
        this.onLoadSuccess = onSuccess;

        setupTable();
        loadData();
    }

    // ==========================================
    // LOGICA DI CARICAMENTO (LOAD)
    // ==========================================

    private void setupTable() {
        colTitle.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getTitle()));

        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        colDate.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getCreationDate().format(dtf)));

        colScore.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.4f", cell.getValue().getFitness())));

        // Abilita bottone solo se riga selezionata
        solutionsTable.getSelectionModel().selectedItemProperty().addListener((obs, oldVal, newVal) ->
                loadButton.setDisable(newVal == null)
        );
    }

    private void loadData() {
        try {
            List<SolutionMetadata> data = dao.findByUser(currentUser);
            if (data.isEmpty()) {
                statusLabel.setText("Nessuna soluzione salvata trovata.");
            } else {
                solutionsTable.setItems(FXCollections.observableArrayList(data));
            }
        } catch (Exception e) {
            statusLabel.setText("Errore DB: " + e.getMessage());
        }
    }

    @FXML
    private void handleLoadConfirm() {
        SolutionMetadata selected = solutionsTable.getSelectionModel().getSelectedItem();
        if (selected == null) return;

        try {
            statusLabel.setText("Caricamento in corso...");
            Optional<LoadedSession> sessionOpt = dao.loadSolution(selected.getId());

            if (sessionOpt.isPresent()) {
                if (onLoadSuccess != null) {
                    onLoadSuccess.accept(sessionOpt.get());
                }
                stage.close(); // Chiudi finestra
            } else {
                statusLabel.setText("Errore: Soluzione non trovata o corrotta.");
            }
        } catch (DataPersistenceException e) {
            statusLabel.setText("Errore Caricamento: " + e.getMessage());
        }
    }

    @FXML
    private void handleCancel() {
        stage.close();
    }


    // ==========================================
    // LOGICA DI SALVATAGGIO (SAVE)
    // Metodo helper statico o istanza separata, non serve FXML qui.
    // ==========================================

    /**
     * Esegue il workflow di salvataggio "al volo" tramite Dialogs.
     * Replica `runSaveSession` del Controller CLI.
     */
    public static void runSaveWorkflow(SolutionDAOContract dao, Individual solution, User user, boolean isDemoMode, DomainDefinition domainDef, Runnable onSuccess) {

        // 1. Check Demo Mode
        if (isDemoMode) {
            showInfoAlert("Salvataggio Disabilitato", "Sei in modalità Guest. Effettua il login per salvare.");
            return;
        }

        // 2. Chiedi Titolo
        TextInputDialog dialog = new TextInputDialog("My Solution");
        dialog.setTitle("Salva Soluzione");
        dialog.setHeaderText("Salva la soluzione nel database");
        dialog.setContentText("Inserisci un titolo:");

        Optional<String> result = dialog.showAndWait();

        result.ifPresent(title -> {
            if (title.trim().isEmpty()) {
                showErrorAlert("Errore", "Il titolo non può essere vuoto.");
                return;
            }

            try {
                boolean success = dao.saveSolution(solution, user, title.trim(), domainDef);
                if (success) {
                    showInfoAlert("Successo", "Soluzione salvata correttamente nel database!");

                    // --- PUNTO CHIAVE: ESEGUI LA CALLBACK ---
                    if (onSuccess != null) {
                        onSuccess.run();
                    }

                } else {
                    showErrorAlert("Errore", "Il database ha rifiutato l'operazione.");
                }
            } catch (DataPersistenceException e) {
                showErrorAlert("Errore di Sistema", e.getMessage());
            }
        });
    }

    // --- UTILS ---

    private static void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }

    private static void showErrorAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
