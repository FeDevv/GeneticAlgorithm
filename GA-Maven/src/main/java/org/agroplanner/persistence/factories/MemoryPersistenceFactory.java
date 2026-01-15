package org.agroplanner.persistence.factories;

import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.persistence.implementations.access.MemoryUserDAO;
import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.persistence.implementations.domainsystem.MemoryDomainDAO;
import org.agroplanner.persistence.implementations.domainsystem.SqlDomainDAO;
import org.agroplanner.persistence.implementations.gasystem.MemorySolutionDAO;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.persistence.implementations.inventory.MemoryPlantVarietyDAO;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;

public class MemoryPersistenceFactory extends AgroPersistenceFactory {

    @Override
    protected UserDAOContract createUserDAO() {
        return new MemoryUserDAO();
    }

    @Override
    protected PlantVarietyDAOContract createPlantDAO() {
        return new MemoryPlantVarietyDAO();
    }

    // NUOVO
    @Override
    protected SolutionDAOContract createSolutionDAO() {
        return new MemorySolutionDAO();
    }

    @Override
    public boolean isVolatile() {
        return true;
    }

    @Override
    protected DomainDAOContract createDomainDAO() {
        return new MemoryDomainDAO();
    }
}
