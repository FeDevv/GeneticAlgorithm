package org.agroplanner.domainsystem.model;

import org.agroplanner.gasystem.model.Individual;

import java.awt.geom.Rectangle2D;

/**
 * <p><strong>Geometric Contract for the Domain Model.</strong></p>
 *
 * <p>This interface defines the behavior of any geometric shape serving as a problem domain.
 * Implementations represent specific geometries (e.g., Rectangle, Circle) and provide
 * logic for spatial validation and boundary definition.</p>
 */
public interface Domain {
    /**
     * Determines if a specific coordinate lies outside the domain boundaries.
     * <p>
     * This method implements the core geometric inequality for the specific shape.
     * </p>
     *
     * @param x The X coordinate of the point to test.
     * @param y The Y coordinate of the point to test.
     * @return {@code true} if the point is <strong>outside</strong> the domain;
     * {@code false} if it is inside or on the boundary.
     */
    boolean isPointOutside(double x, double y);

    /**
     * Validates whether an entire Individual satisfies the domain's spatial constraints.
     * <p>
     * An individual is valid if and only if every Point in its chromosome sequence
     * resides within the domain boundaries.
     * </p>
     *
     * @param individual The candidate solution to validate.
     * @return {@code true} if all points are strictly contained within the domain, {@code false} otherwise.
     */
    boolean isValidIndividual(Individual individual);

    /**
     * Retrieves the Bounding Box (Smallest Enclosing Rectangle) of the domain.
     * <p>
     * This rectangle is used by the population factory to define the generation limits for random coordinates,
     * ensuring points are generated within a relevant area before precise geometric validation.
     * </p>
     *
     * @return A {@link Rectangle2D} representing the rectangular bounds of this domain.
     */
    Rectangle2D getBoundingBox();
}
