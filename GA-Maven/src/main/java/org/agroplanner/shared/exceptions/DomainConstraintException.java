package org.agroplanner.shared.exceptions;

/**
 * Indicates that a requested operation or instantiation violates the physical or geometric
 * invariants of a Domain Entity.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Domain Integrity:</strong> This exception serves as the primary enforcement mechanism for
 * <em>Class Invariants</em> within the Domain Model. It ensures that no Domain Object (POJO)
 * can ever exist in an inconsistent or physically impossible state.</li>
 * <li><strong>Deep Protection:</strong> Typically thrown by constructors or factory methods immediately
 * upon detecting invalid geometric relationships preventing the propagation of corrupt state
 * to the Calculation Engine.</li>
 * </ul>
 *
 * @see TerrainExceptions
 */
public class DomainConstraintException extends TerrainExceptions {

    /**
     * Constructs an exception for a specific parameter validation failure, enforcing a standardized message format.
     *
     * @param paramName The name of the parameter that violated the constraint.
     * @param reason    The specific rule that was broken (e.g., "must be strictly positive").
     */
    public DomainConstraintException(String paramName, String reason) {
        super(String.format("Invalid parameter '%s': %s", paramName, reason));
    }

    /**
     * Constructs an exception for complex or relational constraint violations.
     *
     * <p><strong>Use Case:</strong></p>
     * Used for <em>Cross-Field Validation</em> where the validity of one parameter depends on another
     * (e.g., ensuring an Inner Radius is strictly smaller than an Outer Radius).
     *
     * @param message The detailed description of the constraint violation.
     */
    public DomainConstraintException(String message) {
        super(message);
    }
}
