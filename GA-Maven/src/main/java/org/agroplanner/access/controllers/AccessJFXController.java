package org.agroplanner.access.controllers;

import javafx.animation.PauseTransition;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.util.Duration;
import org.agroplanner.access.model.CredentialsDTO;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.shared.exceptions.DuplicateUserException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.function.Consumer;

/**
 * JavaFX Controller for the Access Subsystem.
 * <p>
 * This controller acts as the <strong>Code-Behind</strong> for the Access View (FXML).
 * It handles UI events, delegates business logic to the {@link AccessService},
 * and communicates successful authentication back to the Orchestrator via callbacks.
 * </p>
 */
public class AccessJFXController {

    // --- DEPENDENCIES ---
    /** Business logic provider (Logic Controller). */
    private AccessService service;

    /** Callback to notify the Orchestrator when a user logs in successfully. */
    private Consumer<User> onAuthenticationSuccess;

    // --- FXML INJECTIONS (Login Tab) ---
    @FXML private TextField loginUsernameField;
    @FXML private PasswordField loginPasswordField;
    @FXML private Label loginStatusLabel;
    @FXML private Button loginButton;

    // --- FXML INJECTIONS (Registration Tab) ---
    @FXML private TextField regFirstNameField;
    @FXML private TextField regLastNameField;
    @FXML private TextField regEmailField;
    @FXML private TextField regPhoneField;
    @FXML private TextField regUsernameField;
    @FXML private PasswordField regPasswordField;
    @FXML private ComboBox<Role> regRoleCombo;
    @FXML private Label regStatusLabel;
    @FXML private Button regButton;

    /**
     * Dependency Injection method called by the Orchestrator immediately after loading the FXML.
     * <p>
     * Since JavaFX controllers are instantiated by the {@code FXMLLoader}, we cannot use standard
     * constructor injection.
     * </p>
     *
     * @param service   The initialized business logic service.
     * @param onSuccess The callback to execute upon successful login.
     */
    public void init(AccessService service, Consumer<User> onSuccess) {
        this.service = service;
        this.onAuthenticationSuccess = onSuccess;

        // Populate Role Combo Box
        regRoleCombo.getItems().setAll(Role.USER, Role.AGRONOMIST);
        regRoleCombo.getSelectionModel().selectFirst();
    }

    // ==========================================
    // LOGIN LOGIC
    // ==========================================

    /**
     * Handles the "Sign In" button click.
     * Captures input, calls the service, and provides visual feedback.
     */
    @FXML
    private void handleLoginAction() {
        loginStatusLabel.setText("");
        String u = loginUsernameField.getText();
        String p = loginPasswordField.getText();

        try {
            User user = service.login(u, p);

            if (user != null) {
                // UI Feedback: Success
                loginStatusLabel.setStyle("-fx-text-fill: green;");
                loginStatusLabel.setText("Welcome back, " + user.getFirstName() + "!");
                loginButton.setDisable(true);

                PauseTransition delay = new PauseTransition(Duration.seconds(0.7));
                delay.setOnFinished(event -> {
                    if (onAuthenticationSuccess != null) {
                        onAuthenticationSuccess.accept(user); // Notify Orchestrator
                    }
                });
                delay.play();

            } else {
                // UI Feedback: Failure
                loginStatusLabel.setStyle("-fx-text-fill: red;");
                loginStatusLabel.setText("Invalid credentials.");
            }
        } catch (Exception e) {
            loginStatusLabel.setText("System Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // ==========================================
    // REGISTRATION LOGIC
    // ==========================================

    /**
     * Handles the "Register" button click.
     * Collects DTO data and determines the registration workflow based on the Role.
     */
    @FXML
    private void handleRegisterAction() {
        regStatusLabel.setText("");

        // Construct DTO from UI fields
        CredentialsDTO dto = new CredentialsDTO(
                regUsernameField.getText(),
                regPasswordField.getText(),
                regFirstNameField.getText(),
                regLastNameField.getText(),
                regEmailField.getText(),
                regPhoneField.getText(),
                regRoleCombo.getValue()
        );

        if (dto.getRequestedRole() == Role.AGRONOMIST) {
            // Agronomists require a simulated external validation (Async)
            runAgronomistSimulation(dto);
        } else {
            // Standard users are registered synchronously
            performRegistration(dto);
        }
    }

    /**
     * Executes the actual persistence call via the Service.
     *
     * @param dto The data to persist.
     */
    private void performRegistration(CredentialsDTO dto) {
        try {
            service.register(dto);
            showInfoAlert("Registration Successful", "Account created! You may now sign in.");
            clearRegistrationFields();
        } catch (InvalidInputException | DuplicateUserException e) {
            // Domain errors (Validation/Duplication)
            regStatusLabel.setStyle("-fx-text-fill: red;");
            regStatusLabel.setText(e.getMessage());
        } catch (Exception e) {
            // Unexpected technical errors
            regStatusLabel.setStyle("-fx-text-fill: red;");
            regStatusLabel.setText("Database Error: " + e.getMessage());
        }
    }

    /**
     * Simulates an asynchronous check with a National Register for Agronomists.
     * <p>
     * Uses a JavaFX {@link Task} to perform the "waiting" on a background thread
     * so the UI remains responsive (loading animation).
     * </p>
     *
     * @param dto The user data waiting for approval.
     */
    private void runAgronomistSimulation(CredentialsDTO dto) {
        regStatusLabel.setStyle("-fx-text-fill: orange;");
        regStatusLabel.setText("Contacting National Register...");
        regButton.setDisable(true); // Disable button during process

        Task<Void> simulationTask = new Task<>() {
            @Override
            protected Void call() throws Exception {
                // Simulate network latency (BACKGROUND THREAD)
                for (int i = 0; i < 5; i++) {
                    Thread.sleep(500);
                    updateMessage("Verifying License" + ".".repeat(i));
                }
                return null;
            }
        };

        // Update label text
        regStatusLabel.textProperty().bind(simulationTask.messageProperty());

        simulationTask.setOnSucceeded(e -> {
            regStatusLabel.textProperty().unbind(); // Unbind before setting text manually
            regStatusLabel.setStyle("-fx-text-fill: green;");
            regStatusLabel.setText("License Verified. Proceeding...");

            // Proceed to actual registration
            performRegistration(dto);
            regButton.setDisable(false);
        });

        simulationTask.setOnFailed(e -> {
            regStatusLabel.textProperty().unbind();
            regStatusLabel.setStyle("-fx-text-fill: red;");
            regStatusLabel.setText("Connection to Register failed.");
            regButton.setDisable(false);
        });

        // Start the background thread
        new Thread(simulationTask).start();
    }

    // --- UTILITY METHODS ---

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