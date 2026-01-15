package org.agroplanner.shared.exceptions;

/**
 * Thrown when a computational evolution process exceeds its allocated time limit.
 */
public class EvolutionTimeoutException extends AgroPlannerException {

    public EvolutionTimeoutException(String message) {
        super(message);
    }
}
