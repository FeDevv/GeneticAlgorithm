package org.agroplanner.shared.exceptions;

/**
 * <p><strong>Exception: Geometric/Physical Constraint Violation.</strong></p>
 *
 * <p>Thrown when the parameters defining a geometric domain violate logical or physical rules.
 * This ensures the integrity of the domain model before any calculation begins.</p>
 *
 * <p><strong>Examples:</strong>
 * <ul>
 * <li>Defining a rectangle with negative width or height.</li>
 * <li>Defining a bounding box that is smaller than the objects it must contain.</li>
 * <li>Trying to calculate a radius for a shape that doesn't support it.</li>
 * </ul>
 * </p>
 */
public class DomainConstraintException extends TerrainExceptions {

    /**
     * Constructs an exception for a specific invalid parameter.
     * <p>
     * <strong>Helper Constructor:</strong> Automatically formats the message to ensure
     * consistent logging standards across the application.
     * <br>
     * <em>Format: "Invalid parameter 'paramName': reason"</em>
     * </p>
     *
     * @param paramName The name of the variable that failed validation (e.g., "width").
     * @param reason    The specific rule that was broken (e.g., "must be strictly positive").
     */
    public DomainConstraintException(String paramName, String reason) {
        super(String.format("Invalid parameter '%s': %s", paramName, reason));
    }

    /**
     * Constructs an exception for generic or relational constraint violations.
     * <p>
     * Used when the error involves multiple parameters interacting (e.g., "Radius X is too large for Width Y").
     * </p>
     *
     * @param message The detailed error description.
     */
    public DomainConstraintException(String message) {
        super(message);
    }
}
