package org.agroplanner.shared.exceptions;

public abstract class TerrainExceptions extends RuntimeException {
    public TerrainExceptions(String message) {
        super(message);
    }

    public TerrainExceptions(String message, Throwable cause) {
        super(message, cause);
    }
}
