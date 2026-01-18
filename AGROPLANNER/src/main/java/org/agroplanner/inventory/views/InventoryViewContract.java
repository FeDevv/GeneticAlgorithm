package org.agroplanner.inventory.views;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.List;
import java.util.Optional;

/**
 * Defines the contract for Inventory UI interactions.
 * <p>
 * Decouples the controller logic from the console-specific implementation,
 * facilitating future migration to JavaFX or Web interfaces.
 * </p>
 */
public interface InventoryViewContract {

    void showWizardStart();

    void showSuccessMessage(String message);

    // --- POPULATION FLOW ---
    void showAvailablePlantTypes(PlantType[] types);
    Optional<PlantType> askForPlantType(PlantType[] types);

    // --- CREATION FLOW ---
    PlantVarietySheet askForNewSheetData(PlantType type, double maxDomainRadius);

    // --- SELECTION FLOW ---
    PlantVarietySheet askForVarietySelection(List<PlantVarietySheet> varieties);
    void showNoVarietiesFound(PlantType type);

    // --- QUANTITY ---
    int askForQuantity(String varietyName);

    // --- FEEDBACK ---
    void showCurrentStatus(int totalCount, double maxRadius);
    boolean askIfAddMore();
    void showErrorMessage(String message);

    void displayDetailedManifest(List<PlantVarietySheet> sheets);
}
