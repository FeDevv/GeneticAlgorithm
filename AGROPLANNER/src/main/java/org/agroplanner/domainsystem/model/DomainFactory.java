package org.agroplanner.domainsystem.model;

import org.agroplanner.domainsystem.model.types.*;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.List;
import java.util.Map;

/**
 * Central factory component for the instantiation of concrete geometric domains.
 * <p>
 * Implements the <strong>Parameterized Factory Method</strong> pattern to convert raw configuration maps
 * into strongly-typed {@link Domain} objects, enforcing structural integrity before instantiation.
 * </p>
 */
public class DomainFactory {

    /**
     * Private constructor to strictly enforce the Singleton pattern.
     */
    private DomainFactory() {
        // Enforce Singleton
    }

    /**
     * Static Inner Class responsible for holding the Singleton instance.
     * <p>
     * <strong>JVM Guarantee:</strong> This class is not loaded until {@link #getInstance()} is called.
     * The ClassLoader mechanism guarantees that the static initialization of {@code INSTANCE} is serial
     * and thread-safe, eliminating the need for explicit locking.
     * </p>
     */
    private static class FactoryHolder {
        private static final DomainFactory INSTANCE = new DomainFactory();
    }

    /**
     * Retrieves the global singleton instance of the factory.
     *
     * @return The shared {@code DomainFactory} instance.
     */
    public static DomainFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    // ------------------- BUSINESS LOGIC -------------------

    /**
     * Fabricates a specific {@link Domain} implementation based on metadata and parameters.
     *
     * @param type   The descriptor indicating which concrete class to instantiate.
     * @param params The configuration map containing the geometric values.
     * @return A new, fully initialized and validated {@link Domain} instance.
     * @throws InvalidInputException     If required parameters are missing.
     * @throws DomainConstraintException If parameters are present but numerically invalid (e.g., negative).
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {

        // Phase 1: Fail-Fast Validation
        validateParameters(type, params);

        // Phase 2: Instantiation
        return switch (type) {
            case CIRCLE ->
                    new CircleDomain(params.get("radius"));
            case RECTANGLE ->
                    new RectangleDomain(params.get("width"), params.get("height"));
            case SQUARE ->
                    new SquareDomain(params.get("side"));
            case ELLIPSE ->
                    new EllipseDomain(params.get("semi-width"), params.get("semi-height"));
            case RIGHT_ANGLED_TRIANGLE ->
                    new RATDomain(params.get("base"), params.get("height"));
            case FRAME ->
                    new FrameDomain(params.get("innerWidth"), params.get("innerHeight"), params.get("outerWidth"), params.get("outerHeight"));
            case ANNULUS ->
                    new AnnulusDomain(params.get("innerRadius"), params.get("outerRadius"));

            // Defensive Programming: Handles future enum expansions that miss a factory implementation
            default -> throw new InvalidInputException("Unsupported domain type implementation: " + type);
        };
    }

    /**
     * Enforces the parameter contract defined by the {@link DomainType}.
     *
     * @param type   The domain type schema.
     * @param params The input parameters to validate.
     */
    private void validateParameters(DomainType type, Map<String, Double> params) {

        List<String> requiredKeys = type.getRequiredParameters();

        for (String key : requiredKeys) {
            Double value = params.get(key);

            // --- VALIDATION 1: PRESENCE & NULLITY ---
            // If a parameter is missing, the request itself is malformed.
            if (!params.containsKey(key) || value == null) {
                throw new InvalidInputException(
                        "Missing or null parameter for the domain '" + type.getDisplayName() + "': " + key
                );
            }

            // --- VALIDATION 2: BASIC CONSTRAINTS ---
            // Basic geometric rule: dimensions must be positive.
            // Complex relational constraints (e.g., inner < outer) are deferred to the Domain constructors.
            if (value <= 0) {
                throw new DomainConstraintException(key, "must be strictly positive (> 0).");
            }
        }
    }
}
