package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * <p><strong>View Contract for the Domain Creation Subsystem.</strong></p>
 *
 * <p>This interface defines the required interactions between the User and the System
 * regarding the selection and configuration of the problem domain.
 * By using this interface, the {@code DomainConsoleController} remains decoupled from the specific
 * UI implementation (Console, JavaFX, Web, etc.).</p>
 */
public interface DomainViewContract {

    /**
     * Prompts the user to select a geometric domain type from a provided list.
     *
     * @param types The list of available {@link DomainType} options supported by the system.
     * @return An {@link Optional} containing the selected type, or {@code Optional.empty()}
     * if the user chooses to exit or cancel the operation.
     */
    Optional<DomainType> askForDomainType(List<DomainType> types);

    /**
     * Collects the specific configuration parameters required for the selected domain.
     * <p>
     * <strong>Dynamic UI Generation:</strong> The View should use the metadata provided by
     * {@link DomainType#getRequiredParameters()} to dynamically query the user for inputs
     * (e.g., "Enter radius" for Circle, "Enter width" for Rectangle).
     * </p>
     *
     * @param type The domain type for which parameters are needed.
     * @return A map where keys are the parameter names (e.g., "width") and values are the user inputs.
     */
    Map<String, Double> askForParameters(DomainType type);

    /**
     * Displays a success feedback message indicating the domain has been created.
     */
    void showSuccessMessage();

    /**
     * Displays an error message to the user.
     * <p>
     * Used to communicate validation errors (e.g., negative radius) or system exceptions
     * without crashing the application flow.
     * </p>
     *
     * @param message The detailed error description to display.
     */
    void showErrorMessage(String message);
}
