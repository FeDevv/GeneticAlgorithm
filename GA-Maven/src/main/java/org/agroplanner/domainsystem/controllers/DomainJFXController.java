package org.agroplanner.domainsystem.controllers;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

/**
 * Controller JavaFX per la creazione del Dominio.
 * Gestisce la generazione dinamica del form in base al DomainType selezionato.
 */
public class DomainJFXController {

    // --- DEPENDENCIES ---
    private DomainService service;
    private Consumer<DomainDefinition> onDomainCreated; // Callback verso l'Orchestrator

    // --- FXML FIELDS ---
    @FXML private ComboBox<DomainType> domainTypeCombo;
    @FXML private VBox dynamicFormContainer;
    @FXML private Label feedbackLabel;
    @FXML private Button createButton;

    // --- STATE ---
    // Mappa per tenere traccia dei TextField generati dinamicamente (Nome Parametro -> Input Field)
    private final Map<String, TextField> activeInputFields = new HashMap<>();

    /**
     * Inizializzazione chiamata dall'Orchestrator.
     */
    public void init(DomainService service, Consumer<DomainDefinition> onDomainCreated) {
        this.service = service;
        this.onDomainCreated = onDomainCreated;

        // Popola la combobox usando il service (proprio come nel CLI)
        domainTypeCombo.getItems().setAll(service.getAvailableDomainTypes());
    }

    // ==========================================
    // UI DYNAMICS
    // ==========================================

    @FXML
    private void handleTypeSelection() {
        DomainType selectedType = domainTypeCombo.getValue();

        // Pulisci il form precedente
        dynamicFormContainer.getChildren().clear();
        activeInputFields.clear();
        feedbackLabel.setText("");

        if (selectedType == null) {
            createButton.setDisable(true);
            return;
        }

        createButton.setDisable(false);

        // Generazione campi basata sui metadati (Type.getRequiredParameters)
        // Questo sostituisce il loop "askForParameters" della CLI View
        for (String paramName : selectedType.getRequiredParameters()) {
            VBox fieldGroup = createFieldGroup(paramName);
            dynamicFormContainer.getChildren().add(fieldGroup);
        }
    }

    /**
     * Helper per creare visualmente un blocco Label + TextField
     */
    private VBox createFieldGroup(String paramKey) {
        VBox box = new VBox(5);

        // Capitalizza la prima lettera per l'etichetta (es. "radius" -> "Radius")
        String labelText = paramKey.substring(0, 1).toUpperCase() + paramKey.substring(1).toLowerCase();
        Label label = new Label(labelText + " (m):");

        TextField textField = new TextField();
        textField.setPromptText("Inserisci valore numerico > 0");

        // Salva il riferimento nella mappa per recuperarlo dopo
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
            // 1. Raccogli e Converti i dati dai campi dinamici
            Map<String, Double> params = collectParameters();

            // 2. Chiama il Service (Deep Protection Layer)
            // Se i dati sono geometricamente impossibili, il service lancerà eccezione
            service.createDomain(type, params);

            // 3. Successo
            feedbackLabel.setStyle("-fx-text-fill: green;");
            feedbackLabel.setText("Dominio creato con successo!");
            createButton.setDisable(true); // Evita doppi click

            // Passa il risultato all'Orchestrator
            if (onDomainCreated != null) {
                onDomainCreated.accept(new DomainDefinition(type, params));
            }

        } catch (NumberFormatException e) {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Errore Input: Inserisci solo numeri validi (es. 10.5).");
        } catch (DomainConstraintException e) {
            // Errori di logica geometrica (es. Rettangolo con area negativa)
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Errore Geometria: " + e.getMessage());
        } catch (InvalidInputException e) {
            // Errori di validazione (es. campi vuoti)
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Dati mancanti: " + e.getMessage());
        } catch (Exception e) {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Errore Critico: " + e.getMessage());
        }
    }

    /**
     * Legge i valori dai TextField generati.
     * Lancia NumberFormatException se l'input non è un double.
     */
    private Map<String, Double> collectParameters() throws NumberFormatException {
        Map<String, Double> params = new HashMap<>();

        for (Map.Entry<String, TextField> entry : activeInputFields.entrySet()) {
            String textVal = entry.getValue().getText().trim();
            if (textVal.isEmpty()) {
                throw new NumberFormatException("Campo vuoto");
            }
            // Sostituisce la virgola con punto per compatibilità locale
            double val = Double.parseDouble(textVal.replace(",", "."));
            params.put(entry.getKey(), val);
        }
        return params;
    }
}
