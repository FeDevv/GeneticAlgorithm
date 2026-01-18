package org.agroplanner.inventory.controller;

import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;
import org.agroplanner.inventory.views.InventoryViewContract;
import org.agroplanner.shared.exceptions.DataPersistenceException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.List;
import java.util.Optional;

/**
 * Controller component orchestrating the Inventory and Catalog management workflows.
 * <p>
 * This class acts as the mediator between the user interface (View) and the domain model,
 * handling two distinct flows:
 * <ol>
 * <li><strong>Inventory Population:</strong> Selecting varieties and quantities for a simulation.</li>
 * <li><strong>Catalog Management:</strong> Creating new varieties (Agronomist only).</li>
 * </ol>
 * </p>
 */
public class InventoryConsoleController {

    private static final double MAX_RADIUS = 9999.0;

    private final InventoryViewContract view;
    private final PlantVarietyDAOContract dao;
    private final CatalogService catalogService;

    /**
     * Constructs the controller using Constructor Injection.
     *
     * @param view The abstraction of the user interface.
     * @param dao  The data access object injected by the system orchestrator.
     */
    public InventoryConsoleController(InventoryViewContract view, PlantVarietyDAOContract dao) {
        this.view = view;
        this.dao = dao;
        this.catalogService = new CatalogService(dao);
    }


    /**
     * Executes the interactive wizard to populate the simulation inventory.
     *
     * @param domainMaxRadius The maximum allowable radius imposed by the selected field domain.
     * @return A fully populated {@link PlantInventory} ready for the evolutionary engine.
     */
    public PlantInventory runInventoryWizard(double domainMaxRadius) {
        view.showWizardStart();
        PlantInventory inventory = new PlantInventory();
        boolean keepAdding = true;

        while (keepAdding) {
            try {
                // 1. Category Selection
                view.showAvailablePlantTypes(PlantType.values());
                Optional<PlantType> typeOpt = view.askForPlantType(PlantType.values());

                if (typeOpt.isPresent()) {
                    PlantType selectedType = typeOpt.get();
                    List<PlantVarietySheet> availableVarieties = dao.findByType(selectedType);

                    if (!availableVarieties.isEmpty()) {

                        // 2. Variety Selection
                        PlantVarietySheet selectedSheet = view.askForVarietySelection(availableVarieties);

                        // Constraint Check: Physical fit
                        if (selectedSheet.getMinDistance() > domainMaxRadius) {
                            throw new InvalidInputException("Variety too large for this domain.");
                        }

                        // 3. Quantity Input
                        int quantity = view.askForQuantity(selectedSheet.getVarietyName());
                        inventory.addEntry(selectedSheet, quantity);

                        // 4. Feedback & Loop check
                        view.showCurrentStatus(inventory.getTotalPopulationSize(), inventory.getMaxRadius());
                        keepAdding = view.askIfAddMore();

                    } else {
                        view.showNoVarietiesFound(selectedType);
                    }

                } else {
                    keepAdding = false;
                }

            } catch (InvalidInputException | DataPersistenceException e) {
                view.showErrorMessage(e.getMessage());
            }
        }
        return inventory;
    }

    /**
     * Executes the Catalog Management workflow for authorized personnel.
     *
     * @param currentUser The user attempting to access the module.
     */
    public void runCatalogManager(User currentUser) {
        if (currentUser.getRole() != Role.AGRONOMIST) {
            view.showErrorMessage("Access Denied.");
            return;
        }

        boolean keepCreating = true;
        while (keepCreating) {
            handleCreationFlow(currentUser);
            keepCreating = view.askIfAddMore();
        }
    }

    /**
     * Orchestrates the creation of a single plant variety.
     */
    private void handleCreationFlow(User author) {
        try {
            view.showAvailablePlantTypes(PlantType.values());
            Optional<PlantType> typeOpt = view.askForPlantType(PlantType.values());

            if (typeOpt.isEmpty()) {
                return;
            }
            PlantType type = typeOpt.get();

            // The View handles format validation (Regex, Number parsing)
            PlantVarietySheet newSheet = view.askForNewSheetData(type, MAX_RADIUS);
            newSheet.setAuthor(author);

            // Delegate to Service
            if (catalogService.registerNewVariety(newSheet)) {
                view.showSuccessMessage("New variety '" + newSheet.getVarietyName() + "' added to Catalog.");
            } else {
                view.showErrorMessage("Failed to save variety (Database rejected the operation).");
            }

        } catch (InvalidInputException e) {
            view.showErrorMessage("Validation Error: " + e.getMessage());
        } catch (DataPersistenceException e) {
            view.showErrorMessage("Database Error: " + e.getMessage());
        } catch (Exception e) {
            String msg = e.getMessage() != null ? e.getMessage() : "Unknown Internal Error";
            view.showErrorMessage("Critical Error: " + msg);
        }
    }

    /**
     * Delegates the visualization of the solution manifest to the view.
     */
    public void showSolutionManifest(List<PlantVarietySheet> sheets) {
        view.displayDetailedManifest(sheets);
    }
}