package org.agroplanner.gasystem.views;


import java.util.Locale;

/**
 * Concrete implementation of the Evolutionary View targeting the Command Line Interface (CLI).
 *
 * <p><strong>Architecture & UX:</strong></p>
 * <ul>
 * <li><strong>Responsibility:</strong> Translates the internal state of the {@link org.agroplanner.gasystem.controllers.EvolutionConsoleController}
 * into human-readable text feedback.</li>
 * <li><strong>Visual Grammar:</strong> Utilizes distinct visual cues (ASCII separators, Emojis) to create a
 * "Scannable" log output. This allows the user to instantly recognize Success, Failure, or Processing states
 * without reading every word.</li>
 * <li><strong>Localization Safety:</strong> Enforces {@link Locale#US} for numerical formatting. This ensures
 * that floating-point metrics (e.g., execution time) are displayed consistently (using dots for decimals),
 * preventing confusion or parsing errors on systems with different regional settings.</li>
 * </ul>
 *
 * <p><strong>Static Analysis:</strong>
 * Suppresses {@code java:S106} because writing to Standard Output is the intended behavior for this CLI component.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleEvolutionView implements EvolutionViewContract {

    // ------------------- HELPER METHODS -------------------

    /**
     * Prints a visual delimiter to organize the output sections.
     */
    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────");
    }

    // ------------------- STATUS UPDATES -------------------

    /**
     * {@inheritDoc}
     * <p><strong>Visual Feedback:</strong>
     * Uses a specific icon (🧬) and a separator to mark the beginning of a heavy computational block.
     * </p>
     */
    @Override
    public void showEvolutionStart() {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 🧬  EVOLUTION ENGINE STARTED");
        System.out.println("     Processing generations... Please wait.");
        printSingleSeparator();
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Feedback Logic:</strong></p>
     * Differentiates between a "Soft Retry" (trying again) and a "Hard Stop" (max attempts reached).
     * Uses {@code printf} with {@code Locale.US} to ensure time formatting is consistent (e.g., "1.50 sec" vs "1,50 sec").
     */
    @Override
    public void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec) {
        // Nessun separatore qui per mantenere il log compatto se ci sono molti retry
        System.out.println("\n⚠️  CONVERGENCE ISSUE DETECTED");
        System.out.printf("   Attempt %d of %d failed to find a perfect solution.%n", currentAttempt, maxAttempts);

        if (currentAttempt < maxAttempts) {
            // Soft Failure: System is recovering.
            System.out.printf(Locale.US, "   🔄 Retrying... (Est. time: ~%.2f sec)%n", lastExecutionTimeSec);
        } else {
            // Hard Failure: System is giving up.
            System.out.println("   🛑 Max attempts reached. Returning best available result.");
        }
    }

    /**
     * {@inheritDoc}
     * <p><strong>Visual Feedback:</strong>
     * Highlights the success state clearly (✅) and provides a tabular summary of the performance metrics.
     * </p>
     */
    @Override
    public void showSuccess(int attempt, double executionTimeSec) {
        System.out.println("\n✅ VALID SOLUTION FOUND!");
        // Allineamento tabellare semplice
        System.out.printf("   Attempt       : #%d%n", attempt);
        System.out.printf(Locale.US, "   Execution Time: %.2f seconds%n", executionTimeSec);
        printSingleSeparator();
    }
}

