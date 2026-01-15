package org.agroplanner.persistence.controllers;

import org.agroplanner.boot.model.AgroConfiguration;
import org.agroplanner.boot.model.PersistenceType;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;
import org.agroplanner.persistence.factories.FilePersistenceFactory;
import org.agroplanner.persistence.factories.MemoryPersistenceFactory;
import org.agroplanner.persistence.factories.SqlPersistenceFactory;

/**
 * Service Singleton responsible for bootstrapping the Persistence Layer.
 * <p>
 * This class orchestrates the initialization of the specific storage strategy
 * (SQL, File, or Memory) based on the application configuration.
 * </p>
 */
public class PersistenceService {

    private AgroPersistenceFactory activeFactory;
    private boolean isInitialized = false;

    private PersistenceService() {
        // Enforce non-instantiability
    }

    // --- BILL PUGH SINGLETON ---
    private static class LazyHolder {
        private static final PersistenceService INSTANCE = new PersistenceService();
    }

    public static PersistenceService getInstance() {
        return LazyHolder.INSTANCE;
    }
    // ---------------------------

    /**
     * Bootstraps the persistence engine.
     * <p>
     * This method configures the {@link DBConnection} (if needed) and instantiates
     * the appropriate Concrete Factory.
     * </p>
     *
     * @param config The global system configuration.
     * @throws IllegalArgumentException If the persistence type is unknown.
     */
    public void initialize(AgroConfiguration config) {
        if (isInitialized) {
            return; // Idempotent operation: do nothing if already active.
        }

        PersistenceType type = config.getPersistenceType();

        // 1. Configure the DB Connection Singleton (Logic only, no strings attached)
        DBConnection.getInstance().configure(type);

        // 2. Instantiate the Concrete Factory
        switch (type) {
            case DATABASE -> this.activeFactory = new SqlPersistenceFactory();
            case FILESYSTEM -> this.activeFactory = new FilePersistenceFactory();
            case MEMORY -> this.activeFactory = new MemoryPersistenceFactory();
            default -> throw new IllegalArgumentException("Unknown Persistence Type: " + type);
        }

        this.isInitialized = true;
    }

    /**
     * Retrieves the active abstract factory.
     *
     * @return The configured factory instance.
     * @throws IllegalStateException If called before initialization.
     */
    public AgroPersistenceFactory getFactory() {
        if (!isInitialized) {
            throw new IllegalStateException("CRITICAL: PersistenceService has not been initialized.");
        }
        return activeFactory;
    }
}
