package org.agroplanner.shared.exceptions;

/**
 * Thrown when input data fails to meet format requirements or validation preconditions.
 */
public class InvalidInputException extends AgroPlannerException {

    public InvalidInputException(String message) {
        super(message);
    }
}
