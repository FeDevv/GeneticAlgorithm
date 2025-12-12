package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Exception: Persistence/Storage Failure.</strong></p>
 *
 * <p>Thrown when the system fails to write data to an external storage medium (disk).
 * This typically wraps lower-level I/O exceptions.</p>
 *
 * <p><strong>Architectural Role (Exception Translation):</strong><br>
 * By catching {@code java.io.IOException} in the Service layer and re-throwing this exception,
 * we prevent implementation details (like file system errors) from leaking into the Controller layer.
 * The Controller only needs to know <em>that</em> the export failed, not necessarily <em>why</em> (at a low level).</p>
 */
public class ExportException extends TerrainExceptions {

    /**
     * Constructs the exception with a detailed error message.
     * @param message The explanation of the failure (e.g., "Disk full", "Permission denied").
     */
    public ExportException(String message) {
        super(message);
    }

    /**
     * Constructs the exception with a message and the original cause.
     * <p>
     * Passing the {@code cause} ensures that the
     * full stack trace of the original {@code IOException} is preserved.
     * </p>
     *
     * @param message The context message.
     * @param cause   The original low-level exception (e.g., {@link java.io.IOException}).
     */
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
