package org.agroplanner.gasystem.views;

/**
 * Defines the abstract contract for the presentation layer of the Evolutionary Subsystem.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Pattern:</strong> View Interface (Dependency Inversion). It decouples the orchestration logic
 * ({@link org.agroplanner.gasystem.controllers.EvolutionConsoleController}) from the specific output mechanism.</li>
 * <li><strong>UX Responsibility:</strong> Since the Genetic Algorithm is a blocking, CPU-intensive operation,
 * this interface plays a critical role in <strong>Latency Management</strong>. It provides "Heartbeat" feedback
 * (Start -> Retry -> Success) to assure the user that the system is working and has not frozen.</li>
 * </ul>
 */
public interface EvolutionViewContract {

    /**
     * Signals the initiation of the computationally intensive evolutionary cycle.
     * <p>
     * <strong>UX Purpose:</strong> Sets user expectations regarding processing time.
     * Since the thread might block for several seconds/minutes, an immediate visual cue is required.
     * </p>
     */
    void showEvolutionStart();

    /**
     * Renders a notification regarding a transient failure and subsequent recovery attempt.
     *
     * <p><strong>Stochastic Visibility:</strong></p>
     * Genetic Algorithms are probabilistic. Sometimes a run fails to converge due to a "bad seed".
     * This method makes the <strong>Auto-Recovery Mechanism</strong> transparent to the user, explaining
     * why the process is taking longer than usual (i.e., "Attempt 1 failed, trying Attempt 2...").
     *
     * @param currentAttempt       The 1-based index of the failed attempt.
     * @param maxAttempts          The total retry budget.
     * @param lastExecutionTimeSec The duration of the failed run (performance metric).
     */
    void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec);

    /**
     * Displays visual confirmation of a successful convergence to a valid solution.
     *
     * @param attempt          The attempt index that succeeded.
     * @param executionTimeSec The cumulative wall-clock time required to find the solution.
     */
    void showSuccess(int attempt, double executionTimeSec);

}
