package org.agroplanner.gasystem.dao;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.SolutionMetadata;

import java.util.List;
import java.util.Optional;

public interface SolutionDAOContract {

    /**
     * Prepara lo storage (Crea tabelle SQL o cartelle su disco).
     */
    void initStorage();

    /**
     * Salva una soluzione completa.
     *
     * @param solution L'individuo risultato dell'ottimizzazione (contiene i punti).
     * @param owner    L'utente che ha generato la soluzione.
     * @param title    Un titolo identificativo scelto dall'utente.
     * @return true se il salvataggio ha successo.
     */
    boolean saveSolution(Individual solution, User owner, String title, DomainDefinition domainDef);

    List<SolutionMetadata> findByUser(User user);

    Optional<LoadedSession> loadSolution(int solutionId);
}
