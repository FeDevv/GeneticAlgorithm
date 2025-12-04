package org.agroplanner.shared.exceptions;

public abstract class TerrainExceptions extends RuntimeException {
    protected TerrainExceptions(String message) {
        super(message);
    }

    protected TerrainExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
