package org.agroplanner.shared.exceptions;

// Eccezione generica per input non validi (Configurazioni, Parametri)
public class InvalidInputException extends TerrainExceptions {
    public InvalidInputException(String message) {
        super(message);
    }
}
