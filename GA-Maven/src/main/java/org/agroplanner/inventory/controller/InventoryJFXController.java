package org.agroplanner.inventory.controller;

import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.util.StringConverter;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.List;
import java.util.function.Consumer;

/**
 * Controller JavaFX per la gestione dell'Inventario.
 */
public class InventoryJFXController {

    // --- DEPENDENCIES ---
    private PlantVarietyDAOContract dao;
    private CatalogService catalogService; // Per creare nuove varietà
    private Consumer<PlantInventory> onInventoryConfirmed;
    private User currentUser;
    private double maxDomainRadius;
    private boolean isCatalogMode = false;
    private Runnable onBackToDashboard;

    // --- STATE ---
    // Usiamo una lista osservabile per la tabella
    private final ObservableList<InventoryRow> tableData = FXCollections.observableArrayList();
    // L'oggetto di dominio reale che stiamo costruendo
    private final PlantInventory buildingInventory = new PlantInventory();

    // --- FXML FIELDS ---
    @FXML private ComboBox<PlantType> typeCombo;
    @FXML private ComboBox<PlantVarietySheet> varietyCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addButton;
    @FXML private Label feedbackLabel;
    @FXML private VBox agronomistPanel;

    // Variety Details
    @FXML private VBox varietyDetailsBox;
    @FXML private Label detailNameLabel;
    @FXML private Label detailDistanceLabel;
    @FXML private Label detailAuthorLabel;

    // Table
    @FXML private TableView<InventoryRow> inventoryTable;
    @FXML private TableColumn<InventoryRow, String> colName;
    @FXML private TableColumn<InventoryRow, String> colType;
    @FXML private TableColumn<InventoryRow, String> colRadius;
    @FXML private TableColumn<InventoryRow, Integer> colQty;
    @FXML private TableColumn<InventoryRow, String> colAuthor;
    @FXML private Label totalCountLabel;

    @FXML private Button proceedButton;
    @FXML private Button cancelButton;


    /**
     * Inizializzazione.
     */
    public void init(User user, double maxRadius, PlantVarietyDAOContract dao, Consumer<PlantInventory> onConfirm, Runnable onCancel) {
        this.currentUser = user;
        this.maxDomainRadius = maxRadius;
        this.dao = dao;
        this.catalogService = new CatalogService(dao);
        this.onInventoryConfirmed = onConfirm;
        this.onBackToDashboard = onCancel;

        setupUI();
    }

    public void initCatalogMode(User user, PlantVarietyDAOContract dao, Runnable onBack) {
        this.isCatalogMode = true;
        this.currentUser = user;
        this.maxDomainRadius = 99999.0; // Raggio infinito per il catalogo
        this.dao = dao;
        this.catalogService = new CatalogService(dao);
        this.onBackToDashboard = onBack;

        setupUI(); // Usa il setup esistente

        // TRASFORMAZIONE UI
        quantitySpinner.setVisible(false);
        addButton.setVisible(false);
        totalCountLabel.setVisible(false);

        cancelButton.setVisible(false);

        // Rinomina bottone e colonna per adattarli al contesto
        proceedButton.setText("Torna alla Dashboard");
        proceedButton.setDisable(false);

        // Riutilizziamo la colonna Quantità per mostrare l'Autore
        colAuthor.setText("Autore");
        colAuthor.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().sheet.getAuthor() != null ? cell.getValue().sheet.getAuthor().getUsername() : "-"
        ));
        colAuthor.setVisible(true);
    }

    private void setupUI() {
        // 1. Setup ComboBox Types
        typeCombo.getItems().setAll(PlantType.values());

        // 2. Setup Spinner (min 1, max 1000, default 10)
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10));

        // 3. Setup Table Columns
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().sheet.getVarietyName()));
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().sheet.getType().getLabel()));
        colRadius.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.2f", cell.getValue().sheet.getMinDistance())));
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().qty).asObject());
        colAuthor.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().sheet.getAuthor() != null ? cell.getValue().sheet.getAuthor().getUsername() : "-"
        ));

        inventoryTable.setItems(tableData);

        // 4. Agronomist features
        if (currentUser.getRole() == Role.AGRONOMIST) {
            agronomistPanel.setVisible(true);
        }

        // 5. Converter per mostrare il nome della varietà nella ComboBox invece del toString()
        varietyCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlantVarietySheet object) {
                return object == null ? "" : object.getVarietyName();
            }
            @Override
            public PlantVarietySheet fromString(String string) { return null; }
        });
    }

    // ==========================================
    // SELECTION FLOW
    // ==========================================

    @FXML
    private void handleTypeSelection() {
        PlantType selectedType = typeCombo.getValue();
        if (selectedType == null) return;

        // Reset flow
        varietyCombo.getItems().clear();
        varietyCombo.setDisable(true);
        addButton.setDisable(true);
        varietyDetailsBox.setVisible(false);

        // Fetch Data
        List<PlantVarietySheet> varieties = dao.findByType(selectedType);

        if (varieties.isEmpty()) {
            feedbackLabel.setText("Nessuna varietà trovata per questa categoria.");
        } else {
            varietyCombo.getItems().setAll(varieties);
            varietyCombo.setDisable(false);
            feedbackLabel.setText("");
        }

        if (isCatalogMode && !varieties.isEmpty()) {
            tableData.clear();
            for (PlantVarietySheet p : varieties) {
                // Usiamo 0 come quantità fittizia
                tableData.add(new InventoryRow(p, 0));
            }
        }
    }

    @FXML
    private void handleVarietySelection() {
        PlantVarietySheet selected = varietyCombo.getValue();
        if (selected == null) {
            addButton.setDisable(true);
            varietyDetailsBox.setVisible(false);
            return;
        }

        // Show Details
        varietyDetailsBox.setVisible(true);
        detailNameLabel.setText(selected.getVarietyName());
        detailDistanceLabel.setText(String.format("Distanza: %.2f m", selected.getMinDistance()));
        String authName = (selected.getAuthor() != null) ? selected.getAuthor().getFullName() : "Sconosciuto";
        detailAuthorLabel.setText("Autore: " + authName);

        // Constraint Check (Domain Radius)
        if (selected.getMinDistance() > maxDomainRadius) {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("ATTENZIONE: Questa pianta è troppo grande per il dominio (Max " + maxDomainRadius + "m).");
            addButton.setDisable(true);
        } else {
            feedbackLabel.setText("");
            addButton.setDisable(false);
        }
    }

    // ==========================================
    // ADD & CONFIRM LOGIC
    // ==========================================

    @FXML
    private void handleAddAction() {
        PlantVarietySheet sheet = varietyCombo.getValue();
        int qty = quantitySpinner.getValue();

        // 1. Update Domain Model
        buildingInventory.addEntry(sheet, qty);

        // 2. Update UI Model
        tableData.add(new InventoryRow(sheet, qty));

        // 3. Update Totals
        updateSummary();

        // 4. Feedback
        feedbackLabel.setStyle("-fx-text-fill: green;");
        feedbackLabel.setText("Aggiunto " + qty + " x " + sheet.getVarietyName());
        proceedButton.setDisable(false);
    }

    private void updateSummary() {
        int total = tableData.stream().mapToInt(r -> r.qty).sum();
        totalCountLabel.setText("Totale Piante: " + total);
    }

    @FXML
    private void handleProceedAction() {
        if (isCatalogMode) {
            if (onBackToDashboard != null) onBackToDashboard.run();
        } else {
            // Il codice vecchio:
            if (onInventoryConfirmed != null) onInventoryConfirmed.accept(buildingInventory);
        }
    }

    @FXML
    private void handleCancelAction() {
        // Usa la variabile condivisa
        if (onBackToDashboard != null) {
            onBackToDashboard.run();
        }
    }

    // ==========================================
    // AGRONOMIST: CREATE NEW
    // ==========================================

    @FXML
    private void handleCreateNewVarietyAction() {
        Dialog<PlantVarietySheet> dialog = new Dialog<>();
        dialog.setTitle("Nuova Varietà");
        dialog.setHeaderText("Crea una nuova scheda tecnica");

        // Pulsanti
        ButtonType createButtonType = new ButtonType("Salva", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createButtonType, ButtonType.CANCEL);

        // Layout Form (GridPane)
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // --- DEFINIZIONE CAMPI ---

        // 1. Tipo (Pre-selezionato se possibile)
        ComboBox<PlantType> typeDialogCombo = new ComboBox<>(FXCollections.observableArrayList(PlantType.values()));
        if (typeCombo.getValue() != null) {
            typeDialogCombo.setValue(typeCombo.getValue());
        } else {
            typeDialogCombo.getSelectionModel().selectFirst();
        }

        // 2. Nome
        TextField nameField = new TextField();
        nameField.setPromptText("Es. Mela Fuji");

        // 3. Distanza
        TextField distanceField = new TextField();
        distanceField.setPromptText("Raggio in metri (es. 2.5)");

        // 4. Periodo Semina
        TextField sowingField = new TextField();
        sowingField.setPromptText("Es. Marzo - Aprile");

        // 5. Note
        TextField notesField = new TextField();
        notesField.setPromptText("Note tecniche opzionali");

        // --- POSIZIONAMENTO NELLA GRIGLIA (Colonna, Riga) ---
        grid.add(new Label("Categoria:"), 0, 0);
        grid.add(typeDialogCombo, 1, 0);

        grid.add(new Label("Nome Varietà:"), 0, 1);
        grid.add(nameField, 1, 1);

        grid.add(new Label("Distanza Min (m):"), 0, 2);
        grid.add(distanceField, 1, 2);

        grid.add(new Label("Periodo Semina:"), 0, 3);
        grid.add(sowingField, 1, 3);

        grid.add(new Label("Note:"), 0, 4);
        grid.add(notesField, 1, 4);

        dialog.getDialogPane().setContent(grid);

        // --- CONVERSIONE RISULTATO ---
        dialog.setResultConverter(dialogButton -> {
            if (dialogButton == createButtonType) {
                try {
                    // Validazione minima: controlliamo che nome e distanza non siano vuoti
                    if (nameField.getText().trim().isEmpty() || distanceField.getText().trim().isEmpty()) {
                        return null;
                    }

                    PlantVarietySheet s = new PlantVarietySheet();
                    s.setVarietyName(nameField.getText().trim());
                    s.setType(typeDialogCombo.getValue());

                    // FIX: Sostituiamo la virgola col punto per evitare errori se l'utente scrive "2,5"
                    String distanceStr = distanceField.getText().replace(",", ".");
                    s.setMinDistance(Double.parseDouble(distanceStr));

                    s.setSowingPeriod(sowingField.getText().trim());
                    s.setNotes(notesField.getText().trim());
                    s.setAuthor(currentUser);

                    return s;
                } catch (NumberFormatException e) {
                    // Se la distanza non è un numero valido
                    return null;
                }
            }
            return null;
        });

        // Mostra il Dialog e gestisci il risultato
        dialog.showAndWait().ifPresent(newSheet -> {
            boolean saved = catalogService.registerNewVariety(newSheet);

            if (saved) {
                feedbackLabel.setStyle("-fx-text-fill: green;");
                feedbackLabel.setText("Nuova varietà '" + newSheet.getVarietyName() + "' salvata nel DB!");

                // Refresh intelligente:
                // Se la categoria della nuova pianta corrisponde a quella attualmente visualizzata nel filtro,
                // ricarichiamo la lista.
                // Questo aggiorna sia la ComboBox (in simulazione) sia la Tabella (in catalogo).
                if (typeCombo.getValue() == newSheet.getType()) {
                    handleTypeSelection();
                }
            } else {
                feedbackLabel.setStyle("-fx-text-fill: red;");
                feedbackLabel.setText("Errore salvataggio: controlla i dati.");
            }
        });
    }

    // --- INNER CLASS FOR TABLE ---
    public static class InventoryRow {
        PlantVarietySheet sheet;
        int qty;

        public InventoryRow(PlantVarietySheet sheet, int qty) {
            this.sheet = sheet;
            this.qty = qty;
        }
    }
}
