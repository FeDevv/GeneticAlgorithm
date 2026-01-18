package org.agroplanner.shared.exceptions;

/**
 * Serves as the abstract base class for all runtime exceptions specific to the AgroPlanner domain.
 * <p>
 * By extending {@link RuntimeException}, this class allows for unchecked exception propagation,
 * enabling a centralized handling strategy in the presentation layer.
 * </p>
 */
public abstract class AgroPlannerException extends RuntimeException {

    /**
     * Initializes a new instance with the specified detail message.
     *
     * @param message The detail message intended for logging or user feedback.
     */
    protected AgroPlannerException(String message) {
        super(message);
    }

    /**
     * Initializes a new instance with the specified detail message and the underlying cause.
     *
     * @param message The detail message.
     * @param cause   The throwable that caused this exception to be thrown.
     */
    protected AgroPlannerException(String message, Throwable cause) {
        super(message, cause);
    }
}
