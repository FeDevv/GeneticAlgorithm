package org.agroplanner.access.controllers;

import javafx.application.Platform;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import org.agroplanner.access.model.CredentialsDTO;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.shared.exceptions.DuplicateUserException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.function.Consumer;

/**
 * Controller JavaFX per il sottosistema Access.
 * Gestisce sia il Login che la Registrazione all'interno della stessa scena (tramite Tabs).
 */
public class AccessJFXController {

    // --- DEPENDENCIES ---
    private AccessService service;
    private Consumer<User> onAuthenticationSuccess; // Callback verso l'Orchestrator

    // --- FXML FIELDS (Login) ---
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginStatusLabel;
    @FXML private Button loginButton;

    // --- FXML FIELDS (Registration) ---
    @FXML private TextField regFirstNameField;
    @FXML private TextField regLastNameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regPhoneField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private ComboBox<Role> regRoleCombo;
    @FXML private Label regStatusLabel;

    /**
     * Metodo di inizializzazione chiamato dall'Orchestrator JFX
     * per iniettare le dipendenze necessarie.
     */
    public void init(AccessService service, Consumer<User> onSuccess) {
        this.service = service;
        this.onAuthenticationSuccess = onSuccess;

        // Inizializza la combo box dei ruoli
        regRoleCombo.getItems().setAll(Role.USER, Role.AGRONOMIST);
        regRoleCombo.getSelectionModel().selectFirst();
    }

    // ==========================================
    // LOGIN LOGIC
    // ==========================================

    @FXML
    private void handleLoginAction() {
        loginStatusLabel.setText("");
        String u = loginUsernameField.getText();
        String p = loginPasswordField.getText();

        try {
            // Chiamata diretta al Service (come nel controller CLI)
            User user = service.login(u, p);

            if (user != null) {
                loginStatusLabel.setStyle("-fx-text-fill: green;");
                loginStatusLabel.setText("Benvenuto " + user.getFirstName() + "!");

                // Disabilita controlli per evitare doppi click
                loginButton.setDisable(true);

                // Notifica l'Orchestrator (ritarda leggermente per far vedere il messaggio)
                Platform.runLater(() -> {
                    try { Thread.sleep(500); } catch (Exception ignored){}
                    onAuthenticationSuccess.accept(user);
                });
            } else {
                loginStatusLabel.setStyle("-fx-text-fill: red;");
                loginStatusLabel.setText("Credenziali non valide.");
            }
        } catch (Exception e) {
            loginStatusLabel.setText("Errore di sistema: " + e.getMessage());
        }
    }

    // ==========================================
    // REGISTRATION LOGIC
    // ==========================================

    @FXML
    private void handleRegisterAction() {
        regStatusLabel.setText("");

        // Costruzione DTO dai campi
        CredentialsDTO dto = new CredentialsDTO(
                regUsernameField.getText(),
                regPasswordField.getText(),
                regFirstNameField.getText(),
                regLastNameField.getText(),
                regEmailField.getText(),
                regPhoneField.getText(),
                regRoleCombo.getValue()
        );

        if (dto.requestedRole == Role.AGRONOMIST) {
            // Se Agronomo, avvia la simulazione asincrona
            runAgronomistSimulation(dto);
        } else {
            // Registrazione standard immediata
            performRegistration(dto);
        }
    }

    private void performRegistration(CredentialsDTO dto) {
        try {
            service.register(dto);
            showInfoAlert("Successo", "Registrazione completata! Ora puoi effettuare il login.");
            clearRegistrationFields();
        } catch (InvalidInputException | DuplicateUserException e) {
            regStatusLabel.setStyle("-fx-text-fill: red;");
            regStatusLabel.setText(e.getMessage());
        } catch (Exception e) {
            regStatusLabel.setText("Errore DB: " + e.getMessage());
        }
    }

    /**
     * Replica la logica "showAgronomistValidationSequence" della CLI,
     * ma usando un Task JavaFX per non bloccare l'interfaccia grafica.
     */
    private void runAgronomistSimulation(CredentialsDTO dto) {
        regStatusLabel.setStyle("-fx-text-fill: orange;");
        regStatusLabel.setText("Contattando il Registro Nazionale...");

        Task<Void> simulationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simula attesa di rete (come nel CLI)
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(500);
                    updateMessage("Verifica in corso" + ".".repeat(i));
                }
                return null;
            }
        };

        simulationTask.setOnSucceeded(e -> {
            regStatusLabel.setStyle("-fx-text-fill: green;");
            regStatusLabel.setText("Licenza Verificata. Procedo.");
            performRegistration(dto);
        });

        simulationTask.setOnFailed(e -> {
            regStatusLabel.setText("Errore di connessione al registro.");
        });

        // Avvia il task su un thread separato
        new Thread(simulationTask).start();
    }

    // --- UTILS ---

    private void clearRegistrationFields() {
        regUsernameField.clear();
        regPasswordField.clear();
        regFirstNameField.clear();
        regLastNameField.clear();
        regEmailField.clear();
        regPhoneField.clear();
    }

    private void showInfoAlert(String title, String content) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}
