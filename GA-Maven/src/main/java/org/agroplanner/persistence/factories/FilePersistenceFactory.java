package org.agroplanner.persistence.factories;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.persistence.implementations.access.FileUserDAO;
import org.agroplanner.persistence.implementations.domainsystem.FileDomainDAO;
import org.agroplanner.persistence.implementations.gasystem.FileSolutionDAO;
import org.agroplanner.persistence.implementations.inventory.FilePlantVarietyDAO;

public class FilePersistenceFactory extends AgroPersistenceFactory {

    @Override
    protected UserDAOContract createUserDAO() {
        return new FileUserDAO();
    }

    @Override
    protected PlantVarietyDAOContract createPlantDAO() {
        return new FilePlantVarietyDAO();
    }

    @Override
    protected SolutionDAOContract createSolutionDAO() {
        return new FileSolutionDAO();
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    protected DomainDAOContract createDomainDAO() {
        return new FileDomainDAO();
    }
}
