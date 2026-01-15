package org.agroplanner.shared.exceptions;

/**
 * Thrown when an operation involving the persistence layer fails.
 * <p>
 * This exception wraps lower-level errors (e.g., I/O failures, database connection issues)
 * encountered during data retrieval or storage.
 * </p>
 */
public class DataPersistenceException extends AgroPlannerException {

    public DataPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }

    public DataPersistenceException(String message) {
        super(message);
    }
}
