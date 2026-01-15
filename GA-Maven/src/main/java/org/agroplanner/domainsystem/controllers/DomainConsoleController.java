package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.domainsystem.views.DomainViewContract;

import java.util.Map;
import java.util.Optional;

/**
 * Orchestrates the interactive domain initialization workflow.
 * <p>
 * This controller mediates the configuration process, guiding the user through
 * type selection, parameter acquisition, and validation, ensuring only valid
 * geometric definitions are passed to the rest of the system.
 * </p>
 */
public class DomainConsoleController {

    private final DomainViewContract view;

    private final DomainService service;

    /**
     * Initializes the controller with the IO handler and business logic facade.
     *
     * @param view    The view abstraction for user interaction.
     * @param service The domain service for logic and instantiation.
     */
    public DomainConsoleController(DomainViewContract view, DomainService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Executes the wizard for geometric domain creation.
     * <p>
     * Implements a robust <strong>Retry Loop</strong>: domain-specific exceptions
     * (Validation or Constraints) are caught and displayed as feedback, allowing the
     * user to correct inputs without restarting the application.
     * </p>
     *
     * @return An {@link Optional} containing the valid {@link DomainDefinition} if created,
     * or empty if the user cancels the operation.
     */
    public Optional<DomainDefinition> runDomainCreationWizard() { //🧙‍♂️
        while (true) {
            // ==================================================================================
            // STEP 1: TYPE SELECTION
            // ==================================================================================
            view.showAvailableDomains(service.getAvailableDomainTypes());
            Optional<DomainType> selectedType = view.askForDomainType(service.getAvailableDomainTypes());

            // EXIT CONDITION: User selected "Quit".
            if (selectedType.isEmpty()) {
                return Optional.empty();
            }

            // ==================================================================================
            // STEP 2: PARAMETER ACQUISITION
            // ==================================================================================
            DomainType type = selectedType.get();

            Map<String, Double> params = view.askForParameters(type);

            // ==================================================================================
            // STEP 3: CREATION & VALIDATION (The "Deep Protection" Boundary)
            // ==================================================================================
            try {

                service.createDomain(type, params);

                view.showSuccessMessage();
                return Optional.of(new DomainDefinition(type, params));

            } catch (DomainConstraintException e) {
                // 🛑 CATEGORY: SEMANTIC/LOGIC ERROR
                // The input numbers are valid doubles, but they form an impossible geometry.
                // Action: Feedback -> Retry.
                view.showErrorMessage(e.getMessage());

            } catch (InvalidInputException e) {
                // 🛑 CATEGORY: DATA INTEGRITY ERROR
                // Malformed request (e.g., missing required parameter).
                // Action: Feedback -> Retry.
                view.showErrorMessage("Input Error: " + e.getMessage());

            } catch (Exception e) {
                // 🛑 CATEGORY: UNEXPECTED FAILURE (Safety Net)
                // Catches RuntimeExceptions (NPE, OOM, etc.) to keep the CLI alive.
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
                // e.printStackTrace(); // Uncomment for dev debugging only
            }

            // LOOP: The loop structure ensures that after an exception is caught and displayed,
            // execution returns to the start of the wizard (Retry).
        }
    }
}
