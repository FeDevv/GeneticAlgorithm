package org.agroplanner.domainsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.VBox;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * JavaFX Controller for Geometric Domain Definition.
 * <p>
 * Implements a <strong>Metadata-Driven UI</strong> where input fields are generated
 * dynamically based on the selected {@link DomainType}, ensuring scalability for new shapes.
 * </p>
 */
public class DomainJFXController {

    // --- DEPENDENCIES ---
    private DomainService service;
    private Consumer<DomainDefinition> onDomainCreated; // Callback to Orchestrator

    // --- FXML INJECTIONS ---
    @FXML private ComboBox<DomainType> domainTypeCombo;
    @FXML private VBox dynamicFormContainer;
    @FXML private Label feedbackLabel;
    @FXML private Button createButton;

    // --- STATE ---
    // Maps parameter names (e.g., "width") to their generated input fields
    private final Map<String, TextField> activeInputFields = new HashMap<>();

    /**
     * Initializes the controller with required services.
     *
     * @param service         The domain logic provider.
     * @param onDomainCreated Callback executed upon successful domain creation.
     */
    public void init(DomainService service, Consumer<DomainDefinition> onDomainCreated) {
        this.service = service;
        this.onDomainCreated = onDomainCreated;

        // Populate combo box from service (SSOT - Single Source of Truth)
        domainTypeCombo.getItems().setAll(service.getAvailableDomainTypes());
    }

    // ==========================================
    // DYNAMIC FORM GENERATION
    // ==========================================

    /**
     * Handles the selection of a geometry type.
     * Rebuilds the input form based on the type's required parameters.
     */
    @FXML
    private void handleTypeSelection() {
        DomainType selectedType = domainTypeCombo.getValue();

        // Reset Form State
        dynamicFormContainer.getChildren().clear();
        activeInputFields.clear();
        feedbackLabel.setText("");

        if (selectedType == null) {
            createButton.setDisable(true);
            return;
        }

        createButton.setDisable(false);

        // Generate fields dynamically based on metadata
        for (String paramName : selectedType.getRequiredParameters()) {
            VBox fieldGroup = createFieldGroup(paramName);
            dynamicFormContainer.getChildren().add(fieldGroup);
        }
    }

    /**
     * Helper to create a labeled input block visually.
     *
     * @param paramKey The internal parameter name (e.g., "radius").
     * @return A VBox containing the Label and TextField.
     */
    private VBox createFieldGroup(String paramKey) {
        VBox box = new VBox(5);

        // Capitalize label (e.g., "radius" -> "Radius")
        String labelText = paramKey.substring(0, 1).toUpperCase() + paramKey.substring(1).toLowerCase();
        Label label = new Label(labelText + " (m):");

        TextField textField = new TextField();
        textField.setPromptText("Enter value > 0");

        // Register field for later retrieval
        activeInputFields.put(paramKey, textField);

        box.getChildren().addAll(label, textField);
        return box;
    }

    // ==========================================
    // CREATION LOGIC
    // ==========================================

    @FXML
    private void handleCreateAction() {
        feedbackLabel.setText("");
        DomainType type = domainTypeCombo.getValue();

        try {
            // 1. Collect & Parse Data
            Map<String, Double> params = collectParameters();

            // 2. Delegate to Service (Validation Layer)
            // Throws exception if geometry is invalid
            service.createDomain(type, params);

            // 3. Success Workflow
            feedbackLabel.setStyle("-fx-text-fill: green;");
            feedbackLabel.setText("Domain configured successfully!");
            createButton.setDisable(true); // Prevent double submission

            // Notify Orchestrator
            if (onDomainCreated != null) {
                onDomainCreated.accept(new DomainDefinition(type, params));
            }

        } catch (NumberFormatException e) {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Input Error: Please enter valid numbers.");
        } catch (DomainConstraintException e) {
            // Semantic Errors (e.g., Rectangle with negative area)
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Geometry Error: " + e.getMessage());
        } catch (InvalidInputException e) {
            // Validation Errors (e.g., Empty fields)
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Missing Data: " + e.getMessage());
        } catch (Exception e) {
            // Unexpected Errors
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("System Error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Harvests values from the dynamically generated TextFields.
     *
     * @return A map of parameter names to double values.
     * @throws NumberFormatException If any field contains non-numeric data or is empty.
     */
    private Map<String, Double> collectParameters() throws NumberFormatException {
        Map<String, Double> params = new HashMap<>();

        for (Map.Entry<String, TextField> entry : activeInputFields.entrySet()) {
            String textVal = entry.getValue().getText().trim();
            if (textVal.isEmpty()) {
                throw new NumberFormatException("Empty Field");
            }
            // Sanitize input (Comma to Dot)
            double val = Double.parseDouble(textVal.replace(",", "."));
            params.put(entry.getKey(), val);
        }
        return params;
    }
}