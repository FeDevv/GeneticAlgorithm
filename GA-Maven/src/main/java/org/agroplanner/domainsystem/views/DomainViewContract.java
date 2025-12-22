package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Defines the abstract contract for user interactions regarding the configuration of geometric domains.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> View Interface (Dependency Inversion Principle).</li>
 * <li><strong>Role:</strong> Decouples the {@link org.agroplanner.domainsystem.controllers.DomainConsoleController}
 * from specific UI technologies. The controller interacts purely with this contract, remaining unaware
 * of whether the input comes from a CLI Scanner, a GUI Form, or a Web Request.</li>
 * <li><strong>Dynamic Adaptation:</strong> Supports a <strong>Data-Driven UI</strong> approach. Methods in this
 * contract allow for the generation of input prompts based on metadata (schema) rather than hard-coded fields.</li>
 * </ul>
 */
public interface DomainViewContract {

    /**
     * Renders the catalog of supported geometric shapes to the user.
     *
     * @param types The list of available domain constants to display.
     */
    void showAvailableDomains(List<DomainType> types);

    /**
     * Captures and interprets the user's selection for a specific geometric shape.
     *
     * <p><strong>Contract:</strong></p>
     * The implementation must handle input validation (e.g., ensuring the selected ID exists).
     * It returns an {@link Optional} to explicitly model the "Exit/Cancel" scenario, allowing the
     * Controller to terminate the flow gracefully.
     *
     * @param types The context list of valid options.
     * @return An {@link Optional} containing the selected {@link DomainType},
     * or {@code Optional.empty()} if the user requests to abort the operation.
     */
    Optional<DomainType> askForDomainType(List<DomainType> types);

    /**
     * Dynamically collects the configuration values required to instantiate the selected domain.
     *
     * <p><strong>Dynamic Form Generation:</strong></p>
     * The implementation is expected to iterate over the keys provided by {@link DomainType#getRequiredParameters()}.
     * For each parameter key (e.g., "radius", "width"), it should generate a specific prompt, validate the input
     * (ensure it's a number), and populate the result map.
     * <br>
     * This design prevents the proliferation of shape-specific methods (like {@code askForCircleRadius()},
     * {@code askForRectangleWidth()}) in the interface.
     *
     * @param type The domain schema defining which parameters are needed.
     * @return A {@link Map} mapping parameter names to their user-provided numerical values.
     */
    Map<String, Double> askForParameters(DomainType type);

    /**
     * Provides visual feedback confirming the successful creation of the domain entity.
     */
    void showSuccessMessage();

    /**
     * Renders error states or validation failures to the user.
     *
     * <p><strong>Usage:</strong></p>
     * Invoked by the Controller when catching exceptions from the Model layer (e.g., Invalid Input,
     * Geometric Constraints). Allows the application to provide feedback without terminating the session.
     *
     * @param message The detailed error description to display.
     */
    void showErrorMessage(String message);
}
