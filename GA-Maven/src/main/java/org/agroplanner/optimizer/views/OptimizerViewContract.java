package org.agroplanner.optimizer.views;

/**
 * Defines the abstract contract for the <strong>Main Application View</strong>.
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Scope:</strong> Unlike subsystem views (which handle specific tasks like Domain definition or Export),
 * this view manages the <strong>Global Lifecycle</strong> of the application.</li>
 * <li><strong>Responsibility:</strong> Handles "Meta-Interactions" such as:
 * <ul>
 * <li>Session State transitions (Welcome, New Session, Exit).</li>
 * <li>Global Error Reporting (Displaying exceptions caught by the main safety net).</li>
 * <li>High-level flow control (Restart vs Terminate).</li>
 * </ul>
 * </li>
 * </ul>
 */
public interface OptimizerViewContract {

    // ------------------- LIFECYCLE MESSAGES -------------------

    /**
     * Renders the application branding header / splash screen.
     * <p>Sets the tone and confirms the application has started correctly.</p>
     */
    void showWelcomeMessage();

    /**
     * Renders a visual delimiter indicating the start of a fresh problem-solving cycle.
     * <p>Crucial for CLI readability to distinguish between consecutive runs in the same shell window.</p>
     */
    void showNewSessionMessage();

    /**
     * Renders the shutdown message before the JVM terminates.
     */
    void showExitMessage();


    // ------------------- RESULTS & FEEDBACK -------------------

    /**
     * Displays the primary metric of the optimization result.
     * @param fitness The normalized fitness score (0.0 to 1.0) of the best solution found.
     */
    void showSolutionValue(double fitness);

    /**
     * Prompts the user to decide whether to dump the raw solution data to the console.
     * <p><strong>UX Pattern (Verbosity Control):</strong>
     * For large populations (e.g., N=1000), printing the full coordinate list is spammy and slow.
     * This gate allows the user to opt-in for verbose output only when needed.
     * </p>
     *
     * @return {@code true} to print details, {@code false} to skip.
     */
    boolean askIfPrintChromosome();

    /**
     * Renders the full textual representation of the solution (The Phenotype).
     * @param details The formatted string containing the list of coordinates/genes.
     */
    void printSolutionDetails(String details);

    /**
     * Displays a critical error notification indicating that the current session has crashed
     * but the application is recovering.
     *
     * @param reason The descriptive cause of the abortion (usually the Exception message).
     */
    void showSessionAborted(String reason);

    /**
     * Prompts the user to determine the next action after a session ends (Success or Failure).
     *
     * @return {@code true} to restart the loop (New Session), {@code false} to terminate the application.
     */
    boolean askForNewSession();
}
