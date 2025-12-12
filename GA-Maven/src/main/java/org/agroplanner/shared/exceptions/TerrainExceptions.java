package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Root of the Custom Exception Hierarchy.</strong></p>
 *
 * <p>This abstract class serves as the base type for all domain-specific errors generated
 * within the Terrain Optimizer application. By catching {@code TerrainExceptions},
 * the main controllers can distinguish between expected application errors (validation, constraints)
 * and unexpected system crashes (NPE, OOM).</p>
 *
 * <p><strong>Architecture:</strong> Unchecked Exception (RuntimeException).<br>
 * This avoids boiler-plate {@code throws} declarations in method signatures, allowing
 * errors to bubble up to the UI layer naturally.</p>
 */
public abstract class TerrainExceptions extends RuntimeException {
    /**
     * Constructs a new exception with the specified detail message.
     * @param message The error message.
     */
    protected TerrainExceptions(String message) {
        super(message);
    }

    /**
     * Constructs a new exception with the specified detail message and cause.
     * @param message The error message.
     * @param cause   The underlying cause (e.g., a low-level IOException).
     */
    protected TerrainExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
