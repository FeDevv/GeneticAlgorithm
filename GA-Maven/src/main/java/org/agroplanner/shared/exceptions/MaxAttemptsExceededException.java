package org.agroplanner.shared.exceptions;

/**
 * Thrown when an iterative algorithm reaches its maximum allowed iterations
 * without converging to a result.
 */
public class MaxAttemptsExceededException extends AgroPlannerException {

    public MaxAttemptsExceededException(String message) {
        super(message);
    }
}
