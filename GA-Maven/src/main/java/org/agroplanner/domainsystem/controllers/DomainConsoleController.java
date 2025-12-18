package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.domainsystem.views.DomainViewContract;

import java.util.Map;
import java.util.Optional;

/**
 * <p><strong>UI Controller for the Domain Creation Subsystem.</strong></p>
 *
 * <p>This controller acts as the orchestrator for the domain creation workflow in a Console environment.
 * It follows the <strong>Model-View-Controller (MVC)</strong> pattern:</p>
 * <ul>
 * <li>It delegates user interaction to the {@link DomainViewContract} (View).</li>
 * <li>It delegates business logic and validation to the {@link DomainService} (Service/Model).</li>
 * <li>It manages the control flow (retries, error handling, exit conditions).</li>
 * </ul>
 */
public class DomainConsoleController {

    private final DomainViewContract view;
    private final DomainService service;

    /**
     * Constructs the controller with the necessary dependencies.
     *
     * @param view    The view implementation responsible for I/O operations.
     * @param service The service responsible for business logic and domain instantiation.
     */
    public DomainConsoleController(DomainViewContract view, DomainService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Executes the interactive wizard for domain creation.
     * <p>
     * This method runs a synchronous loop that guides the user through type selection and parameter input.
     * It implements a <strong>"Retry Loop"</strong> mechanism: if the user provides invalid input
     * (syntactic or semantic), the system catches the exception, displays an error message,
     * and allows the user to try again without crashing the application.
     * </p>
     *
     * @return An {@link Optional} containing the created {@link Domain} if successful,
     * or {@code Optional.empty()} if the user explicitly requested to exit/cancel.
     */
    public Optional<Domain> runDomainCreationWizard() {
        while (true) {
            // ==================================================================================
            // STEP 1: TYPE SELECTION
            // ==================================================================================
            // Delegate the menu display and choice collection to the View.
            // The service provides the list of available types to keep the UI agnostic.
            view.showAvailableDomains(service.getAvailableDomainTypes());
            Optional<DomainType> selectedType = view.askForDomainType(service.getAvailableDomainTypes());

            // EXIT CONDITION: The user chose option "0" (Quit).
            if (selectedType.isEmpty()) {
                return Optional.empty();
            }

            // ==================================================================================
            // STEP 2: PARAMETER ACQUISITION
            // ==================================================================================
            DomainType type = selectedType.get();
            // The View handles data type conversion (Scanner -> Double) and basic syntactic validation (positive numbers).
            Map<String, Double> params = view.askForParameters(type);

            // ==================================================================================
            // STEP 3: CREATION & DEEP PROTECTION
            // ==================================================================================
            try {
                // Attempts to create the domain via the Service.
                // This is where "Deep Protection" kicks in: the Factory/Model constructors will
                // validate the consistency of the parameters (e.g., innerRadius < outerRadius).
                Domain domain = service.createDomain(type, params);

                view.showSuccessMessage();
                return Optional.of(domain);

            } catch (DomainConstraintException e) {
                // 🛑 CASE A: SEMANTIC ERROR (Business Logic)
                // The input was syntactically correct (numbers), but logically invalid for the specific Domain
                // (e.g., Inner Radius >= Outer Radius).
                // Action: Show the specific error message defined in the Domain model.
                view.showErrorMessage(e.getMessage());

            } catch (InvalidInputException e) {
                // 🛑 CASE B: VALIDATION ERROR (Data Integrity)
                // Missing parameters, null values, or generic input errors caught by the Service/Factory.
                view.showErrorMessage("Input Error: " + e.getMessage());

            } catch (Exception e) {
                // 🛑 CASE C: UNEXPECTED SYSTEM ERROR (Safety Net)
                // Catches bugs like NullPointerException or OutOfMemoryError.
                // Prevents the application from crashing entirely, allowing the user to retry or exit safely.
                view.showErrorMessage("Unexpected System Error: " + e.getMessage());
                // e.printStackTrace(); // For debugging purposes only
            }

            // LOOP: If an exception occurred, the 'while' loop restarts, allowing the user to correct the input.
        }
    }
}
