package org.agroplanner.gasystem.views;

/**
 * <p><strong>View Contract for the Evolutionary Process.</strong></p>
 *
 * <p>This interface defines the communication protocol between the {@code EvolutionConsoleController}
 * and the user interface. It focuses on providing real-time feedback regarding the
 * progress, retries, and final outcome of the genetic algorithm.</p>
 */
public interface EvolutionViewContract {

    /**
     * Displays a notification indicating the start of the evolutionary process.
     * <p>
     * Useful for setting user expectations
     * before the heavy computation begins.
     * </p>
     */
    void showEvolutionStart();

    /**
     * Displays a warning when a specific evolution attempt fails validation,
     * but the system is configured to retry.
     *
     * <p><strong>UX Purpose:</strong> informs the user that the algorithm hasn't crashed,
     * but is simply restarting to find a better solution.</p>
     *
     * @param currentAttempt       The attempt number that just failed.
     * @param maxAttempts          The maximum number of allowed retries.
     * @param lastExecutionTimeSec The time taken by the failed attempt (in seconds).
     */
    void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec);

    /**
     * Displays a success message upon finding a valid solution.
     *
     * @param attempt          The attempt number in which the solution was found.
     * @param executionTimeSec The total time elapsed across all attempts (in seconds).
     */
    void showSuccess(int attempt, double executionTimeSec);

}
