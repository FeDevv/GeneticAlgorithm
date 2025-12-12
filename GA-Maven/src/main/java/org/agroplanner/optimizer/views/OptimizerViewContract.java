package org.agroplanner.optimizer.views;

/**
 * <p><strong>View Contract for the Main UC Flow.</strong></p>
 *
 * <p>This interface defines the interactions available during the main session lifecycle.
 * Unlike specific sub-views (like Domain or Export), this view handles global application states
 * such as:</p>
 * <ul>
 * <li>Welcome and Exit screens.</li>
 * <li>Global configuration inputs (Problem size, Point radius).</li>
 * <li>Session termination feedback (Aborted/Reset messages).</li>
 * </ul>
 */
public interface OptimizerViewContract {
    // ------------------- LIFECYCLE MESSAGES -------------------

    /** Displays the application header/banner. */
    void showWelcomeMessage();

    /** Displays a visual separator indicating a new problem-solving session has started. */
    void showNewSessionMessage();

    /** Displays the shutdown message. */
    void showExitMessage();

    // ------------------- CONFIGURATION INPUTS -------------------

    /**
     * Prompts the user to define the complexity of the problem (N).
     * @return The number of points (genes) to place in the domain.
     */
    int askForIndividualSize();

    /**
     * Prompts the user to define the size of the points.
     *
     * @param maxLimit The maximum mathematically valid radius for the current domain.
     * The UI should display this to guide the user.
     * @return The chosen radius.
     */
    double askForPointRadius(double maxLimit);

    // ------------------- RESULTS & FEEDBACK -------------------

    /**
     * Displays the final fitness score of the best solution found.
     * @param fitness The score (0.0 to 1.0).
     */
    void showSolutionValue(double fitness);

    /**
     * Asks the user if they want to see the verbose textual representation of the solution.
     * @return true if yes, false otherwise.
     */
    boolean askIfPrintChromosome();

    /**
     * Prints the full raw details of the solution (coordinates list).
     * @param details The string representation of the Individual.
     */
    void printSolutionDetails(String details);

    /**
     * Displays a critical error or abort message, informing the user that
     * the current session is being reset.
     *
     * @param reason The cause of the abortion (Exception message).
     */
    void showSessionAborted(String reason);
}
