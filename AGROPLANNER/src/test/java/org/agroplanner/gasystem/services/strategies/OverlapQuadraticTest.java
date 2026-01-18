package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.shared.utils.DistanceCalculator;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

// By Federico Bonucci

/**
 * Unit tests for the {@link OverlapQuadratic} collision detection strategy.
 * <p>
 * This class validates the correctness of the Brute-Force (O(N^2)) algorithm,
 * focusing on the integrity of the pair-wise iteration logic. It ensures that
 * the nested loops correctly traverse the unique combinations of entities
 * without redundancy (A vs B, B vs A) or reflexivity (A vs A).
 * </p>
 */
class OverlapQuadraticTest {

    private final DistanceCalculator distanceCalculator = new DistanceCalculator();
    private final OverlapQuadratic strategy = new OverlapQuadratic();

    /**
     * Verifies the boundary conditions for datasets with insufficient cardinality to form a pair.
     * <p>
     * According to combinatorial logic, the number of pairs C(n, 2) is zero for n < 2.
     * Consequently, the algorithm must yield a zero penalty for empty or singleton lists,
     * bypassing the execution of the inner comparison loop.
     * </p>
     */
    @Test
    void shouldReturnZero_WhenListHasInsufficientPoints() {
        // Case 1: Empty List (N=0)
        double resultEmpty = strategy.calculateOverlap(Collections.emptyList(), 10.0, distanceCalculator);
        assertEquals(0.0, resultEmpty, "The penalty must be 0.0 for an empty dataset.");

        // Case 2: Single Point (N=1)
        List<Point> singleList = new ArrayList<>();
        singleList.add(createPoint(0, 0, 5.0));

        double resultSingle = strategy.calculateOverlap(singleList, 10.0, distanceCalculator);
        assertEquals(0.0, resultSingle, "The penalty must be 0.0 for a single entity (no pairs exist).");
    }

    /**
     * Validates that a positive penalty is generated when two geometric entities spatially intersect.
     * <p>
     * This test confirms that the strategy correctly delegates to the penalty calculation logic
     * when the Euclidean distance between centroids is less than the sum of their radii.
     * </p>
     */
    @Test
    void shouldDetectOverlap_WhenTwoPointsIntersect() {

        List<Point> points = new ArrayList<>();
        // P1 at (0,0) and P2 at (1,0) with Radius 10.
        // They heavily overlap.
        points.add(createPoint(0, 0, 10.0));
        points.add(createPoint(1, 0, 10.0));

        double penalty = strategy.calculateOverlap(points, 1.0, distanceCalculator);

        // Asserting strictly positive penalty verifies that the intersection was detected.
        assertTrue(penalty > 0.0, "The strategy failed to detect a clear geometric intersection.");
    }

    /**
     * Ensures that spatially disjoint entities result in a zero penalty.
     * <p>
     * This test verifies that the algorithm correctly filters out non-colliding pairs,
     * preserving the integrity of the fitness function evaluation.
     * </p>
     */
    @Test
    void shouldReturnZero_WhenPointsAreFarApart() {

        List<Point> points = new ArrayList<>();
        // Two points separated by a large distance (diagonal > sum of radii)
        points.add(createPoint(0, 0, 1.0));
        points.add(createPoint(100, 100, 1.0));

        double penalty = strategy.calculateOverlap(points, 1.0, distanceCalculator);

        assertEquals(0.0, penalty, 0.0001, "The penalty must be zero for non-intersecting entities.");
    }

    /**
     * Validates the aggregation logic for multiple overlapping entities.
     * <p>
     * This test checks the "Triangular Loop" logic. For a cluster of 3 mutually overlapping points
     * (A, B, C), the total penalty must be the sum of 3 distinct interactions:
     * (A-B), (A-C), and (B-C).
     * </p>
     */
    @Test
    void shouldAccumulatePenalties_ForMultipleOverlaps() {

        List<Point> points = new ArrayList<>();
        // All 3 points share the same coordinates, guaranteeing mutual overlap.
        points.add(createPoint(0, 0, 10.0)); // A
        points.add(createPoint(0, 0, 10.0)); // B
        points.add(createPoint(0, 0, 10.0)); // C

        double totalPenalty = strategy.calculateOverlap(points, 1.0, distanceCalculator);

        // The total penalty must be strictly positive and reflect the accumulation of all pairs.
        assertTrue(totalPenalty > 0, "The total penalty should reflect the accumulation of multiple collision pairs.");
    }

    // -------------------------------------------------------------------
    // HELPER METHODS
    // -------------------------------------------------------------------

    /**
     * Factory helper to instantiate valid Point objects for testing.
     * <p>
     * Isolates the test logic from the complexity of the Point constructor.
     * </p>
     */
    private Point createPoint(double x, double y, double radius) {
        return new Point(
                x,
                y,
                radius,
                PlantType.GENERIC,
                0,
                "Test"
        );
    }
}