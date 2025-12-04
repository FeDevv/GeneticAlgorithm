package org.agroplanner.shared.exceptions;

public class ExportException extends TerrainExceptions {
    public ExportException(String message) {
        super(message);
    }

    // Fondamentale: accetta la 'cause' (l'IOException originale)
    // così non perdiamo lo stack trace originale per il debug.
    public ExportException(String message, Throwable cause) {
        super(message, cause);
    }
}
