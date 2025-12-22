package org.agroplanner.domainsystem.controllers;

import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.domainsystem.model.DomainFactory;
import org.agroplanner.domainsystem.model.DomainType;

import java.awt.geom.Rectangle2D;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Service component responsible for the logical management of geometric domains.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Service Layer / Facade. It acts as a gateway between the presentation-focused
 * Controller and the underlying Domain Model, providing a simplified interface for domain creation and analysis.</li>
 * <li><strong>Statelessness:</strong> This class maintains no internal state. It delegates object creation
 * to the {@link DomainFactory} singleton and performs calculations based solely on method arguments.
 * This design promotes low coupling and allows the service to be used as a shared resource.</li>
 * </ul>
 */
public class DomainService {

    /**
     * Retrieves the catalog of supported geometric shapes.
     *
     * <p><strong>Usage:</strong></p>
     * Consumed by the View layer to dynamically populate selection menus (e.g., CLI options or Dropdowns),
     * ensuring the UI is always synchronized with the available {@link DomainType} enumeration.
     *
     * @return A list containing all defined {@link DomainType} constants.
     */
    public List<DomainType> getAvailableDomainTypes() {
        return Arrays.asList(DomainType.values());
    }

    /**
     * Orchestrates the creation of a concrete Domain entity.
     *
     * <p><strong>Pattern (Delegation):</strong></p>
     * Delegates the actual instantiation logic to the {@link DomainFactory}. This method acts as a
     * pass-through for the "Deep Protection" mechanism: exceptions originating from the Model's validation
     * layers are allowed to bubble up to the Controller for user feedback.
     *
     * @param type   The metadata descriptor for the desired shape (e.g., RECTANGLE).
     * @param params The configuration map containing the required geometric parameters.
     * @return A fully initialized, validated, and immutable {@link Domain} instance.
     * @see DomainFactory#createDomain(DomainType, Map)
     */
    public Domain createDomain(DomainType type, Map<String, Double> params) {
        return DomainFactory.getInstance().createDomain(type, params);
    }

    /**
     * Computes the theoretical upper bound for a plant's radius within a specific domain.
     *
     * <p><strong>Business Rule:</strong></p>
     * Ensures that users cannot configure plants that are physically too large to fit in the field.
     * The logic uses a conservative heuristic based on the Minimum Bounding Rectangle (MBR).
     * <br>
     * <em>Logic:</em> A circular object cannot be contained within a shape if its diameter exceeds
     * the shortest dimension of that shape's bounding box.
     *
     * @param domain The geometric domain instance to analyze.
     * @return The maximum permissible radius (half of the MBR's shortest side).
     */
    public double calculateMaxValidRadius(Domain domain) {
        Rectangle2D boundingBox = domain.getBoundingBox();
        double boxWidth = boundingBox.getWidth();
        double boxHeight = boundingBox.getHeight();

        // Geometric Constraint: The diameter (2*r) cannot exceed the shortest side of the box.
        // Therefore: r_max = min(width, height) / 2.0
        return Math.min(boxWidth, boxHeight) / 2.0;
    }
}
