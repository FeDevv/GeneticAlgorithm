package view;

public class EvolutionConsoleView {
    // --- MESSAGGIO DI INIZIALIZZAZIONE ---

    public void displayStartMessage(int generations, int populationSize) {
        System.out.println("\n🧬 Starting Genetic Algorithm...");
        System.out.printf("Configuration: %d Generations | %d Individuals.\n\n",
                generations, populationSize);
        System.out.println("Executing Evolutionary Cycle ...");
    }

    // --- MESSAGGI DI TENTATIVO / RIPROVA ---

    public void displayRetryWarning(int currentAttempt, int maxAttempts, double lastTimeSecs) {
        System.out.println("⚠️ WARNING: Invalid solution found.");
        System.out.printf("Attempt #%d of %d\n", currentAttempt, maxAttempts);
        if (currentAttempt!=maxAttempts) {
            System.out.printf("Estimated time for next attempt: ~%.2f seconds.\n", lastTimeSecs);
        }
    }

    // --- MESSAGGI DI RISULTATO ---

    public void displaySuccess(int attempt, double timeSecs) {
        System.out.println("\n✅ Success! Valid solution found at attempt #" + attempt + ".");
        System.out.printf("Execution time: %.2f seconds.\n\n", timeSecs);
    }

    /* Teoricamente lascio la visualizzazione del risultato ad altre classi, non al controller.
    *   public static void displayFinalSolution(double fitness, String details) {
    *       System.out.printf("Fitness Finale: %.6f\n", fitness);
    *       System.out.println("Dettagli Soluzione: " + details);
    *   }
    */


    // --- MESSAGGI DI ERRORE ---

    public void displayCriticalFailure(int maxAttempts, double lastFitness, double totalTimeSecs) {
        System.err.println("\n--- 🛑 CRITICAL FAILURE ---");
        System.err.printf("Could not find a valid solution after %d complete evolutionary cycles (total %.2f seconds of computation).\n",
                maxAttempts, totalTimeSecs);
        System.err.printf("Fitness of best invalid individual found: %.6f\n", lastFitness);
    }
}
