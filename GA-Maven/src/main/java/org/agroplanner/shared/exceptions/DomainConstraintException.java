package org.agroplanner.shared.exceptions;

// Specifica per i vincoli del dominio (es. larghezza negativa)
public class DomainConstraintException extends TerrainExceptions {
    // Costruttore per errori su un singolo parametro
    public DomainConstraintException(String paramName, String reason) {
        super(String.format("Invalid parameter '%s': %s", paramName, reason));
    }

    // Costruttore per errori generici o relazionali
    public DomainConstraintException(String message) {
        super(message);
    }
}
