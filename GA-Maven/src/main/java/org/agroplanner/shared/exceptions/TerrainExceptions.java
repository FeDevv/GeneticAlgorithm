package org.agroplanner.shared.exceptions;

/**
 * Abstract base class for the entire exception hierarchy within the AgroPlanner domain.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Implements the <em>Exception Translation</em> idiom. Low-level technical errors
 * (e.g., I/O failures) are captured by the Service Layer and wrapped into specific subclasses of this
 * exception to maintain the abstraction level of the Domain Model.</li>
 * <li><strong>Type:</strong> Extends {@link RuntimeException} (Unchecked). This follows <em>Clean Code</em>
 * principles to avoid polluting method signatures with checked exceptions and allows errors to
 * "bubble up" naturally to the Main Controller (Global Error Handler).</li>
 * </ul>
 *
 * <p><strong>Usage:</strong></p>
 * Subclasses should be granular enough to allow the Controller to distinguish between:
 * <ul>
 * <li><em>Recoverable Errors:</em> (e.g., {@code InvalidInputException}) handled via retry loops.</li>
 * <li><em>Fatal Errors:</em> (e.g., {@code EvolutionTimeoutException}) requiring a session reset.</li>
 * </ul>
 *
 * @see RuntimeException
 */
public abstract class TerrainExceptions extends RuntimeException {

    /**
     * Constructs a new domain exception with the specified detail message.
     *
     * @param message The detail message, saved for later retrieval by the {@link #getMessage()} method.
     * Should contain business-relevant information.
     */
    protected TerrainExceptions(String message) {
        super(message);
    }

    /**
     * Constructs a new domain exception with the specified detail message and cause.
     * <p>
     * This constructor is crucial for the <em>Exception Chaining</em> mechanism, preserving the
     * stack trace of the original low-level error (the "cause") while presenting a high-level
     * abstraction to the caller.
     * </p>
     *
     * @param message The detail message.
     * @param cause   The underlying cause (e.g., a generic {@link java.io.IOException} or {@link java.lang.IllegalArgumentException}).
     */
    protected TerrainExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
