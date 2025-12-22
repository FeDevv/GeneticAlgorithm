package org.agroplanner.inventory.view;

import org.agroplanner.inventory.model.PlantType;

/**
 * Defines the abstract contract for user interface interactions within the Inventory subsystem.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> View Interface (Dependency Inversion Principle).</li>
 * <li><strong>Role:</strong> Decouples the {@link org.agroplanner.inventory.controller.InventoryConsoleController}
 * from specific UI implementations (e.g., CLI, JavaFX, REST API). The Controller interacts strictly against
 * this interface, remaining unaware of the underlying presentation technology.</li>
 * <li><strong>Responsibility:</strong> Handles all low-level I/O operations, data formatting, and
 * basic input sanitization loops, returning clean, strong-typed data to the Controller.</li>
 * </ul>
 */
public interface InventoryViewContract {

    /**
     * Renders the initialization sequence or visual header for the inventory wizard.
     */
    void showWizardStart();

    /**
     * Displays the catalog of supported biological species.
     *
     * @param types The array of available {@link PlantType} constants to be rendered.
     */
    void showAvailablePlants(PlantType[] types);

    /**
     * Captures and interprets user input to select a specific species.
     * <p>
     * <strong>Contract:</strong> The implementation must ensure the returned {@link PlantType} is valid.
     * It should handle invalid selections (e.g., unknown IDs) internally by prompting the user again.
     * </p>
     *
     * @param types The context list of valid options.
     * @return The selected domain constant.
     */
    PlantType askForPlantSelection(PlantType[] types);

    /**
     * Prompts the user for the population size of the current batch.
     *
     * @param selectedType The context (which plant is being configured) for UI labeling.
     * @return A strictly positive integer representing the quantity.
     */
    int askForQuantity(PlantType selectedType);

    /**
     * Prompts the user for the physical radius of the plants.
     *
     * <p><strong>Contextual Validation:</strong></p>
     * The implementation receives {@code maxAllowedRadius} to perform immediate boundary checking
     * (e.g., preventing the user from entering a radius larger than the entire field), providing
     * instant feedback before the data reaches the Controller.
     *
     * @param selectedType     The context for UI labeling.
     * @param maxAllowedRadius The upper bound constraint derived from the domain geometry.
     * @return A positive double value representing the radius in meters.
     */
    double askForRadius(PlantType selectedType, double maxAllowedRadius);

    /**
     * Determines whether the data entry session should continue or conclude.
     *
     * @return {@code true} if the user intends to add another batch; {@code false} otherwise.
     */
    boolean askIfAddMore();

    /**
     * Provides real-time visual feedback regarding the aggregated inventory state.
     *
     * @param totalItems       The cumulative count of plants currently in the cart.
     * @param maxCurrentRadius The maximum radius currently configured (for reference).
     */
    void showCurrentStatus(int totalItems, double maxCurrentRadius);

    /**
     * Renders error states or validation failures to the user.
     *
     * @param message The descriptive error message to display.
     */
    void showErrorMessage(String message);
}
