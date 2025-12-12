package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Exception: Input Validation Failure.</strong></p>
 *
 * <p>Thrown when user-provided data fails to meet basic syntactic or logical requirements.
 * This is part of the "Deep Protection" layer, ensuring that services never process
 * obviously malformed data.</p>
 *
 * <p><strong>Examples:</strong>
 * <ul>
 * <li>Negative radius or population size.</li>
 * <li>Empty filename string.</li>
 * <li>Selection of a non-existent menu ID.</li>
 * </ul>
 * </p>
 */
public class InvalidInputException extends TerrainExceptions {

    /**
     * Constructs the exception with a specific error message describing the invalid input.
     * @param message The explanation of what went wrong.
     */
    public InvalidInputException(String message) {
        super(message);
    }
}
