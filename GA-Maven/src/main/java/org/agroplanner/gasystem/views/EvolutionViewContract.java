package org.agroplanner.gasystem.views;

public interface EvolutionViewContract {

    // Inizio operazioni
    void showEvolutionStart();

    // Un tentativo è fallito (individuo non valido), stiamo per riprovare
    void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec);

    // Successo! Trovata soluzione valida
    void showSuccess(int attempt, double executionTimeSec);

}
