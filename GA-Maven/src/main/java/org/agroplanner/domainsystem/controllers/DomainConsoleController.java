package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.domainsystem.views.DomainViewContract;

import java.util.Map;
import java.util.Optional;

/**
 * Controller component orchestrating the domain initialization workflow within the CLI environment.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> MVC Controller. It acts as the mediator between the user interface
 * ({@link DomainViewContract}) and the business logic ({@link DomainService}).</li>
 * <li><strong>Responsibility:</strong> Manages the session state during the configuration phase,
 * enforcing the execution order (Selection -> Parameter Input -> Instantiation).</li>
 * <li><strong>Fault Tolerance:</strong> Implements a <em>Retry Loop</em> strategy. Instead of terminating upon
 * validation errors, the controller catches domain-specific exceptions and cycles the workflow,
 * allowing the user to correct their input dynamically.</li>
 * </ul>
 */
public class DomainConsoleController {

    /**
     * Abstraction of the User Interface.
     */
    private final DomainViewContract view;

    /**
     * Facade for Domain Business Logic.
     */
    private final DomainService service;

    /**
     * Constructs the controller injecting required dependencies.
     *
     * @param view    The IO handler implementation.
     * @param service The domain logic facade.
     */
    public DomainConsoleController(DomainViewContract view, DomainService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Executes the synchronous wizard for geometric domain creation.
     *
     * <p><strong>Workflow:</strong></p>
     * <ol>
     * <li><strong>Type Selection:</strong> Fetches available {@link DomainType}s from the Service and prompts the user via the View.</li>
     * <li><strong>Data Acquisition:</strong> Requests the specific parameters defined by the selected type's schema.</li>
     * <li><strong>Instantiation:</strong> Invokes the Service to create the entity. This triggers the "Deep Protection" validation chain.</li>
     * </ol>
     *
     * <p><strong>Error Handling Strategy:</strong></p>
     * Differentiates between error categories to provide specific feedback:
     * <ul>
     * <li>{@link DomainConstraintException}: Semantic errors (e.g., "Inner Radius > Outer Radius"). Logic is rejected.</li>
     * <li>{@link InvalidInputException}: Structural errors (e.g., Missing keys). Data is rejected.</li>
     * <li>{@link Exception}: Unexpected system failures. Caught to prevent JVM crash (Graceful Degradation).</li>
     * </ul>
     *
     * @return an {@link Optional} containing the valid {@link Domain} instance,
     * or {@code Optional.empty()} if the user cancels the operation (Exit condition).
     */
    public Optional<Domain> runDomainCreationWizard() {
        while (true) {
            // ==================================================================================
            // STEP 1: TYPE SELECTION
            // ==================================================================================
            // Data Flow: Service (Data) -> Controller -> View (Render)
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
            // The View handles low-level parsing (String -> Double)
            Map<String, Double> params = view.askForParameters(type);

            // ==================================================================================
            // STEP 3: CREATION & VALIDATION (The "Deep Protection" Boundary)
            // ==================================================================================
            try {
                // Delegation: The Service orchestrates the Factory, which invokes Constructors.
                // Any violation of Physical or Topological invariants throws an exception here.
                Domain domain = service.createDomain(type, params);

                view.showSuccessMessage();
                return Optional.of(domain);

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
