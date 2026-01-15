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
 * JavaFX Controller for Inventory Management.
 * <p>
 * This controller serves dual purposes based on the initialization mode:
 * <ul>
 * <li><strong>Wizard Mode:</strong> Allows users to build an inventory for simulation.</li>
 * <li><strong>Catalog Mode:</strong> Allows Agronomists to view and manage the database.</li>
 * </ul>
 * </p>
 */
public class InventoryJFXController {

    // --- DEPENDENCIES ---
    private PlantVarietyDAOContract dao;
    private CatalogService catalogService;
    private Consumer<PlantInventory> onInventoryConfirmed;
    private User currentUser;
    private double maxDomainRadius;
    private Runnable onBackToDashboard;

    // --- STATE FLAGS ---
    private boolean isCatalogMode = false;
    private final ObservableList<InventoryRow> tableData = FXCollections.observableArrayList();
    private final PlantInventory buildingInventory = new PlantInventory();

    // --- FXML INJECTIONS ---
    @FXML private ComboBox<PlantType> typeCombo;
    @FXML private ComboBox<PlantVarietySheet> varietyCombo;
    @FXML private Spinner<Integer> quantitySpinner;
    @FXML private Button addButton;
    @FXML private Label feedbackLabel;
    @FXML private VBox agronomistPanel;

    // Variety Details Panel
    @FXML private VBox varietyDetailsBox;
    @FXML private Label detailNameLabel;
    @FXML private Label detailDistanceLabel;
    @FXML private Label detailAuthorLabel;

    // Data Table
    @FXML private TableView<InventoryRow> inventoryTable;
    @FXML private TableColumn<InventoryRow, String> colName;
    @FXML private TableColumn<InventoryRow, String> colType;
    @FXML private TableColumn<InventoryRow, String> colRadius;
    @FXML private TableColumn<InventoryRow, Integer> colQty;
    @FXML private TableColumn<InventoryRow, String> colAuthor;
    @FXML private Label totalCountLabel;

    // Navigation Buttons
    @FXML private Button proceedButton;
    @FXML private Button cancelButton;


    /**
     * Initializes the controller in "Wizard Mode" (Simulation Setup).
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

    /**
     * Initializes the controller in "Catalog Mode" (Database Management).
     */
    public void initCatalogMode(User user, PlantVarietyDAOContract dao, Runnable onBack) {
        this.isCatalogMode = true;
        this.currentUser = user;
        this.maxDomainRadius = 99999.0; // Virtually infinite for catalog viewing
        this.dao = dao;
        this.catalogService = new CatalogService(dao);
        this.onBackToDashboard = onBack;

        setupUI();
        transformUiForCatalogMode();
    }

    private void setupUI() {
        // 1. Populate Types
        typeCombo.getItems().setAll(PlantType.values());

        // 2. Configure Spinner (min 1, max 1000, default 10)
        quantitySpinner.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 10));

        // 3. Bind Table Columns
        colName.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().sheet.getVarietyName()));
        colType.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().sheet.getType().getLabel()));
        colRadius.setCellValueFactory(cell -> new SimpleStringProperty(String.format("%.2f", cell.getValue().sheet.getMinDistance())));
        colQty.setCellValueFactory(cell -> new SimpleIntegerProperty(cell.getValue().qty).asObject());

        // Author column is displayed only when data is available or in Catalog Mode
        colAuthor.setCellValueFactory(cell -> new SimpleStringProperty(
                cell.getValue().sheet.getAuthor() != null ? cell.getValue().sheet.getAuthor().getUsername() : "-"
        ));

        inventoryTable.setItems(tableData);

        // 4. Enable Agronomist Features if applicable
        if (currentUser.getRole() == Role.AGRONOMIST) {
            agronomistPanel.setVisible(true);
        }

        // 5. Custom StringConverter for ComboBox to display names properly
        varietyCombo.setConverter(new StringConverter<>() {
            @Override
            public String toString(PlantVarietySheet object) {
                return object == null ? "" : object.getVarietyName();
            }
            @Override
            public PlantVarietySheet fromString(String string) { return null; }
        });
    }

    private void transformUiForCatalogMode() {
        // Hide simulation-specific controls
        quantitySpinner.setVisible(false);
        addButton.setVisible(false);
        totalCountLabel.setVisible(false);
        cancelButton.setVisible(false);

        // Re-purpose Navigation Button
        proceedButton.setText("Back to Dashboard");
        proceedButton.setDisable(false);

        // Ensure Author column is visible and clearly labeled
        colAuthor.setText("Author");
        colAuthor.setVisible(true);
    }

    // ==========================================
    // SELECTION LOGIC
    // ==========================================

    @FXML
    private void handleTypeSelection() {
        PlantType selectedType = typeCombo.getValue();
        if (selectedType == null) return;

        // Reset UI state
        varietyCombo.getItems().clear();
        varietyCombo.setDisable(true);
        addButton.setDisable(true);
        varietyDetailsBox.setVisible(false);

        // Fetch Data
        List<PlantVarietySheet> varieties = dao.findByType(selectedType);

        if (varieties.isEmpty()) {
            feedbackLabel.setText("No varieties found for this category.");
        } else {
            varietyCombo.getItems().setAll(varieties);
            varietyCombo.setDisable(false);
            feedbackLabel.setText("");
        }

        // In Catalog Mode, auto-populate table with filtered results
        if (isCatalogMode && !varieties.isEmpty()) {
            tableData.clear();
            for (PlantVarietySheet p : varieties) {
                tableData.add(new InventoryRow(p, 0)); // 0 qty for display
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

        // Display Details
        varietyDetailsBox.setVisible(true);
        detailNameLabel.setText(selected.getVarietyName());
        detailDistanceLabel.setText(String.format("Min Distance: %.2f m", selected.getMinDistance()));
        String authName = (selected.getAuthor() != null) ? selected.getAuthor().getFullName() : "Unknown";
        detailAuthorLabel.setText("Author: " + authName);

        // Constraint Validation (Domain Radius)
        if (selected.getMinDistance() > maxDomainRadius) {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("WARNING: Plant too large for this domain (Max " + maxDomainRadius + "m).");
            addButton.setDisable(true);
        } else {
            feedbackLabel.setText("");
            addButton.setDisable(false);
        }
    }

    // ==========================================
    // INVENTORY BUILDING LOGIC
    // ==========================================

    @FXML
    private void handleAddAction() {
        PlantVarietySheet sheet = varietyCombo.getValue();
        int qty = quantitySpinner.getValue();

        // 1. Update Domain Model
        buildingInventory.addEntry(sheet, qty);

        // 2. Update UI Table
        tableData.add(new InventoryRow(sheet, qty));

        // 3. Update Summary
        updateSummary();

        // 4. User Feedback
        feedbackLabel.setStyle("-fx-text-fill: green;");
        feedbackLabel.setText("Added " + qty + " x " + sheet.getVarietyName());
        proceedButton.setDisable(false);
    }

    private void updateSummary() {
        int total = tableData.stream().mapToInt(r -> r.qty).sum();
        totalCountLabel.setText("Total Plants: " + total);
    }

    @FXML
    private void handleProceedAction() {
        if (isCatalogMode) {
            if (onBackToDashboard != null) onBackToDashboard.run();
        } else {
            if (onInventoryConfirmed != null) onInventoryConfirmed.accept(buildingInventory);
        }
    }

    @FXML
    private void handleCancelAction() {
        if (onBackToDashboard != null) onBackToDashboard.run();
    }

    // ==========================================
    // AGRONOMIST FEATURES (CREATE NEW)
    // ==========================================

    @FXML
    private void handleCreateNewVarietyAction() {
        // 1. Setup Dialog
        Dialog<PlantVarietySheet> dialog = new Dialog<>();
        dialog.setTitle("New Plant Variety");
        dialog.setHeaderText("Create a new technical sheet");

        ButtonType createBtnType = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE);
        dialog.getDialogPane().getButtonTypes().addAll(createBtnType, ButtonType.CANCEL);

        // 2. Build UI
        VarietyFormContext form = createFormContext();
        dialog.getDialogPane().setContent(form.layout());

        dialog.setResultConverter(btn -> {
            if (btn == createBtnType) {
                return convertFormToSheet(form);
            }
            return null;
        });

        // 4. Result management
        dialog.showAndWait().ifPresent(this::saveAndGiveFeedback);
    }

    // helpers

    private VarietyFormContext createFormContext() {
        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(20, 150, 10, 10));

        // Components
        ComboBox<PlantType> typeBox = new ComboBox<>(FXCollections.observableArrayList(PlantType.values()));

        if (typeCombo.getValue() != null) typeBox.setValue(typeCombo.getValue());
        else typeBox.getSelectionModel().selectFirst();

        TextField name = new TextField();
        name.setPromptText("e.g., Fuji Apple");

        TextField dist = new TextField();
        dist.setPromptText("Radius in meters (e.g., 2.5)");

        TextField sowing = new TextField();
        sowing.setPromptText("e.g., March - April");

        TextField notes = new TextField();
        notes.setPromptText("Optional technical notes");

        // Layout
        grid.add(new Label("Category:"), 0, 0);       grid.add(typeBox, 1, 0);
        grid.add(new Label("Variety Name:"), 0, 1);   grid.add(name, 1, 1);
        grid.add(new Label("Min Distance (m):"), 0, 2); grid.add(dist, 1, 2);
        grid.add(new Label("Sowing Period:"), 0, 3);  grid.add(sowing, 1, 3);
        grid.add(new Label("Notes:"), 0, 4);          grid.add(notes, 1, 4);

        return new VarietyFormContext(typeBox, name, dist, sowing, notes, grid);
    }

    private PlantVarietySheet convertFormToSheet(VarietyFormContext form) {
        try {
            String name = form.nameField().getText().trim();
            String distStr = form.distanceField().getText().trim();

            if (name.isEmpty() || distStr.isEmpty()) {
                return null;
            }

            PlantVarietySheet s = new PlantVarietySheet();
            s.setVarietyName(name);
            s.setType(form.typeCombo().getValue());

            s.setMinDistance(Double.parseDouble(distStr.replace(",", ".")));

            s.setSowingPeriod(form.sowingField().getText().trim());
            s.setNotes(form.notesField().getText().trim());
            s.setAuthor(currentUser);

            return s;
        } catch (NumberFormatException _) {
            return null;
        }
    }

    private void saveAndGiveFeedback(PlantVarietySheet newSheet) {
        boolean saved = catalogService.registerNewVariety(newSheet);

        if (saved) {
            feedbackLabel.setStyle("-fx-text-fill: green;");
            feedbackLabel.setText("Success! '" + newSheet.getVarietyName() + "' added to Catalog.");

            // Auto-refresh logic
            if (typeCombo.getValue() == newSheet.getType()) {
                handleTypeSelection();
            }
        } else {
            feedbackLabel.setStyle("-fx-text-fill: red;");
            feedbackLabel.setText("Save Failed: Check your data.");
        }
    }

    // --- INNER CLASS FOR TABLE DATA MODEL ---
    public static class InventoryRow {
        PlantVarietySheet sheet;
        int qty;

        public InventoryRow(PlantVarietySheet sheet, int qty) {
            this.sheet = sheet;
            this.qty = qty;
        }
    }

    private record VarietyFormContext(
            ComboBox<PlantType> typeCombo,
            TextField nameField,
            TextField distanceField,
            TextField sowingField,
            TextField notesField,
            GridPane layout
    ) {}
}