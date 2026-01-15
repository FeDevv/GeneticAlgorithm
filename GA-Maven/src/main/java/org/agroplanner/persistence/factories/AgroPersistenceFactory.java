package org.agroplanner.persistence.factories;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;

/**
 * Abstract Factory defining the contract for the Persistence Layer family.
 * <p>
 * Ensures that all Data Access Objects (DAOs) created belong to the same storage
 * mechanism (SQL, File System, or Memory), maintaining consistency across the application.
 * </p>
 */
public abstract class AgroPersistenceFactory {

    private final UserDAOContract userDAO;
    private final PlantVarietyDAOContract plantDAO;
    private final SolutionDAOContract solutionDAO;
    private final DomainDAOContract domainDAO;

    /**
     * Initializes the factory and instantiates all DAOs.
     * <p>
     * <strong>Note:</strong> The initialization order is critical. {@code createDomainDAO()}
     * is called before {@code createSolutionDAO()} because the SQL implementation of the latter
     * depends on the former.
     * </p>
     */
    public AgroPersistenceFactory() {
        this.userDAO = createUserDAO();
        this.plantDAO = createPlantDAO();
        this.domainDAO = createDomainDAO();
        this.solutionDAO = createSolutionDAO();
    }

    // Getters
    public UserDAOContract getUserDAO() { return userDAO; }
    public PlantVarietyDAOContract getPlantDAO() { return plantDAO; }
    public SolutionDAOContract getSolutionDAO() { return solutionDAO; }
    public DomainDAOContract getDomainDAO() { return domainDAO; }

    // --- FACTORY METHODS  ---
    protected abstract UserDAOContract createUserDAO();
    protected abstract PlantVarietyDAOContract createPlantDAO();
    protected abstract SolutionDAOContract createSolutionDAO();
    protected abstract DomainDAOContract createDomainDAO();

    /**
     * Indicates whether the storage mechanism is volatile (lost on shutdown).
     * @return {@code true} if data is stored in memory, {@code false} if persistent.
     */
    public abstract boolean isVolatile();
}
