package org.agroplanner.persistence.implementations.domainsystem;

import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.domainsystem.model.DomainDefinition;

/**
 * Volatile implementation.
 * <p>
 * This implementation intentionally disables persistence capabilities.
 * Any attempt to save data will result in an exception, enforcing the "Guest" mode restrictions.
 * </p>
 */
public class MemoryDomainDAO implements DomainDAOContract {
    @Override
    public void initStorage() { /* No-op for memory storage */ }

    /**
     * @throws UnsupportedOperationException Always, as persistence is disabled in this mode.
     */
    @Override
    public int save(DomainDefinition domainDef) {
        throw new UnsupportedOperationException("Not available in Demo Mode");
    }

    @Override
    public DomainDefinition load(int id) {
        return null;
    }
}
