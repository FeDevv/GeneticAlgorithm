package org.agroplanner.gasystem.controllers;

import org.agroplanner.gasystem.model.GeneticConfig;
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
 * <p><strong>The Engine Room: Genetic Algorithm Orchestrator.</strong></p>
 *
 * <p>This service encapsulates the high-level logic of the Evolutionary Process.
 * It acts as a <strong>Facade</strong> for the Genetic Algorithm subsystem, coordinating:</p>
 * <ul>
 * <li>Initialization (via {@link PopulationFactory}).</li>
 * <li>Operators (Selection, Crossover, Mutation).</li>
 * <li>Evaluation (via {@link FitnessCalculator}).</li>
 * <li>Safety Mechanisms (Timeouts and Deep Protection).</li>
 * </ul>
 */
public class EvolutionService {

    // ------------------- CONTEXT STATE -------------------

    private final Domain domain;
    private final int individualSize;
    private final double pointRadius;

    // ------------------- OPERATORS & HELPERS -------------------

    /** Evaluates the quality of solutions. */
    private final FitnessCalculator fitnessCalculator;

    /** Introduces random variations (The "Gamma Rays"). */
    private final Mutation gammaRays;

    /** Mixes genetic material (The "Mixer"). */
    private final Crossover mixer;

    /** Selects parents and survivors. */
    private final Selection selector;

    /** Creates the initial population. */
    private final PopulationFactory populationFactory;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the Evolutionary Engine.
     *
     * @param domain         The geometric problem domain.
     * @param individualSize The number of genes (points) per individual.
     * @param pointRadius    The radius of each point.
     * @throws InvalidInputException     If inputs are negative or zero.
     * @throws DomainConstraintException If the requested point size is physically too large for the domain.
     */
    public EvolutionService(Domain domain, int individualSize, double pointRadius) {

        // 1. DEEP PROTECTION: Basic Input Validation
        if (individualSize <= 0) {
            throw new InvalidInputException("Individual size must be strictly positive.");
        }
        if (pointRadius <= 0) {
            throw new InvalidInputException("Point radius must be strictly positive.");
        }

        // 2. DEEP PROTECTION: Cross-Field Logic Check
        // Ensure the requested point isn't larger than the domain itself.
        // We check against half the smallest dimension of the bounding box.
        double minDomainDim = Math.min(domain.getBoundingBox().getWidth(), domain.getBoundingBox().getHeight());
        if (pointRadius > minDomainDim / 2.0) {
            throw new DomainConstraintException("pointRadius", "Point is too big for this domain.");
        }

        this.domain = domain;
        this.individualSize = individualSize;
        this.pointRadius = pointRadius;

        // COMPONENT WIRING
        // Instantiate the specialized operators required for the lifecycle.
        this.populationFactory = new PopulationFactory(domain, individualSize, pointRadius);
        this.fitnessCalculator = new FitnessCalculator(domain, pointRadius);

        // Operators are configured with global constants from GeneticConfig
        this.gammaRays = new Mutation(GeneticConfig.MUTATION_PROB, GeneticConfig.INITIAL_MUTATION_STRENGTH, domain, GeneticConfig.GENERATIONS);
        this.mixer = new Crossover(GeneticConfig.CROSSOVER_PROB);
        this.selector = new Selection(GeneticConfig.TOURNAMENT_SIZE, GeneticConfig.ELITES_PERCENTAGE);
    }

    // ------------------- CORE LOGIC -------------------

    /**
     * Executes a full evolutionary simulation cycle (e.g., 800 generations).
     *
     * <p><strong>Flow:</strong>
     * <ol>
     * <li><strong>Genesis:</strong> Create random initial population.</li>
     * <li><strong>Evolution Loop:</strong> Iterate for N generations.
     * <ul>
     * <li>Check Timeout (Safety Guard).</li>
     * <li><strong>Elitism:</strong> Save best individuals.</li>
     * <li><strong>Reproduction:</strong> Generate children via Tournament -> Crossover -> Mutation.</li>
     * <li><strong>Evaluation:</strong> Calculate fitness (Parallelized).</li>
     * </ul>
     * </li>
     * <li><strong>Result:</strong> Return the absolute best solution found.</li>
     * </ol>
     * </p>
     *
     * @return The best {@link Individual} evolved during this cycle.
     * @throws EvolutionTimeoutException If the process exceeds the calculated time budget.
     */
    public Individual executeEvolutionCycle() {

        // 1. Setup Time Budget (Safety Mechanism against infinite loops or excessive lag)
        long maxDurationMs = EvolutionUtils.calculateTimeBudget(individualSize);
        Instant startTime = Instant.now();

        // 2. Genesis (Phase 1)
        List<Individual> population = populationFactory.createFirstGeneration();

        // Initial Evaluation (Parallelized for performance)
        population.parallelStream().forEach(ind ->
                ind.setFitness(fitnessCalculator.getFitness(ind))
        );

        // Track Global Best (The "King")
        Individual bestSoFar = EvolutionUtils.findBestSolution(population, null);



        // 3. Evolution Loop (Phase 2)
        for (int i = 0; i < GeneticConfig.GENERATIONS; i++) {

            // --- TIME GUARD CHECK ---
            // Verify execution time at the start of each generation.
            Instant now = Instant.now();
            long elapsedMs = Duration.between(startTime, now).toMillis();

            if (elapsedMs > maxDurationMs) {
                throw new EvolutionTimeoutException(
                        String.format("Evolution timed out after %d ms (Limit: %d ms). Processed %d/%d generations.",
                                elapsedMs, maxDurationMs, i, GeneticConfig.GENERATIONS)
                );
            }

            // Snapshot current generation for read-access during parallel processing
            final List<Individual> currentGeneration = population;
            List<Individual> newGeneration = new ArrayList<>(GeneticConfig.POPULATION_SIZE);

            // Elitism (Preservation)
            List<Individual> elites = selector.selectElites(currentGeneration);
            newGeneration.addAll(elites);

            // Reproduction (Innovation)
            // Generate the rest of the population using Parallel Streams.
            int childrenToGenerate = GeneticConfig.POPULATION_SIZE - elites.size();
            final int currentGenerationAge = i;

            List<Individual> children = IntStream.range(0, childrenToGenerate)
                    .parallel() // <--- CRITICAL: Enables multi-core processing
                    .mapToObj(j -> {
                        // 1. Selection (Tournament)
                        Individual dad = selector.tournament(currentGeneration);
                        Individual mom = selector.tournament(currentGeneration);

                        // Prevent asexual reproduction (optional, but good practice)
                        while (mom == dad) {
                            mom = selector.tournament(currentGeneration);
                        }

                        // 2. Crossover (Mixing)
                        Individual child = mixer.uniformCrossover(mom, dad);

                        // 3. Mutation (Variation) - In-place modification
                        gammaRays.mutate(child, currentGenerationAge);

                        // 4. Evaluation (Fitness) - Done immediately on the new child
                        child.setFitness(fitnessCalculator.getFitness(child));

                        return child;
                    })
                    .toList(); // Collect to immutable list

            newGeneration.addAll(children);

            // Update Records
            bestSoFar = EvolutionUtils.findBestSolution(newGeneration, bestSoFar);

            // Advance Generation
            population = newGeneration;
        }

        // Return a defensive copy to detach the result from internal logic references.
        return bestSoFar.copy();
    }

    /**
     * Validates if a solution is fully compliant with the domain geometry.
     * <p>
     * Used by the UI Controller to determine if a retry is needed (e.g., if the best solution
     * still has points outside the boundary).
     * </p>
     *
     * @param ind The individual to check.
     * @return {@code true} if valid, {@code false} otherwise.
     */
    public boolean isValidSolution(Individual ind) {
        return domain.isValidIndividual(ind);
    }

}
