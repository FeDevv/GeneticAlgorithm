package org.agroplanner.domainsystem.model;

import org.agroplanner.domainsystem.model.types.*;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.List;
import java.util.Map;

/**
 * Central factory component responsible for the instantiation of concrete geometric domains.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Implements the <em>Parameterized Factory Method</em> pattern. It centralizes
 * the complex logic of converting raw configuration maps into strongly-typed {@link Domain} objects.</li>
 * <li><strong>SOLID Principles:</strong> Supports the <em>Open/Closed Principle (OCP)</em> partially (new shapes
 * require updating the switch, but clients remain unaffected) and the <em>Dependency Inversion Principle (DIP)</em>
 * by decoupling high-level controllers from concrete domain implementations.</li>
 * <li><strong>Singleton Strategy:</strong> Uses the <em>Initialization-on-demand holder idiom</em> (Bill Pugh)
 * to guarantee thread-safety and lazy initialization without the performance overhead of synchronized blocks.</li>
 * </ul>
 */
public class DomainFactory {

    /**
     * Private constructor to strictly enforce the Singleton pattern.
     */
    private DomainFactory() {
        // Stateless initialization
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
     * Fabricates a specific {@link Domain} implementation based on the provided metadata and parameters.
     *
     * <p><strong>Validation Pipeline:</strong></p>
     * <ol>
     * <li>Performs structural validation (presence of required keys) via {@link #validateParameters}.</li>
     * <li>Delegates instantiation to the specific domain constructor.</li>
     * <li>Domain constructors enforce deep semantic invariants (e.g., inner radius < outer radius).</li>
     * </ol>
     *
     * @param type   The metadata descriptor indicating which concrete class to instantiate.
     * @param params The configuration map containing the geometric values (e.g., "radius" -> 5.0).
     * @return A new, fully initialized and validated instance of a {@link Domain} subclass.
     * @throws InvalidInputException     If the provided map is missing required keys defined in {@link DomainType} or wrong values are entered.
     * @throws DomainConstraintException If the parameters are present but numerically invalid (e.g., negative).
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {

        // Phase 1: Pre-creation Validation (Fail-Fast)
        validateParameters(type, params);

        // Phase 2: Instantiation (Switch Expression)
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
     * Internal helper to enforce the parameter contract defined by the {@link DomainType}.
     *
     * <p><strong>Checks Performed:</strong></p>
     * <ul>
     * <li><strong>Completeness:</strong> Ensures the map contains all keys listed in {@link DomainType#getRequiredParameters()}.</li>
     * <li><strong>Non-Nullity:</strong> Ensures no value is null.</li>
     * <li><strong>Positivity:</strong> Enforces the fundamental geometric rule that dimensions must be strictly positive.</li>
     * </ul>
     *
     * @param type   The domain type schema.
     * @param params The input parameters to validate.
     * @throws InvalidInputException     If the schema contract is violated (missing keys).
     * @throws DomainConstraintException If physical constraints are violated (negative values).
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
