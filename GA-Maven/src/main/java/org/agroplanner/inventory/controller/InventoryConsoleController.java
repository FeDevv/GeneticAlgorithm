package org.agroplanner.inventory.controller;

import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.view.InventoryViewContract;
import org.agroplanner.shared.exceptions.InvalidInputException;

/**
 * Controller component responsible for orchestrating the inventory population workflow.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> MVC Controller. Acts as the mediator between the {@link InventoryViewContract} (UI)
 * and the {@link PlantInventory} (Model).</li>
 * <li><strong>Dependency Inversion:</strong> Dependencies are injected via the constructor using the
 * <em>Constructor Injection</em> pattern. This decouples the controller from specific View implementations
 * (e.g., Console vs. GUI) and facilitates Unit Testing via mocking.</li>
 * <li><strong>Workflow:</strong> Implements a synchronous "Wizard" pattern, guiding the user through
 * a sequential series of steps to populate the plant list.</li>
 * </ul>
 */
public class InventoryConsoleController {

    /**
     * The abstract interface for user interaction.
     */
    private final InventoryViewContract view;

    /**
     * Constructs a new controller with the specified view implementation.
     *
     * @param view The UI contract implementation (Dependency Injection).
     */
    public InventoryConsoleController(InventoryViewContract view) {
        this.view = view;
    }

    /**
     * Executes the interactive inventory configuration wizard.
     *
     * <p><strong>Control Flow:</strong></p>
     * Enters a conditional loop that persists until the user explicitly opts to stop adding items.
     * Within each iteration, it:
     * <ol>
     * <li>Request data input from the View (Type, Quantity, Radius).</li>
     * <li>Attempts to mutate the Model state.</li>
     * <li>Provides feedback on the current inventory status.</li>
     * </ol>
     *
     * <p><strong>Error Handling Strategy (Recovery Loop):</strong></p>
     * Implements a <code>try-catch</code> block around the Model mutation. If the Model rejects the data
     * (throwing {@link InvalidInputException}), the controller catches the error, instructs the View
     * to display the error message, and restarts the loop iteration. This ensures the application
     * remains stable and allows the user to correct their input without crashing the session.
     *
     * @param domainMaxRadius A contextual constraint from the main application, used to validate
     * that plants do not physically exceed the available domain boundaries.
     * @return A fully populated, validated {@link PlantInventory} instance ready for processing.
     */
    public PlantInventory runInventoryWizard(double domainMaxRadius) {
        view.showWizardStart();
        PlantInventory inventory = new PlantInventory();
        boolean keepAdding = true;

        while (keepAdding) {
            try {
                // 1. View Interaction
                view.showAvailablePlants(PlantType.values());
                PlantType type = view.askForPlantSelection(PlantType.values());

                int quantity = view.askForQuantity(type);
                double radius = view.askForRadius(type, domainMaxRadius);

                // 2. Model Update (Deep Protection boundary)
                // If inputs are invalid (logic bypassed in View), addEntry throws InvalidInputException.
                inventory.addEntry(type, quantity, radius);

                // 3. Feedback Loop
                view.showCurrentStatus(inventory.getTotalPopulationSize(), inventory.getMaxRadius());

                // 4. Continuation Check
                keepAdding = view.askIfAddMore();

            } catch (InvalidInputException e) {
                // Recoverable Error Boundary:
                // Captures domain validation failures and bridges them back to the UI.
                view.showErrorMessage(e.getMessage());
            }
        }

        return inventory;
    }
}