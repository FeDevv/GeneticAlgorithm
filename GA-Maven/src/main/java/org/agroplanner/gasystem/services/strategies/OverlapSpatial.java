package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.services.helpers.PenaltyHelper;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Concrete implementation of the collision strategy using a <strong>Spatial Hashing (Uniform Grid)</strong> approach.
 *
 * <p><strong>Architecture & Complexity:</strong></p>
 * This strategy optimizes the calculation for large populations by reducing the search space.
 * <ul>
 * <li><strong>Complexity:</strong> O(N) average case. Building the grid takes O(N). Querying neighbors takes constant time O(k) per point (assuming uniform density).</li>
 * <li><strong>Trade-off:</strong> Introduces overhead for map allocation and hashing. It becomes efficient only when N > 80.</li>
 * </ul>
 */
public class OverlapSpatial implements OverlapStrategy {

    /**
     * Immutable composite key representing a discrete grid coordinate (i, j).
     * <p><strong>Performance Note:</strong> Java Records provide optimized, collision-resistant implementations
     * of {@code hashCode()} and {@code equals()}, making them ideal keys for the bucket map.</p>
     */
    private record Cell(int i, int j) {}

    /**
     * The dimensions of a single bucket in the grid.
     * <p><strong>Constraint:</strong> Must be >= 2•r_{max} to guarantee that colliding pairs
     * reside either in the same cell or in immediately adjacent cells.</p>
     */
    private final double cellSize;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the spatial indexer.
     *
     * @param maxRadius The absolute maximum radius of any entity in the system.
     */
    public OverlapSpatial(double maxRadius) {
        // Geometric Requirement:
        // To catch all collisions using only the immediate 3x3 neighborhood (Moore Neighborhood),
        // the cell size must be at least the diameter of the largest object.
        // If smaller, an object could physically span across multiple cells, potentially touching
        // an object in a non-adjacent cell (Tunneling effect).
        this.cellSize = 2.0 * maxRadius;
    }

    // ------------------- MAIN ALGORITHM -------------------

    /**
     * Computes the total overlap penalty using grid-based partitioning.
     *
     * @param chromosomes        The population to index.
     * @param overlapWeight      The penalty multiplier.
     * @param distanceCalculator The metric service.
     * @return The total penalty.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        // Phase 1: Indexing (Bucketing). Complexity: O(N).
        Map<Cell, List<Point>> grid = populateGrid(chromosomes);

        double penalty = 0.0;

        // Phase 2: Querying. Average Complexity: O(N * constant).
        for (Point referencePoint : chromosomes) {
            penalty += calculatePenaltyForPoint(referencePoint, grid, overlapWeight, distanceCalculator);
        }

        return penalty;
    }

    // ------------------- INTERNAL LOGIC -------------------

    /**
     * Maps a continuous Cartesian coordinate to a discrete grid index.
     * index = ⌊{coord}/{size}⌋
     */
    private int getCellIndex(double coordinate) {
        return (int) Math.floor(coordinate / cellSize);
    }

    /**
     * Distributes all points into grid buckets.
     */
    private Map<Cell, List<Point>> populateGrid(List<Point> chromosomes) {
        // Assuming roughly 1 point per cell to minimize resizing.
        Map<Cell, List<Point>> grid = new HashMap<>();

        for (Point p : chromosomes) {
            int i = getCellIndex(p.getX());
            int j = getCellIndex(p.getY());
            // Functional insertion: "If cell missing, create list. Then add point."
            grid.computeIfAbsent(new Cell(i, j), k -> new ArrayList<>()).add(p);
        }
        return grid;
    }

    /**
     * Evaluates collisions for a single point by checking its local vicinity.
     */
    private double calculatePenaltyForPoint(
            Point referencePoint,
            Map<Cell, List<Point>> grid,
            double overlapWeight,
            DistanceCalculator distCalc
    ) {
        double localPenalty = 0.0;
        int iCell = getCellIndex(referencePoint.getX());
        int jCell = getCellIndex(referencePoint.getY());

        // Moore Neighborhood Iteration (3x3 Grid):
        // Checks the central cell (0,0) and the 8 surrounding neighbors (-1 to +1).
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {

                // Look up the bucket. Hash map lookup is O(1).
                List<Point> neighbors = grid.get(new Cell(iCell + di, jCell + dj));

                // Optimization: Skip empty cells (Null check).
                if (neighbors == null) continue;

                localPenalty += processNeighbors(referencePoint, neighbors, overlapWeight, distCalc);
            }
        }
        return localPenalty;
    }

    /**
     * Compares the reference point against a candidate list of neighbors.
     */
    private double processNeighbors(
            Point referencePoint,
            List<Point> neighbors,
            double overlapWeight,
            DistanceCalculator distCalc
    ) {
        double penalty = 0.0;
        for (Point neighborPoint : neighbors) {

            // Symmetry Breaking: Prevent checking (A, B) and then (B, A).
            if (shouldSkipPair(referencePoint, neighborPoint)) continue;

            penalty += PenaltyHelper.calculatePairPenalty(referencePoint, neighborPoint, overlapWeight, distCalc);
        }
        return penalty;
    }

    /**
     * Determines if a pair should be processed to ensure each unique pair is checked exactly once.
     *
     * <p><strong>Logic (Canonical Ordering):</strong></p>
     * In the quadratic loop, we used {@code j = i + 1} to avoid double counting.
     * Here, we iterate over collections without indices. To solve this, we impose an arbitrary order
     * using the object's memory identity hash.
     * <ul>
     * <li><strong>Reflexivity:</strong> {@code p1 == p2} (Self-collision is impossible).</li>
     * <li><strong>Symmetry:</strong> If {@code Hash(p1) > Hash(p2)}, we skip. We only process if {@code Hash(p1) < Hash(p2)}.</li>
     * </ul>
     * This guarantees that the pair {A, B} is evaluated only when A visits B (or vice versa), but never both.
     */
    private boolean shouldSkipPair(Point p1, Point p2) {
        return p1 == p2 || System.identityHashCode(p1) > System.identityHashCode(p2);
    }
}
