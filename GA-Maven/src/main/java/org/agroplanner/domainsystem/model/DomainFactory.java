package org.agroplanner.domainsystem.model;

import org.agroplanner.domainsystem.model.types.*;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;

import java.util.List;
import java.util.Map;

/**
 * <p><strong>Factory for Domain Instantiation.</strong></p>
 *
 * <p>This class implements the <strong>Factory Method Pattern</strong> to centralize the creation logic
 * of concrete {@link Domain} objects (e.g., Circle, Rectangle).
 * Its responsibilities adhere to the <strong>Single Responsibility Principle (SRP)</strong>:</p>
 * <ul>
 * <li>It decouples the creation logic from the client (Service/Controller).</li>
 * <li>It performs preliminary validation on input parameters based on the {@link DomainType} metadata.</li>
 * </ul>
 *
 * <p><strong>Pattern:</strong> Singleton (Initialization-on-demand holder idiom).</p>
 */
public class DomainFactory {

    /**
     * Private constructor to prevent direct instantiation.
     */
    private DomainFactory() {
        // Logica di inizializzazione vuota (stateless)
    }

    /**
     * Static Inner Class (Lazy Holder).
     * <p>
     * This class is loaded by the JVM only when {@code getInstance()} is invoked for the first time.
     * This idiom guarantees native Thread-Safety without the overhead of synchronized blocks.
     * </p>
     */
    private static class FactoryHolder {
        private static final DomainFactory INSTANCE = new DomainFactory();
    }

    /**
     * Retrieves the global singleton instance of the factory.
     * @return The singleton instance.
     */
    public static DomainFactory getInstance() {
        return FactoryHolder.INSTANCE;
    }

    // ------------------- BUSINESS LOGIC -------------------

    /**
     * Creates a concrete instance of a Domain based on the provided type and parameters.
     * <p>
     * This method acts as the entry point for the creation process. It delegates the specific
     * instantiation to a switch expression after validating the inputs.
     * </p>
     *
     * @param type   The metadata describing the domain to create.
     * @param params The map containing configuration values (e.g., width, radius).
     * @return A new instance of the specific {@link Domain} implementation.
     * @throws InvalidInputException     If required parameters are missing, null, or if the type is unsupported.
     * @throws DomainConstraintException If parameters are present but mathematically invalid (e.g., negative).
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {

        // Pre-creation Validation
        validateParameters(type, params);

        // Instantiation (Switch Expression)
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
            // Defensive Programming: Covers the case where an Enum is added but the Factory is not updated.
            default -> throw new InvalidInputException("Unsupported domain type implementation: " + type);
        };
    }

    /**
     * internal helper method for validation.
     * <p>
     * Checks that all required parameters defined in {@link DomainType#getRequiredParameters()}
     * are present, non-null, and strictly positive.
     * </p>
     *
     * @param type   The domain type.
     * @param params The input parameters.
     * @throws InvalidInputException     If a key is missing or value is null.
     * @throws DomainConstraintException If a value is <= 0.
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
            // More complex constraints (e.g., inner < outer) are handled by the specific Domain constructors.
            if (value <= 0) {
                throw new DomainConstraintException(key, "must be strictly positive (> 0).");
            }
        }
    }
}
