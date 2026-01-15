package org.agroplanner.orchestrator.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;

public class DashboardJFXController {

    private JFXOrchestrator orchestrator;

    @FXML private Label userLabel;
    @FXML private Label roleLabel;
    @FXML private Button catalogButton;

    public void init(JFXOrchestrator orchestrator, User user) {
        this.orchestrator = orchestrator;

        userLabel.setText(user.getFullName());
        roleLabel.setText(user.getRole().getLabel());

        // Nascondi gestione catalogo se non agronomo
        if (user.getRole() != Role.AGRONOMIST) {
            catalogButton.setVisible(false);
            catalogButton.setManaged(false); // Rimuove lo spazio occupato
        }
    }

    @FXML private void handleNewSession() { orchestrator.startOptimizationFlow(); }
    @FXML private void handleLoadSession() { orchestrator.startLoadFlow(); }
    @FXML private void handleCatalog() { orchestrator.startCatalogFlow(); }
    @FXML private void handleLogout() { orchestrator.logout(); }
}