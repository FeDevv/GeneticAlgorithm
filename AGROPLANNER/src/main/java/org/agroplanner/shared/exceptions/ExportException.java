package org.agroplanner.shared.exceptions;

/**
 * Thrown when the data export process fails.
 * <p>
 * This exception typically wraps I/O errors occurring during file generation or writing.
 * </p>
 */
public class ExportException extends AgroPlannerException {

    public ExportException(String message) {
        super(message);
    }

    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
