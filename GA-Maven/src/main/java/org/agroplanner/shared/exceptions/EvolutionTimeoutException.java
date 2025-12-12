package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Exception: Time Limit Exceeded.</strong></p>
 *
 * <p>Thrown when the Evolutionary Algorithm exceeds its maximum allowed execution time
 * for a single run (Time Budget Exhausted).</p>
 *
 * <p><strong>Use Case:</strong>
 * This exception prevents the application from hanging indefinitely on extremely complex
 * optimization problems. It allows the controller to abort the current attempt gracefully
 * and return control to the user.</p>
 */
public class EvolutionTimeoutException extends TerrainExceptions {

    /**
     * Constructs the exception with the timeout details.
     * @param message The message usually containing the time limit that was breached (e.g., "Execution exceeded 60s").
     */
    public EvolutionTimeoutException(String message) {
        super(message);
    }
}
