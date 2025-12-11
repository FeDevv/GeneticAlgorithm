package org.agroplanner.gasystem.services.strategies;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.services.helpers.PenaltyHelper;
import org.agroplanner.shared.utils.DistanceCalculator;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * <p><strong>Collision Strategy: Spatial Hashing (Uniform Grid).</strong></p>
 *
 * <p>This implementation reduces the algorithmic complexity from <strong>O(N²)</strong> to approximately <strong>O(N)</strong>
 * (linear time). It is the preferred strategy for dense populations where brute-force checks become a bottleneck.</p>
 *
 * <p><strong>Algorithmic Principle:</strong><br>
 * The continuous 2D space is divided into a discrete grid of cells. Instead of checking a point against everyone else,
 * we only check it against points in the same cell or in the immediately adjacent cells (3x3 neighborhood).</p>
 */
public class OverlapSpatial implements OverlapStrategy {

    /**
     * Immutable record representing a grid coordinate Key (i, j).
     * <p>Java Records provide optimized, collision-resistant implementations of {@code equals()} and {@code hashCode()},
     * making them ideal keys for the {@link HashMap}.</p>
     */
    private record Cell(int i, int j) {}

    /**
     * The width/height of a single grid cell.
     * Calculated to ensure strict locality of collision checks.
     */
    private final double cellSize;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the spatial strategy.
     *
     * @param maxRadius The absolute maximum radius any point in the system can have.
     */
    public OverlapSpatial(double maxRadius) {
        // MATH CRITICAL: Cell Size = 2 * Max_Radius (Diameter).
        // Why? To guarantee correctness.
        // If a circle has diameter D, it can AT MOST overlap with objects in its own cell
        // or the 8 neighbors. If the cell were smaller, a large circle could reach into
        // cells further away (e.g., index +2), breaking the optimization logic.
        this.cellSize = 2.0 * maxRadius;
    }

    // ------------------- MAIN ALGORITHM -------------------

    /**
     * Computes the total overlap penalty using the Grid-based approach.
     *
     * @param chromosomes        The list of points to evaluate.
     * @param overlapWeight      The penalty multiplier.
     * @param distanceCalculator The distance utility.
     * @return The accumulated penalty score.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        // Spatial Indexing (Building the Grid). Complexity: O(N).
        Map<Cell, List<Point>> grid = populateGrid(chromosomes);

        double penalty = 0.0;

        // Collision Detection (Querying the Grid). Average Complexity: O(N * k), where k << N.
        for (Point referencePoint : chromosomes) {
            penalty += calculatePenaltyForPoint(referencePoint, grid, overlapWeight, distanceCalculator);
        }

        return penalty;
    }

    // ------------------- INTERNAL LOGIC -------------------

    /**
     * Maps a continuous coordinate to a discrete grid index.
     * {@code index = floor(coord / size)}
     */
    private int getCellIndex(double coordinate) {
        return (int) Math.floor(coordinate / cellSize);
    }

    /**
     * Buckets all points into their respective grid cells.
     */
    private Map<Cell, List<Point>> populateGrid(List<Point> chromosomes) {
        Map<Cell, List<Point>> grid = new HashMap<>();
        for (Point p : chromosomes) {
            int i = getCellIndex(p.getX());
            int j = getCellIndex(p.getY());
            // computeIfAbsent creates the list if the cell is visited for the first time.
            grid.computeIfAbsent(new Cell(i, j), k -> new ArrayList<>()).add(p);
        }
        return grid;
    }

    /**
     * Calculates penalties for a single point by inspecting its local 3x3 neighborhood.
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

        // Iterate over the Moore Neighborhood (Current cell + 8 surrounding cells).
        // di, dj range from -1 to +1.
        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {

                // Retrieve the list of potential colliders in that cell.
                List<Point> neighbors = grid.get(new Cell(iCell + di, jCell + dj));

                // If the cell is empty, skip.
                if (neighbors == null) continue;

                localPenalty += processNeighbors(referencePoint, neighbors, overlapWeight, distCalc);
            }
        }
        return localPenalty;
    }

    /**
     * Checks collisions against a list of candidate neighbors.
     */
    private double processNeighbors(
            Point referencePoint,
            List<Point> neighbors,
            double overlapWeight,
            DistanceCalculator distCalc
    ) {
        double penalty = 0.0;
        for (Point neighborPoint : neighbors) {

            // Critical Optimization: Skip invalid or duplicate pairs.
            if (shouldSkipPair(referencePoint, neighborPoint)) continue;

            penalty += PenaltyHelper.calculatePairPenalty(referencePoint, neighborPoint, overlapWeight, distCalc);
        }
        return penalty;
    }

    /**
     * Determines if a pair of points should be skipped to prevent double counting.
     *
     * <p><strong>Logic:</strong>
     * <ol>
     * <li><strong>Self-Check:</strong> {@code p1 == p2}. A point cannot overlap with itself.</li>
     * <li><strong>Canonical Ordering:</strong> Uses {@link System#identityHashCode} to impose an arbitrary order.
     * We only process the pair if {@code Hash(A) < Hash(B)}. If {@code Hash(A) > Hash(B)}, we skip.
     * This ensures the pair {A, B} is penalized exactly once, replacing the {@code j = i + 1} logic of nested loops.</li>
     * </ol>
     * </p>
     */
    private boolean shouldSkipPair(Point p1, Point p2) {
        // Skip self-comparison OR Skip if the pair order is reversed (to avoid double counting).
        return p1 == p2 || System.identityHashCode(p1) > System.identityHashCode(p2);
    }
}
