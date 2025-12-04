package org.agroplanner.gasystem.views;

public class ConsoleEvolutionView implements EvolutionViewContract {

    @Override
    public void showEvolutionStart() {
        System.out.println("\n🚀 Starting Evolution Engine ...");
        System.out.println("Executing Evolutionary Cycles ...");
    }

    @Override
    public void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec) {
        System.out.println("\n⚠️ WARNING: Invalid solution found.");
        System.out.printf("Attempt #%d of %d%n", currentAttempt, maxAttempts);
        if (currentAttempt != maxAttempts) {
            System.out.printf("Estimated time for next attempt: ~%.2f seconds.%n", lastExecutionTimeSec);
        }
    }

    @Override
    public void showSuccess(int attempt, double executionTimeSec) {
        System.out.println("\n✅ Success! Valid solution found at attempt #" + attempt + ".");
        System.out.printf("   -> Execution time: %.2f seconds.%n%n", executionTimeSec);
    }

}
