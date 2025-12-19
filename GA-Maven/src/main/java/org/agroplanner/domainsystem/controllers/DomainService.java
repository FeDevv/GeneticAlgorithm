package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainFactory;
import org.agroplanner.domainsystem.model.DomainType;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * <p><strong>Business Logic for the Domain Subsystem.</strong></p>
 *
 * <p>This service acts as the bridge between the UI Controller and the low-level model logic (Factory and Entities).
 * Its responsibilities include:</p>
 * <ul>
 * <li><strong>Abstraction:</strong> Decoupling the controller from the specific instantiation logic (Factory pattern).</li>
 * <li><strong>Data Provisioning:</strong> Supplying available domain types to the UI.</li>
 * <li><strong>Geometric Validation:</strong> Calculating constraints based on the chosen domain (e.g., max valid point radius).</li>
 * </ul>
 *
 * <p>This class is stateless regarding the session, but depends on the {@link DomainFactory} singleton.</p>
 */
public class DomainService {

    /**
     * Initializes the service and retrieves the singleton instance of the Factory.
     */
    public DomainService() {  }

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
