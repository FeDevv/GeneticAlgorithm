package org.agroplanner.access.controllers;

import org.agroplanner.access.model.CredentialsDTO;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.access.views.AccessViewInterface;
import org.agroplanner.shared.exceptions.DataPersistenceException;
import org.agroplanner.shared.exceptions.DuplicateUserException;
import org.agroplanner.shared.exceptions.InvalidInputException;

/**
 * Controller orchestrating the authentication and registration workflows.
 * <p>
 * Implements the main entry point for user access, mediating interaction between
 * the presentation layer (View) and the domain logic (Service).
 * </p>
 */
public class AccessConsoleController {

    private final AccessViewInterface view;
    private final AccessService service;

    /**
     * Initializes the controller with required dependencies.
     *
     * @param view    The UI contract implementation.
     * @param service The business logic provider for authentication.
     */
    public AccessConsoleController(AccessViewInterface view, AccessService service) {
        this.view = view;
        this.service = service;
    }

    /**
     * Executes the Access Module workflow loop.
     * <p>
     * Keeps the user in the authentication menu until a valid login occurs or the exit command is issued.
     * </p>
     *
     * @return The authenticated {@link User} object, or {@code null} if the application exit was requested.
     */
    public User run() {
        view.showWelcomeMessage();

        while (true) {
            int choice = view.askInitialChoice();

            switch (choice) {
                case 0: return null; // Exit Application
                case 1: return handleLogin();
                case 2: handleRegistration(); break;
                default: view.showErrorMessage("Invalid choice.");
            }
        }
    }

    /**
     * Orchestrates the login sequence.
     *
     * @return The User entity if authentication succeeds, {@code null} otherwise.
     */
    private User handleLogin() {
        try {
            CredentialsDTO creds = view.askLoginDetails();
            User user = service.login(creds.getUsername(), creds.getPassword());

            if (user != null) {
                view.showSuccessMessage("Welcome back, " + user.getFullName());
                return user;
            } else {
                view.showErrorMessage("Invalid credentials.");
            }
        } catch (DataPersistenceException e) {
            view.showErrorMessage("System Error: " + e.getMessage());
        }
        return null;
    }

    /**
     * Orchestrates the registration sequence.
     */
    private void handleRegistration() {
        CredentialsDTO dto = view.askRegistrationDetails();

        // Specific UI workflow for Agronomists (Simulation)
        if (dto.getRequestedRole() == Role.AGRONOMIST) {
            view.showAgronomistValidationSequence();
        }

        try {
            // passing DTO
            service.register(dto);
            view.showSuccessMessage("Registration successful! Please login.");

        } catch (InvalidInputException e) {
            view.showErrorMessage("Validation: " + e.getMessage());
        } catch (DuplicateUserException e) {
            view.showErrorMessage("Error: " + e.getMessage());
        } catch (DataPersistenceException e) {
            view.showErrorMessage("Database Error: " + e.getMessage());
        }
    }
}
