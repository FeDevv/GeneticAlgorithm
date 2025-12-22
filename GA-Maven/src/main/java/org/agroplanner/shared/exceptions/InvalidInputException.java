package org.agroplanner.shared.exceptions;

/**
 * Indicates that provided input data violates domain constraints, format requirements,
 * or logical preconditions.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Defensive Programming:</strong> This exception implements the <em>Fail-Fast</em> principle.
 * It prevents the system from entering an inconsistent state by rejecting malformed data immediately
 * upon detection.</li>
 * <li><strong>Deep Protection:</strong> Used within Domain Model constructors to enforce class invariants
 * (e.g., ensuring geometric dimensions are positive) and within Controllers to validate raw user input
 * before passing it to the Service Layer.</li>
 * </ul>
 *
 * @see TerrainExceptions
 */
public class InvalidInputException extends TerrainExceptions {

    /**
     * Constructs the exception with a specific message detailing the constraint violation.
     *
     * @param message A description of the specific validation rule that failed
     * (e.g., "Radius must be positive", "Filename cannot be empty").
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
