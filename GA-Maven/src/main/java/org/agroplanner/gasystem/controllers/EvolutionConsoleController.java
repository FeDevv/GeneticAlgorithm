package org.agroplanner.gasystem.controllers;

import org.agroplanner.shared.exceptions.EvolutionTimeoutException;
import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.views.EvolutionViewContract;

import java.time.Duration;
import java.time.Instant;

/**
 * <p><strong>UI Controller for the Evolutionary Subsystem.</strong></p>
 *
 * <p>This controller orchestrates the execution of the Genetic Algorithm in a user-interactive session.
 * Its primary responsibility is managing the <strong>"Retry Loop"</strong> strategy:</p>
 * <ul>
 * <li>It triggers the calculation via the {@link EvolutionService}.</li>
 * <li>It evaluates the success of the result (checking validity).</li>
 * <li>It decides whether to accept the solution, retry (if invalid), or abort (if attempts are exhausted).</li>
 * </ul>
 */
public class EvolutionConsoleController {

    // ------------------- DEPENDENCIES -------------------

    private final EvolutionViewContract view;
    private final EvolutionService service;

    // ------------------- CONFIGURATION -------------------

    /**
     * The maximum number of times the system will try to find a valid solution
     * before declaring a critical failure.
     */
    private static final int MAX_RETRY_ATTEMPTS = 3;

    // ------------------- CONSTRUCTOR -------------------

    /**
     * Initializes the controller.
     *
     * @param view    The abstraction of the UI.
     * @param service The business logic engine.
     */
    public EvolutionConsoleController(EvolutionViewContract view, EvolutionService service) {
        this.view = view;
        this.service = service;
    }

    // ------------------- FLOW LOGIC -------------------

    /**
     * Runs the evolutionary session with automatic retries.
     *
     * <p><strong>Process Flow:</strong>
     * <ol>
     * <li>Display Start Message.</li>
     * <li><strong>Attempt Loop (Max 3):</strong>
     * <ul>
     * <li>Execute Evolution Cycle (Heavy Computation).</li>
     * <li>Check Validity (Constraints).</li>
     * <li>If Valid -> Return Success.</li>
     * <li>If Invalid -> Show Warning and Retry.</li>
     * </ul>
     * </li>
     * <li>If all attempts fail -> Throw Critical Exception.</li>
     * </ol>
     * </p>
     *
     * @return The best valid {@link Individual} found.
     * @throws EvolutionTimeoutException    If the calculation exceeds the time budget (Rethrown immediately).
     * @throws MaxAttemptsExceededException If the system fails to find a valid solution after N attempts.
     */
    public Individual runEvolution() {
        view.showEvolutionStart();

        int currentAttempt = 0;
        double totalTimeMs = 0;
        Individual lastSolution = null;

        try {
            do {
                currentAttempt++;
                Instant start = Instant.now();

                // EXECUTION: Delegate heavy lifting to the Service
                // This blocks until the cycle finishes or timeouts.
                lastSolution = service.executeEvolutionCycle();

                Instant end = Instant.now();
                double durationMs = Duration.between(start, end).toMillis();
                totalTimeMs += durationMs;

                // VALIDATION: Check if the best solution found is actually valid.
                // It might happen that the algorithm converged but couldn't solve all point positions.
                if (service.isValidSolution(lastSolution)) {
                    // SUCCESS: Valid solution found
                    view.showSuccess(currentAttempt, totalTimeMs / 1000.0);
                    return lastSolution;
                }

                // FAILURE (Recoverable): Invalid solution.
                // We warn the user and loop again (if attempts remain).
                view.showRetryWarning(currentAttempt, MAX_RETRY_ATTEMPTS, durationMs / 1000.0);

            } while (currentAttempt < MAX_RETRY_ATTEMPTS);
        } catch (EvolutionTimeoutException e) {
            // CRITICAL FAILURE (Timeout)
            // It rethrow the specific exception so the Main Controller
            // can handle it distinctly.
            throw e;
        }

        // CRITICAL FAILURE (Exhausted Attempts)
        // If we exit the loop, it means we failed N times.
        // We construct a detailed error report and abort the session.
        String failureDetails = String.format(
                "Converge failed after %d attempts (%.2fs).\n   -> Best invalid fitness found: %.6f",
                MAX_RETRY_ATTEMPTS,
                totalTimeMs / 1000.0,
                lastSolution.getFitness()
        );

        throw new MaxAttemptsExceededException(failureDetails);
    }
}
