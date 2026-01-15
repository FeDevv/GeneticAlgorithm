package org.agroplanner.domainsystem.model;

import java.util.Map;

/**
 * DTO (Data Transfer Object) holding the raw configuration for a domain.
 * Used for persistence serialization/deserialization.
 */
public class DomainDefinition {
    private final DomainType type;
    private final Map<String, Double> parameters;

    public DomainDefinition(DomainType type, Map<String, Double> parameters) {
        this.type = type;
        this.parameters = parameters;
    }

    public DomainType getType() { return type; }
    public Map<String, Double> getParameters() { return parameters; }
}
