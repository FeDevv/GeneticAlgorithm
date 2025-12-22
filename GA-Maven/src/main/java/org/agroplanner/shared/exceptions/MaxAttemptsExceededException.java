package org.agroplanner.shared.exceptions;

/**
 * Indicates that an iterative process has exhausted its maximum allowed execution attempts
 * without producing a valid result.
 *
 * <p><strong>Domain Context:</strong></p>
 * Genetic and evolutionary algorithms are stochastic in nature. This exception enforces
 * a termination condition to prevent infinite loops when a solution cannot be found
 * within the defined constraints.
 *
 * @see TerrainExceptions
 */
public class MaxAttemptsExceededException extends TerrainExceptions {

    /**
     * Constructs a new exception indicating the retry limit has been reached.
     *
     * @param message A description of the failure context (e.g., specific operation and limit reached).
     */
    public MaxAttemptsExceededException(String message) {
        super(message);
    }
}
