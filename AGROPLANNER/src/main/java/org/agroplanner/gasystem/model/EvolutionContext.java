package org.agroplanner.gasystem.model;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.controllers.EvolutionService;
import org.agroplanner.persistence.factories.AgroPersistenceFactory;

import java.util.function.Consumer;

public record EvolutionContext(
        EvolutionService service,
        Domain domain,
        DomainDefinition domainDef,
        AgroPersistenceFactory factory,
        User user,
        boolean isDemoMode,
        Individual loadedSolution,
        Consumer<Individual> onExportRequested,
        Runnable onExit
) {}
