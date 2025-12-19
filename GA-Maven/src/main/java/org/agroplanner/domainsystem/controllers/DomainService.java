package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainFactory;
import org.agroplanner.domainsystem.model.DomainType;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p><strong>Service Layer for Domain Management.</strong></p>
 *
 * <p>This class acts as a facade/gateway between the Controller and the Model (Domain entities).
 * It handles logic related to domain capabilities validation and acts as a firewall for invalid inputs.</p>
 *
 * <p><strong>Instantiation Strategy:</strong></p>
 * <p>This service is <strong>stateless</strong>. It relies on the Java implicit default constructor
 * and accesses the {@link DomainFactory} directly via its Enum Singleton instance.
 * No dependency injection or complex initialization is required.</p>
 */
public class DomainService {

    /**
     * Retrieves the list of supported domain types.
     * <p>Used by the View to populate selection menus dynamically.</p>
     *
     * @return A list containing all enum constants of {@link DomainType}.
     */
    public List<DomainType> getAvailableDomainTypes() {
        return Arrays.asList(DomainType.values());
    }

    /**
     * Attempts to create a specific Domain instance based on the provided parameters.
     * <p>
     * This method delegates the actual object creation to the {@link DomainFactory}.
     * It acts as a gateway for "Deep Protection", allowing exceptions from the Model layer
     * to bubble up to the Controller.
     * </p>
     *
     * <p>
     * Throws InvalidInputException    If required parameters are missing or null.
     * Throws DomainConstraintException If the parameters are present but violate geometric or logic constraints
     * (e.g., negative dimensions, inner radius > outer radius).
     * </p>
     *
     * @param type   The type of domain to create (e.g., RECTANGLE, CIRCLE).
     * @param params A map of parameters required by the specific domain constructor (e.g., "width", "radius").
     * @return A new, fully validated instance of {@link Domain}.
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {
        return DomainFactory.getInstance().createDomain(type, params);
    }

    /**
     * Calculates the maximum permissible radius for a point (gene) within the given domain.
     * <p>
     * This logic resides in the Service layer because it represents a business rule:
     * a point cannot be larger than the smallest dimension of the container, otherwise
     * it would be physically impossible to place it without overlapping the boundaries.
     * </p>
     *
     * @param domain The domain instance to analyze.
     * @return The maximum valid radius (half of the shortest side of the bounding box).
     */
    public double calculateMaxValidRadius(Domain domain) {
        Rectangle2D boundingBox = domain.getBoundingBox();
        double boxWidth = boundingBox.getWidth();
        double boxHeight = boundingBox.getHeight();

        // Geometric Constraint: The diameter cannot exceed the shortest side of the box.
        // Therefore, Radius <= min(width, height) / 2.0
        return Math.min(boxWidth, boxHeight) / 2.0;
    }
}
