package org.agroplanner.persistence.factories;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.persistence.implementations.access.SqlUserDAO;
import org.agroplanner.persistence.implementations.domainsystem.SqlDomainDAO;
import org.agroplanner.persistence.implementations.gasystem.SqlSolutionDAO;
import org.agroplanner.persistence.implementations.inventory.SqlPlantVarietyDAO;

public class SqlPersistenceFactory extends AgroPersistenceFactory {

    @Override
    protected UserDAOContract createUserDAO() {
        return new SqlUserDAO();
    }

    @Override
    protected PlantVarietyDAOContract createPlantDAO() {
        return new SqlPlantVarietyDAO();
    }

    @Override
    protected SolutionDAOContract createSolutionDAO() {
        DomainDAOContract domainDAO = getDomainDAO();
        // Dependency Injection: SQL Solution DAO needs access to Domain table handling
        return new SqlSolutionDAO(domainDAO);
    }

    @Override
    public boolean isVolatile() {
        return false;
    }

    @Override
    protected DomainDAOContract createDomainDAO() {
        return new SqlDomainDAO();
    }
}
