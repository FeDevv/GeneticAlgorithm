package org.agroplanner.shared.utils;

import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.gasystem.model.Point;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

/**
 * <p><strong>Utility Class for Stochastic Operations.</strong></p>
 *
 * <p>This class centralizes all random number generation logic used by the Genetic Algorithm.
 * It is designed for high-performance concurrent environments.</p>
 *
 * <p><strong>Architectural Choice: ThreadLocalRandom</strong><br>
 * Since the Evolution Engine uses {@code parallelStream()}, standard {@code java.util.Random} would cause
 * thread contention (waiting for locks). {@code ThreadLocalRandom} avoids this by keeping an isolated
 * seed for each thread, ensuring linear scalability.</p>
 */
public class RandomUtils {

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Private constructor to prevent instantiation.
     * This is a static utility class.
     */
    private RandomUtils() {
        throw new IllegalStateException("Utility class");
    }

    // ------------------- CORE METHODS -------------------

    /**
     * Generates a list of distinct integers within a range.
     * <p>Used primarily for <strong>Tournament Selection</strong> to pick N distinct candidates from the population.</p>
     *
     * @param n            The number of unique indices required.
     * @param maxExclusive The upper bound of the range (0 to maxExclusive-1).
     * @return A list of unique integers.
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
     * Simulates a binary decision (Coin Flip).
     * <p>Used for <strong>Uniform Crossover</strong> to decide inheritance (Mom vs Dad).</p>
     *
     * @return 0 or 1.
     */
    public static int coinToss() {
        // Usa ThreadLocalRandom
        return ThreadLocalRandom.current().nextInt(2);
    }

    /**
     * Generates a normalized random probability.
     * <p>Used for rate checks (e.g., "Is random() < mutationProbability?").</p>
     *
     * @return A double value between 0.0 (inclusive) and 1.0 (exclusive).
     */
    public static double randDouble() {
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Returns the current thread's Random instance.
     * <p>Required for methods like {@link java.util.Collections#shuffle(List, Random)}.</p>
     */
    public static Random getRandom() {
        return ThreadLocalRandom.current();
    }

    /**
     * Generates a random Point strictly within the given Bounding Box.
     * <p>Used for the initialization of the <strong>First Generation</strong>.</p>
     *
     * @param boundingBox The geometric limits (minX, minY, width, height).
     * @param radius      The fixed radius of the new point.
     * @return A new Point instance with randomized coordinates.
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
