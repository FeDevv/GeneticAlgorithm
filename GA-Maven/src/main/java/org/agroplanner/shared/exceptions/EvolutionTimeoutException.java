package org.agroplanner.shared.exceptions;

/**
 * Indicates that a computational process has exceeded its allocated execution time window.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Implements the <em>Time-Boxing</em> strategy.</li>
 * <li><strong>System Stability:</strong> Genetic Algorithms are computationally intensive. This exception acts as a
 * <strong>Failsafe Mechanism</strong> to prevent the application from becoming unresponsive (freezing)
 * during complex optimization tasks. It enforces a deterministic upper bound on execution duration,
 * regardless of whether a solution has been found.</li>
 * </ul>
 *
 * @see TerrainExceptions
 */
public class EvolutionTimeoutException extends TerrainExceptions {

    /**
     * Constructs the exception with a message detailing the time constraint violation.
     *
     * @param message A description of the timeout event (e.g., specifying the duration limit that was breached).
     */
    public EvolutionTimeoutException(String message) {
        super(message);
    }
}
