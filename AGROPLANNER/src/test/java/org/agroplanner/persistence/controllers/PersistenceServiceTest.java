package org.agroplanner.persistence.controllers;

import org.agroplanner.boot.model.AgroConfiguration;
import org.agroplanner.boot.model.PersistenceType;
import org.agroplanner.persistence.factories.MemoryPersistenceFactory;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.*;

// By Federico Bonucci

/**
 * Unit tests for the {@link PersistenceService} singleton controller.
 * <p>
 * This class validates the bootstrapping logic of the persistence layer, ensuring that
 * the correct concrete factory is instantiated based on the provided system configuration.
 * Due to the Singleton nature of the service, tests utilize Java Reflection to enforce
 * state isolation between executions.
 * </p>
 */
class PersistenceServiceTest {

    /**
     * Verifies that the service correctly initializes the {@link MemoryPersistenceFactory}
     * when the configuration explicitly requests the {@code MEMORY} persistence strategy.
     * <p>
     * This test ensures that the internal switch-case logic maps the configuration enum
     * to the correct concrete implementation of the Abstract Factory.
     * </p>
     *
     * @throws Exception if reflection operations fail during state reset.
     */
    @Test
    void shouldInitializeMemoryFactory_WhenConfiguredForMemory() throws Exception {

        // Accesses the Singleton instance.
        PersistenceService instance = PersistenceService.getInstance();

        // resets the 'isInitialized' flag to false.
        // This is necessary to bypass the idempotency guard clause, allowing the service
        // to be re-initialized within the test context.
        Field initField = PersistenceService.class.getDeclaredField("isInitialized");
        initField.setAccessible(true);
        initField.setBoolean(instance, false);

        // clears the 'activeFactory' field to ensure a clean slate.
        Field factoryField = PersistenceService.class.getDeclaredField("activeFactory");
        factoryField.setAccessible(true);
        factoryField.set(instance, null);

        // Instantiates a concrete configuration DTO.
        // The MEMORY persistence type is selected to test the logic without requiring
        // external database connectivity or file system IO.
        AgroConfiguration testConfig = new AgroConfiguration(PersistenceType.MEMORY, false);

        // Invokes the initialization routine.
        instance.initialize(testConfig);

        // Validates that the factory has been instantiated.
        assertNotNull(instance.getFactory(), "The persistence factory instance must not be null after initialization.");

        // Validates that the instantiated factory matches the specific strategy requested (Polymorphism check).
        assertTrue(instance.getFactory() instanceof MemoryPersistenceFactory,
                "The service must instantiate MemoryPersistenceFactory when the MEMORY strategy is configured.");
    }
}