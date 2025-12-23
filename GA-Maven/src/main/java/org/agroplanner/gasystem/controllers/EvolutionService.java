package org.agroplanner.gasystem.controllers;

import org.agroplanner.gasystem.model.GeneticConfig;
import org.agroplanner.inventory.model.PlantInventory;
import org.agroplanner.gasystem.services.helpers.EvolutionUtils;
import org.agroplanner.gasystem.services.helpers.PopulationFactory;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.EvolutionTimeoutException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.services.operators.Crossover;
import org.agroplanner.gasystem.services.operators.FitnessCalculator;
import org.agroplanner.gasystem.services.operators.Mutation;
import org.agroplanner.gasystem.services.operators.Selection;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Service component acting as the <strong>Orchestrator Engine</strong> of the Genetic Algorithm.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> Facade / Mediator. It encapsulates the complexity of the evolutionary operators,
 * providing a simple high-level interface ({@link #executeEvolutionCycle()}) to the Controller layer.</li>
 * <li><strong>Concurrency Strategy:</strong> Leverages Java <strong>Parallel Streams</strong> to parallelize the most
 * CPU-intensive phases (Reproduction and Evaluation). This is made thread-safe by the immutable design of the
 * {@link org.agroplanner.gasystem.model.Point} class.</li>
 * <li><strong>Reliability:</strong> Implements a "Circuit Breaker" mechanism via {@link EvolutionTimeoutException}
 * to prevent runaway processes on large datasets.</li>
 * </ul>
 */
public class EvolutionService {

    // ------------------- CONTEXT STATE -------------------

    /** The geometric boundaries of the problem. */
    private final Domain domain;

    /** The biological requirements (Recipe). */
    private final PlantInventory inventory;

    // ------------------- OPERATORS & HELPERS -------------------

    /** The objective function engine. */
    private final FitnessCalculator fitnessCalculator;

    /** The mutation operator (Diversity). */
    private final Mutation gammaRays;

    /** The crossover operator (Recombination). */
    private final Crossover mixer;

    /** The selection operator (Survival). */
    private final Selection selector;

    /** The factory for Generation 0. */
    private final PopulationFactory populationFactory;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the Evolutionary Engine and wires all dependencies.
     *
     * <p><strong>Deep Protection & Validation:</strong></p>
     * Performs sanity checks before allocating resources. For example, it verifies that the largest requested plant
     * can physically fit within the domain bounds to avoid running an impossible simulation.
     *
     * @param domain    The geometric problem domain.
     * @param inventory The plant configuration.
     * @throws InvalidInputException     If the inventory is empty.
     * @throws DomainConstraintException If geometric constraints are violated (e.g., Plant Radius > Domain Size).
     */
    public EvolutionService(Domain domain, PlantInventory inventory) {

        // 1. Basic Integrity Check
        if (inventory.getTotalPopulationSize() <= 0) {
            throw new InvalidInputException("Inventory cannot be empty. Please select at least one plant.");
        }

        // 2. Geometric Feasibility Check (Physics Constraint)
        // Check if the largest atom (plant) fits into the container.
        double maxRadius = inventory.getMaxRadius();
        double minDomainDim = Math.min(domain.getBoundingBox().getWidth(), domain.getBoundingBox().getHeight());

        // Constraint: Diameter (2*r) must be <= Smallest Dimension.
        // Or strictly: Radius <= SmallestDimension / 2.0
        if (maxRadius > minDomainDim / 2.0) {
            throw new DomainConstraintException("maxRadius",
                    String.format("The largest plant in inventory (r=%.2f) is too big for this domain.", maxRadius));
        }

        this.domain = domain;
        this.inventory = inventory;

        // 3. Component Wiring (Dependency Composition)
        this.populationFactory = new PopulationFactory(domain, inventory);
        this.fitnessCalculator = new FitnessCalculator(domain, maxRadius);

        // Operators configuration injection from GeneticConfig
        this.gammaRays = new Mutation(GeneticConfig.MUTATION_PROB, GeneticConfig.INITIAL_MUTATION_STRENGTH, domain, GeneticConfig.GENERATIONS);
        this.mixer = new Crossover(GeneticConfig.CROSSOVER_PROB);
        this.selector = new Selection(GeneticConfig.TOURNAMENT_SIZE, GeneticConfig.ELITES_PERCENTAGE);
    }

    // ------------------- CORE LOGIC -------------------

    /**
     * Executes the full evolutionary lifecycle.
     *
     *
     * <p><strong>Execution Flow:</strong></p>
     * <ol>
     * <li><strong>Genesis:</strong> Spawns the initial random population.</li>
     * <li><strong>Evolution Loop:</strong> Iterates for {@code GeneticConfig.GENERATIONS}.
     * <ul>
     * <li><em>Timeout Check:</em> Aborts if CPU time exceeds the calculated budget.</li>
     * <li><em>Elitism:</em> Secures the best individuals.</li>
     * <li><em>Parallel Reproduction:</em> Uses multi-threading to Select, Cross, and Mutate children.</li>
     * <li><em>Evaluation:</em> Computes fitness for new solutions.</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @return The absolute best {@link Individual} found during the run (detached copy).
     * @throws EvolutionTimeoutException If the simulation takes too long.
     */
    public Individual executeEvolutionCycle() throws EvolutionTimeoutException {

        // 1. Resource Estimation
        long maxDurationMs = EvolutionUtils.calculateTimeBudget(inventory.getTotalPopulationSize());
        Instant startTime = Instant.now();

        // 2. Genesis (Generation 0)
        List<Individual> population = populationFactory.createFirstGeneration();

        // Initial Evaluation (Parallelized)
        population.parallelStream().forEach(ind ->
                ind.setFitness(fitnessCalculator.getFitness(ind))
        );

        // Initialize Global Best Tracker
        Individual bestSoFar = EvolutionUtils.findBestSolution(population, null);

        // 3. Main Evolutionary Loop
        for (int i = 0; i < GeneticConfig.GENERATIONS; i++) {

            // --- SAFETY GUARD: TIMEOUT ---
            Instant now = Instant.now();
            long elapsedMs = Duration.between(startTime, now).toMillis();
            if (elapsedMs > maxDurationMs) {
                throw new EvolutionTimeoutException(
                        String.format("Evolution timed out after %d ms (Limit: %d ms). Processed %d/%d generations.",
                                elapsedMs, maxDurationMs, i, GeneticConfig.GENERATIONS)
                );
            }

            // --- PHASE A: ELITISM (Sequential) ---
            // Must be done sequentially to correctly identify the top K sorted elements.
            final List<Individual> currentGeneration = population;
            List<Individual> newGeneration = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
            List<Individual> elites = selector.selectElites(currentGeneration);
            newGeneration.addAll(elites);

            // --- PHASE B: REPRODUCTION (Parallel) ---
            // Calculating how many children we need to fill the rest of the population.
            int childrenToGenerate = GeneticConfig.POPULATION_SIZE - elites.size();
            final int currentGenerationAge = i;

            // it is used a parallel stream to generate children.
            // Since Selection, Crossover, and Mutation logic is thread-safe (stateless or immutable),
            // this scales linearly with CPU cores.
            List<Individual> children = IntStream.range(0, childrenToGenerate)
                    .parallel()
                    .mapToObj(j -> {
                        // 1. Selection (Tournament)
                        Individual dad = selector.tournament(currentGeneration);
                        Individual mom = selector.tournament(currentGeneration);

                        // Prevent asexual reproduction
                        while (mom == dad) {
                            mom = selector.tournament(currentGeneration);
                        }

                        // 2. Crossover (Recombination)
                        Individual child = mixer.uniformCrossover(mom, dad);

                        // 3. Mutation (Variation)
                        // Note: Mutation modifies the child in-place. Safe because 'child' is local to this thread.
                        gammaRays.mutate(child, currentGenerationAge);

                        // 4. Evaluation (Fitness)
                        // Immediate evaluation avoids a separate pass later.
                        child.setFitness(fitnessCalculator.getFitness(child));

                        return child;
                    })
                    .toList(); // Collect to immutable list

            newGeneration.addAll(children);

            // --- PHASE C: UPDATE ---
            // Check if we found a new champion.
            bestSoFar = EvolutionUtils.findBestSolution(newGeneration, bestSoFar);

            // Advance generation pointer
            population = newGeneration;
        }

        // Return a defensive copy to ensure the result is independent of the engine's state.
        return bestSoFar.copy();
    }

    /**
     * Checks if a specific solution satisfies all Domain Hard Constraints.
     *
     * <p><strong>Usage:</strong></p>
     * Used by the Controller/UI to verify the final result. Even if a solution is the "best found",
     * it might still be invalid (e.g., points outside boundaries). This method acts as the final gatekeeper.
     *
     * @param ind The individual to inspect.
     * @return {@code true} if all points are strictly within the domain bounds.
     */
    public boolean isValidSolution(Individual ind) {
        return domain.isValidIndividual(ind);
    }

}
