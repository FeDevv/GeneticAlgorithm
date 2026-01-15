package org.agroplanner.shared.exceptions;

/**
 * Thrown when an operation results in a state that violates the business rules
 * or physical constraints of a domain entity.
 */
public class DomainConstraintException extends AgroPlannerException {

    /**
     * Initializes the exception with a formatted message describing the invalid parameter.
     *
     * @param paramName The name of the parameter violating the constraint.
     * @param reason    The specific reason for the violation.
     */
    public DomainConstraintException(String paramName, String reason) {
        super(String.format("Invalid parameter '%s': %s", paramName, reason));
    }

    /**
     * Initializes the exception with a general violation message.
     *
     * @param message The detail message.
     */
    public DomainConstraintException(String message) {
        super(message);
    }
}
