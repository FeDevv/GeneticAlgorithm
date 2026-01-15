package org.agroplanner.domainsystem.dao;

import org.agroplanner.domainsystem.model.DomainDefinition;

/**
 * Persistence contract for saving and loading Domain configurations.
 */
public interface DomainDAOContract {

    /**
     * Initializes the underlying storage (File/DB).
     */
    void initStorage();

    /**
     * Persists a domain definition.
     * @return The ID of the saved entity.
     */
    int save(DomainDefinition domainDef);

    /**
     * Loads a domain definition by ID.
     */
    DomainDefinition load(int id);
}
