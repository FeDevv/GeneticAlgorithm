package org.agroplanner.shared.exceptions;

/**
 * Thrown when an attempt is made to register or update an entity using a unique identifier
 * that is already present in the system repository.
 */
public class DuplicateUserException extends AgroPlannerException {

    /**
     * Initializes the exception for a specific conflicting username.
     *
     * @param username The username that caused the conflict.
     */
    public DuplicateUserException(String username) {
        super("The username '" + username + "' is already in use.");
    }
}
