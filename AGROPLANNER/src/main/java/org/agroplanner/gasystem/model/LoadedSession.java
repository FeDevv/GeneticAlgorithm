package org.agroplanner.gasystem.model;

import org.agroplanner.domainsystem.model.DomainDefinition;

/**
 * Wrapper che rappresenta una sessione completa recuperata dallo storage.
 */
public class LoadedSession {
    private final Individual solution;
    private final DomainDefinition domainDefinition;

    public LoadedSession(Individual solution, DomainDefinition domainDefinition) {
        this.solution = solution;
        this.domainDefinition = domainDefinition;
    }

    public Individual getSolution() { return solution; }
    public DomainDefinition getDomainDefinition() { return domainDefinition; }
}
