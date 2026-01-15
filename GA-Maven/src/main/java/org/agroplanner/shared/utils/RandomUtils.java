package org.agroplanner.shared.utils;

import org.agroplanner.gasystem.model.Point;
import org.agroplanner.inventory.model.PlantType;

import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

/**
 * Static utility class providing high-throughput stochastic primitives used throughout the
 * evolutionary process.
 *
 * <p><strong>Architecture & Concurrency:</strong></p>
 * <ul>
 * <li><strong>Thread Safety:</strong> Delegates all random generation to {@link java.util.concurrent.ThreadLocalRandom}.
 * Unlike {@code java.util.Random}, which uses an atomic seed and suffers from thread contention
 * in parallel environments, {@code ThreadLocalRandom} maintains isolated seeds per thread.
 * This ensures linear scalability when the Genetic Algorithm executes via {@code parallelStream()}.</li>
 * <li><strong>Statelessness:</strong> The class holds no state, ensuring side-effect-free execution context.</li>
 * </ul>
 */
public class RandomUtils {

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Prevents instantiation to enforce the static utility pattern.
     *
     * @throws IllegalStateException if instantiation is attempted via reflection.
     */
    private RandomUtils() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    // ------------------- CORE METHODS -------------------

    /**
     * Generates a set of distinct integers selected from a uniform distribution.
     * <p>
     * Implements <em>Sampling without Replacement</em>. This is algorithmically required
     * for selecting distinct parents during the Tournament Selection phase without bias.
     * </p>
     *
     * <p><strong>Performance Note:</strong>
     * Uses a {@link HashSet} for O(1) average time complexity when checking for duplicates.
     * </p>
     *
     * @param n            The cardinality of the required set (number of unique indices).
     * @param maxExclusive The upper bound of the sample space (0 to maxExclusive-1).
     * @return A list containing exactly {@code n} unique integers.
     * @throws IllegalArgumentException if {@code n > maxExclusive}, as it is mathematically impossible
     * to select that many unique integers from the given range.
     */
    public static List<Integer> uniqueIndices(int n, int maxExclusive) {
        if (n > maxExclusive) {
            throw new IllegalArgumentException(String.format(
                    "Cannot select %d unique indices from a range of %d.", n, maxExclusive));
        }

        Set<Integer> uniqueIndices = HashSet.newHashSet(n);
        ThreadLocalRandom currentRandom = ThreadLocalRandom.current();

        while (uniqueIndices.size() < n) {
            uniqueIndices.add(currentRandom.nextInt(maxExclusive));
        }

        return new ArrayList<>(uniqueIndices);
    }

    /**
     * Simulates a Bernoulli trial with p=0.5 (Fair Coin Toss).
     * <p>
     * Provides the boolean entropy required for operations like <em>Uniform Crossover</em>,
     * determining gene inheritance direction from one of two parents.
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
     * This value serves as the basis for probabilistic thresholds (e.g., comparing against a
     * mutation rate to determine if a gene modification event should occur).
     * </p>
     *
     * @return A value in the range [0.0, 1.0).
     */
    public static double randDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Factory method that generates a new coordinate point strictly contained within a specified geometric region.
     * <p>
     * Performs a linear mapping of normalized random factors onto the dimensions of the provided
     * bounding box, ensuring the resulting point lies inside the defined bounds.
     * </p>
     *
     * @param boundingBox The 2D geometric constraints defining the valid area.
     * @param radius      The radius attribute for the new point.
     * @param type        The botanical classification (PlantType).
     * @param varietyId   The specific variety ID from the persistence layer.
     * @param varietyName The human-readable name of the variety.
     * @return A new {@link Point} instance localized within the bounding box with full metadata.
     */
    public static Point insideBoxGenerator(Rectangle2D boundingBox, double radius, PlantType type, int varietyId, String varietyName) {

        // Generate normalized factors [0.0, 1.0)
        double randomXfactor = ThreadLocalRandom.current().nextDouble();
        double randomYfactor = ThreadLocalRandom.current().nextDouble();

        // Linear Mapping (Lerp): [0,1] -> [Min, Max]
        double x = boundingBox.getMinX() + (randomXfactor * boundingBox.getWidth());
        double y = boundingBox.getMinY() + (randomYfactor * boundingBox.getHeight());

        return new Point(x, y, radius, type, varietyId, varietyName);
    }
}
