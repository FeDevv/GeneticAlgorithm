package org.agroplanner.inventory.controller;

import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.view.InventoryViewContract;
import org.agroplanner.shared.exceptions.InvalidInputException;

public class InventoryConsoleController {

    private final InventoryViewContract view;

    public InventoryConsoleController(InventoryViewContract view) {
        this.view = view;
    }

    public PlantInventory runInventoryWizard(double domainMaxRadius) {
        view.showWizardStart();
        PlantInventory inventory = new PlantInventory();
        boolean keepAdding = true;

        while (keepAdding) {
            try {
                // 1. Interazione View
                view.showAvailablePlants(PlantType.values());
                PlantType type = view.askForPlantSelection(PlantType.values());

                int quantity = view.askForQuantity(type);
                double radius = view.askForRadius(type, domainMaxRadius);

                // 2. Aggiornamento Model (con Deep Protection)
                // Se quantity o radius fossero invalidi (es. bug nella view),
                // addEntry lancerebbe InvalidInputException.
                inventory.addEntry(type, quantity, radius);

                // 3. Feedback
                view.showCurrentStatus(inventory.getTotalPopulationSize(), inventory.getMaxRadius());

                // 4. Loop Check
                keepAdding = view.askIfAddMore();

            } catch (InvalidInputException e) {
                // Questo catch serve da "paracadute" se la logica della View fallisce
                // e passa dati sporchi al Model.
                view.showErrorMessage(e.getMessage());
            }
        }

        return inventory;
    }
}