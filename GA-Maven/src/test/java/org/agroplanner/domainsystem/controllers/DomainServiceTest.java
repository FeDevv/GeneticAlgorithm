package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

// By Federico Bonucci

/**
 * Integration tests for the {@link DomainService} controller.
 * <p>
 * This class validates the orchestration logic between the Service Layer and the Domain Model.
 * Unlike isolated unit tests, these tests purposefully utilize the real {@code DomainFactory}
 * to ensure that the Service correctly propagates input parameters and strictly enforces
 * geometric business rules (e.g., maximum plant size constraints).
 * </p>
 */
class DomainServiceTest {

    /**
     * Verifies that the service calculates the correct geometric constraints based on the
     * Minimum Bounding Rectangle (MBR) of the created domain.
     * <p>
     * <strong>Business Rule:</strong>
     * The maximum permissible radius for a plant must not exceed half of the shortest dimension
     * of the field's bounding box ($R_{max} = \min(w, h) / 2$).
     * </p>
     */
    @Test
    void shouldCalculateCorrectMaxRadius_WhenDomainIsCreated() {

        DomainService service = new DomainService();

        // Simulate user input from the UI/Wizard layer.
        // Configuration: Rectangle (Width=100, Height=50).
        Map<String, Double> params = new HashMap<>();
        params.put("width", 100.0);
        params.put("height", 50.0);

        // Create a REAL domain object (Integration Style: Service + Factory).
        // This confirms that the service correctly delegates instantiation to the factory.
        Domain realDomain = service.createDomain(DomainType.RECTANGLE, params);

        double result = service.calculateMaxValidRadius(realDomain);

        // Derivation: The shortest side is Height (50.0).
        // Constraint: 50.0 / 2.0 = 25.0.
        assertEquals(25.0, result, 0.001,
                "The calculated constraint must adhere to the Minimum Bounding Rectangle heuristic (half of the shortest side).");
    }

    /**
     * Ensures that the service correctly propagates validation exceptions originating
     * from the lower layers (Model/Factory) when inputs are malformed.
     * <p>
     * This test validates the "Deep Protection" mechanism, confirming that invalid
     * configuration attempts (e.g., missing mandatory parameters) are rejected
     * before any domain object is materialized.
     * </p>
     */
    @Test
    void shouldThrowException_WhenCreatingDomainWithMissingParams() {

        DomainService service = new DomainService();

        // Construct a malformed parameter map (Missing mandatory 'height' for Rectangle).
        Map<String, Double> params = new HashMap<>();
        params.put("width", 100.0);

        // The service should allow the InvalidInputException to bubble up from the Factory.
        assertThrows(InvalidInputException.class, () -> {
            service.createDomain(DomainType.RECTANGLE, params);
        }, "The service must propagate InvalidInputException when mandatory parameters are missing.");
    }
}