package org.agroplanner.shared.utils;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.gasystem.model.Point;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Static utility class providing high-throughput stochastic primitives used throughout the
 * evolutionary process.
 *
 * <p><strong>Architecture & Concurrency:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Static Utility (Stateless).</li>
 * <li><strong>Thread Safety:</strong> Delegates all random generation to {@link java.util.concurrent.ThreadLocalRandom}.
 * Unlike {@code java.util.Random}, which uses an atomic seed and suffers from thread contention
 * in parallel environments, {@code ThreadLocalRandom} maintains isolated seeds per thread.
 * This ensures linear scalability when the Genetic Algorithm executes via {@code parallelStream()}.</li>
 * </ul>
 */
public class RandomUtils {

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Prevents instantiation to enforce the static utility pattern.
     *
     * @throws IllegalStateException if verified via reflection.
     */
    private RandomUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ------------------- CORE METHODS -------------------

    /**
     * Generates a set of distinct integers selected from a uniform distribution.
     * <p>
     * Implements <em>Sampling without Replacement</em> logic. This is algorithmically required
     * for selecting distinct parents during the Tournament Selection phase without bias.
     * </p>
     *
     * <p><strong>Performance Note:</strong>
     * Uses a {@link HashSet} for O(1) average time complexity checking of duplicates.
     * </p>
     *
     * @param n            The cardinality of the required set (number of unique indices).
     * @param maxExclusive The upper bound of the sample space (0 to maxExclusive-1).
     * @return A list containing exactly {@code n} unique integers.
     * @throws IllegalArgumentException (Implicit) if {@code n > maxExclusive}, causing an infinite loop.
     * Caller is responsible for validating bounds.
     */
    public static List<Integer> uniqueIndices(int n, int maxExclusive) {
        // Use a Set to automatically handle uniqueness efficiently.
        Set<Integer> uniqueIndices = new HashSet<>();
        ThreadLocalRandom currentRandom = ThreadLocalRandom.current();

        // Warning: If n > maxExclusive, this loop will be infinite.
        while (uniqueIndices.size() < n) {
            uniqueIndices.add(currentRandom.nextInt(maxExclusive));
        }

        return new ArrayList<>(uniqueIndices);
    }

    /**
     * Simulates a Bernoulli trial with p=0.5 (Fair Coin Toss).
     * <p>
     * Provides the boolean entropy required for operations like <em>Uniform Crossover</em>,
     * determining gene inheritance direction.
     * </p>
     *
     * @return {@code 0} or {@code 1} with equal probability.
     */
    public static int coinToss() {
        return ThreadLocalRandom.current().nextInt(2);
    }

    /**
     * Generates a pseudorandom {@code double} value from a uniform distribution between 0.0 and 1.0.
     * <p>
     * This value serves as the basis for probabilistic thresholds (e.g., determining if a
     * Mutation event should occur based on the mutation rate).
     * </p>
     *
     * @return A value in the range [0.0, 1.0).
     */
    public static double randDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Generates a new coordinate point strictly contained within a specified geometric region.
     * <p>
     * Applies <em>Linear Interpolation</em> on normalized random factors to map the [0,1]
     * probability space onto the target 2D Cartesian bounds.
     * </p>
     *
     * @param boundingBox The 2D geometric constraints (minX, minY, width, height).
     * @param radius      The radius attribute for the generated point.
     * @param type        The botanical classification associated with this point.
     * @return A new {@link Point} instance with coordinates localized within the bounding box.
     */
    public static Point insideBoxGenerator(Rectangle2D boundingBox, double radius, PlantType type) {

        // Generate normalized factors [0.0, 1.0)
        double randomXfactor = ThreadLocalRandom.current().nextDouble();
        double randomYfactor = ThreadLocalRandom.current().nextDouble();

        // Linear Mapping (Lerp)
        // Map [0,1] to [MinX, MaxX]
        double x = boundingBox.getMinX() + (randomXfactor * boundingBox.getWidth());
        double y = boundingBox.getMinY() + (randomYfactor * boundingBox.getHeight());

        return new Point(x, y, radius, type);
    }
}
