package org.example.views;

@SuppressWarnings("java:S106")
public class EvolutionConsoleView {
    // --- MESSAGGIO DI INIZIALIZZAZIONE ---

    public void displayStartMessage() {
        System.out.println("\nExecuting Evolutionary Cycle ...");
    }

    // --- MESSAGGI DI TENTATIVO / RIPROVA ---

    public void displayRetryWarning(int currentAttempt, int maxAttempts, double lastTimeSecs) {
        System.out.println("\n⚠️ WARNING: Invalid solution found.");
        System.out.printf("Attempt #%d of %d%n", currentAttempt, maxAttempts);
        if (currentAttempt!=maxAttempts) {
            System.out.printf("Estimated time for next attempt: ~%.2f seconds.%n", lastTimeSecs);
        }
    }

    // --- MESSAGGI DI RISULTATO ---

    public void displaySuccess(int attempt, double timeSecs) {
        System.out.println("\n✅ Success! Valid solution found at attempt #" + attempt + ".");
        System.out.printf("   -> Execution time: %.2f seconds.%n%n", timeSecs);
    }

    // Lascio la visualizzazione del risultato ad altre classi.

    // --- MESSAGGI DI ERRORE ---

    public void displayCriticalFailure(int maxAttempts, double lastFitness, double totalTimeSecs) {
        System.err.println("\n--- 🛑 CRITICAL FAILURE ---");
        System.err.printf("Could not find a valid solution after %d complete evolutionary cycles (total %.2f seconds of computation).%n",
                maxAttempts, totalTimeSecs);
        System.err.printf("Fitness of best invalid individual found: %.6f%n", lastFitness);
    }
}
