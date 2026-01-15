package org.agroplanner.persistence.implementations.gasystem;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.SolutionMetadata;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

/**
 * Volatile implementation.
 * <p>Persistence is disabled in this mode to enforce the Demo/Guest policy.</p>
 */
public class MemorySolutionDAO implements SolutionDAOContract {
    @Override
    public void initStorage() { /* No-op */ }

    @Override
    public boolean saveSolution(Individual solution, User owner, String title, DomainDefinition domainDef) {
        throw new UnsupportedOperationException("Not available in Demo Mode");
    }

    @Override
    public List<SolutionMetadata> findByUser(User user) {
        return Collections.emptyList();
    }

    @Override
    public Optional<LoadedSession> loadSolution(int solutionId) {
        return Optional.empty();
    }
}
