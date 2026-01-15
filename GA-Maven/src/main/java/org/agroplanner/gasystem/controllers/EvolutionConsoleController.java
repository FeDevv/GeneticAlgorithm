package org.agroplanner.gasystem.controllers;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.views.EvolutionViewContract;
import org.agroplanner.shared.exceptions.EvolutionTimeoutException;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;

import java.time.Duration;
import java.time.Instant;

/**
 * Controller component orchestrating the interactive evolutionary session.
 * <p>
 * <strong>Resilience Strategy:</strong>
 * Genetic Algorithms are stochastic processes. A single run might fail due to "unlucky" initialization.
 * This controller implements an automatic <strong>Retry Loop</strong> (up to {@value #MAX_RETRY_ATTEMPTS} attempts)
 * to smooth out statistical variance before reporting a failure to the user.
 * </p>
 */
public class EvolutionConsoleController {

    // ------------------- DEPENDENCIES -------------------

    private final EvolutionViewContract view;
    private final EvolutionService service;

    // ------------------- CONFIGURATION -------------------

    /**
     * The persistence threshold.
     * <p>Defines how many times the system attempts to find a valid solution.
     * Value 3 is chosen as a trade-off between robustness (fixing bad seeds) and user latency.</p>
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the controller with required subsystems.
     *
     * @param view    The IO handler.
     * @param service The evolutionary engine.
     */
    public EvolutionConsoleController(EvolutionViewContract view, EvolutionService service) {
        this.view = view;
        this.service = service;
    }

    // ------------------- FLOW LOGIC -------------------

    /**
     * Executes the evolutionary process with built-in fault tolerance.
     *
     * <p><strong>Algorithm:</strong></p>
     * <ol>
     * <li><strong>Execution:</strong> Invokes the Evolution Service.</li>
     * <li><strong>Validation:</strong> Checks if the best individual found satisfies all hard constraints.</li>
     * <li><strong>Branching:</strong>
     * <ul>
     * <li><em>Success:</em> Returns the solution immediately.</li>
     * <li><em>Soft Failure (Invalid Solution):</em> Increments attempt counter and Retries (if attempts < Max).</li>
     * <li><em>Hard Failure (Timeout):</em> Propagates exception immediately (No Retry).</li>
     * </ul>
     * </li>
     * </ol>
     *
     * @return The best valid {@link Individual} found.
     * @throws EvolutionTimeoutException    If the calculation exceeds the time budget. Not caught locally to enforce "Fail Fast".
     * @throws MaxAttemptsExceededException If the system fails to converge to a valid solution after N attempts.
     */
    public Individual runEvolution() throws EvolutionTimeoutException, MaxAttemptsExceededException {
        view.showEvolutionStart();

        int currentAttempt = 0;
        double totalTimeMs = 0;
        Individual lastSolution = null;

        do {
            currentAttempt++;
            Instant start = Instant.now();

            // STEP 1: EXECUTION
            // Delegate to the Service.
            // This blocking call might throw EvolutionTimeoutException.
            lastSolution = service.executeEvolutionCycle();

            // Note on Timeout Handling:
            // it is intentional to NOT catch EvolutionTimeoutException here.
            // If the service times out, it indicates a resource exhaustion or excessive complexity.
            // Retrying would likely result in another timeout, degrading UX.
            // Therefore, we let the exception bubble up to abort the session.

            Instant end = Instant.now();
            double durationMs = Duration.between(start, end).toMillis();
            totalTimeMs += durationMs;

            // STEP 2: VALIDATION
            // Even if the GA finished, the "Best" solution might still have minor overlaps
            // or boundary violations if the penalty didn't drive fitness high enough.
            if (service.isValidSolution(lastSolution)) {
                // SUCCESS CASE:
                view.showSuccess(currentAttempt, totalTimeMs / 1000.0);
                return lastSolution;
            }

            // STEP 3: RECOVERY (Soft Failure)
            // The algorithm converged, but the solution is technically invalid.
            // This might be due to a local optimum. We try again with a new random seed (implicit in next call).
            view.showRetryWarning(currentAttempt, MAX_RETRY_ATTEMPTS, durationMs / 1000.0);

        } while (currentAttempt < MAX_RETRY_ATTEMPTS);


        // ==================================================================================
        // STEP 4: CRITICAL FAILURE (Exhausted Strategy)
        // ==================================================================================
        // If we reach this point, the probabilistic recovery failed N times.
        // We act as a Gateway: throwing a checked exception forces the MainController
        // to handle the "Session Abort" flow cleanly.
        String failureDetails = String.format(
                "Converge failed after %d attempts (%.2fs).%n   -> Best invalid fitness found: %.6f",
                MAX_RETRY_ATTEMPTS,
                totalTimeMs / 1000.0,
                lastSolution.getFitness()
        );

        throw new MaxAttemptsExceededException(failureDetails);
    }

    /**
     * Delegates the result visualization to the View.
     *
     * @param solution The solution to display.
     */
    public void handleResultDisplay(Individual solution) {
        view.showSolutionValue(solution.getFitness(), solution.getDimension());
        if (view.askIfPrintDetails()) {

            view.printDetailedReport(solution);
        }
    }

}
