package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Exception: Convergence Failure.</strong></p>
 *
 * <p>Thrown when an iterative process (such as the Evolutionary Algorithm's retry loop)
 * exhausts its maximum allowed attempts without producing a valid result.</p>
 *
 * <p><strong>Use Case:</strong>
 * Used by the {@code EvolutionConsoleController} to signal that despite multiple restarts,
 * the algorithm could not solve the geometric constraints (likely due to an overcrowded domain).</p>
 */
public class MaxAttemptsExceededException extends TerrainExceptions {
    /**
     * Constructs the exception with a detail message.
     * @param message Detailed explanation of the failure.
     */
    public MaxAttemptsExceededException(String message) {
        super(message);
    }
}
