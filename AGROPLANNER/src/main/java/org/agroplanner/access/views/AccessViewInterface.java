package org.agroplanner.access.views;

import org.agroplanner.access.model.CredentialsDTO;

/**
 * Defines the contract for the Access User Interface.
 * <p>
 * Ensures the Controller remains agnostic of the specific UI implementation (Console vs GUI),
 * adhering to the Dependency Inversion Principle.
 * </p>
 */
public interface AccessViewInterface {

    int askInitialChoice();
    CredentialsDTO askLoginDetails();
    CredentialsDTO askRegistrationDetails();
    void showAgronomistValidationSequence();
    void showErrorMessage(String message);
    void showSuccessMessage(String message);
    void showWelcomeMessage();
}
