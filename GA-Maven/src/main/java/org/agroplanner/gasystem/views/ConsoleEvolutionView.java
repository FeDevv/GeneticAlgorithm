package org.agroplanner.gasystem.views;


import java.util.Locale;

/**
 * <p><strong>Concrete View for Evolution Status.</strong></p>
 *
 * <p>Displays real-time feedback during the execution of the genetic algorithm.
 * Focuses on system status updates, timing metrics, and retry notifications.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleEvolutionView implements EvolutionViewContract {

    // ------------------- HELPER METHODS -------------------

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────");
    }

    // ------------------- STATUS UPDATES -------------------

    @Override
    public void showEvolutionStart() {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 🧬  EVOLUTION ENGINE STARTED");
        System.out.println("     Processing generations... Please wait.");
        printSingleSeparator();
    }

    @Override
    public void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec) {
        // Nessun separatore qui per mantenere il log compatto se ci sono molti retry
        System.out.println("\n⚠️  CONVERGENCE ISSUE DETECTED");
        System.out.printf("   Attempt %d of %d failed to find a perfect solution.%n", currentAttempt, maxAttempts);

        if (currentAttempt < maxAttempts) {
            System.out.printf(Locale.US, "   🔄 Retrying... (Est. time: ~%.2f sec)%n", lastExecutionTimeSec);
        } else {
            System.out.println("   🛑 Max attempts reached. Returning best available result.");
        }
    }

    @Override
    public void showSuccess(int attempt, double executionTimeSec) {
        System.out.println("\n✅ VALID SOLUTION FOUND!");
        // Allineamento tabellare semplice
        System.out.printf("   Attempt       : #%d%n", attempt);
        System.out.printf(Locale.US, "   Execution Time: %.2f seconds%n", executionTimeSec);
        printSingleSeparator();
    }
}

