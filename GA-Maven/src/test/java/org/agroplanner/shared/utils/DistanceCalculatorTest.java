package org.agroplanner.shared.utils;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

// By Federico Bonucci

/**
 * Unit tests for the {@link DistanceCalculator} service component.
 * <p>
 * This class validates the mathematical accuracy of spatial metric computations,
 * ensuring strict adherence to Euclidean geometry principles without relying on external dependencies.
 * </p>
 */
class DistanceCalculatorTest {

    /**
     * Verifies the Euclidean distance calculation using a standard Pythagorean triple (3-4-5).
     * <p>
     * This test ensures that the underlying geometric formula is correctly applied.
     * Given inputs representing the legs of a right triangle (dx=3, dy=4), the result
     * must equal the hypotenuse (5).
     * </p>
     */
    @Test
    void shouldCalculateCorrectEuclideanDistance() {

        DistanceCalculator calculator = new DistanceCalculator();

        // P1 placed at origin (0,0)
        Point p1 = createPoint(0, 0);
        // P2 placed at (3,4)
        Point p2 = createPoint(3, 4);

        double distance = calculator.getDistance(p1, p2);

        // --- ASSERT ---
        // Expected derivation: sqrt(3^2 + 4^2) = sqrt(9 + 16) = sqrt(25) = 5.0
        assertEquals(5.0, distance, 0.0001, "The calculated distance deviates from the expected Pythagorean result.");
    }

    /**
     * Validates the identity property of a metric space.
     * <p>
     * According to metric space axioms, the distance from a point to itself (d(x,x))
     * must always be zero.
     * </p>
     */
    @Test
    void shouldReturnZero_WhenPointsAreIdentical() {

        DistanceCalculator calculator = new DistanceCalculator();
        Point p1 = createPoint(10.5, 20.1);

        double distance = calculator.getDistance(p1, p1);

        // --- ASSERT ---
        assertEquals(0.0, distance, 0.0001, "Distance from a point to itself must be exactly zero.");
    }

    /**
     * Ensures mathematical correctness when coordinates lie in negative Cartesian quadrants.
     * <p>
     * This test verifies that the algorithm correctly handles signed integers/doubles,
     * confirming that directionality does not affect the magnitude of the scalar distance.
     * </p>
     */
    @Test
    void shouldHandleNegativeCoordinates() {

        DistanceCalculator calculator = new DistanceCalculator();

        // P1: 3rd Quadrant (-2, -2)
        Point p1 = createPoint(-2, -2);
        // P2: 1st Quadrant (1, 2)
        Point p2 = createPoint(1, 2);

        double distance = calculator.getDistance(p1, p2);

        // Delta X = 1 - (-2) = 3
        // Delta Y = 2 - (-2) = 4
        // Result must still be 5.0
        assertEquals(5.0, distance, 0.0001, "Algorithm failed to process negative coordinate inputs correctly.");
    }

    // -------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------

    /**
     * Factory helper to instantiate a valid Point object for testing purposes.
     * <p>
     * This method isolates the tests from the specific validation requirements
     * of the Point constructor (e.g., non-null PlantType), allowing the test
     * logic to focus solely on coordinate values.
     * </p>
     *
     * @param x The X coordinate.
     * @param y The Y coordinate.
     * @return A valid Point instance with generic metadata.
     */
    private Point createPoint(double x, double y) {
        return new Point(
                x,
                y,
                1.0,                // Valid radius > 0
                PlantType.GENERIC,  // Valid enum constant
                0,                  // Dummy ID
                "TestVariety"       // Dummy Name
        );
    }
}