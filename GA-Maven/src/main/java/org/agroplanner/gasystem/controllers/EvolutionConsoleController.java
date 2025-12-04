package org.agroplanner.gasystem.controllers;

import org.agroplanner.shared.exceptions.MaxAttemptsExceededException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.views.EvolutionViewContract;

import java.time.Duration;
import java.time.Instant;

public class EvolutionConsoleController {

    private final EvolutionViewContract view;
    private final EvolutionService service;

    private static final int MAX_RETRY_ATTEMPTS = 3;

    public EvolutionConsoleController(EvolutionViewContract view, EvolutionService service) {
        this.view = view;
        this.service = service;
    }

    public Individual runEvolution() {
        view.showEvolutionStart();

        int currentAttempt = 0;
        double totalTimeMs = 0;
        Individual lastSolution = null;

        do {
            currentAttempt++;
            Instant start = Instant.now();

            // 1. Chiamata al servizio puro (Calcolo pesante)
            lastSolution = service.executeEvolutionCycle();

            Instant end = Instant.now();
            double durationMs = Duration.between(start, end).toMillis();
            totalTimeMs += durationMs;

            // 2. Verifica Validità (tramite servizio o dominio)
            if (service.isValidSolution(lastSolution)) {
                // SUCCESSO
                view.showSuccess(currentAttempt, totalTimeMs / 1000.0);
                return lastSolution;
            }

            // FALLIMENTO TENTATIVO
            view.showRetryWarning(currentAttempt, MAX_RETRY_ATTEMPTS, durationMs / 1000.0);

        } while (currentAttempt < MAX_RETRY_ATTEMPTS);

        // 3. FALLIMENTO CRITICO
        String failureDetails = String.format(
                "Converge failed after %d attempts (%.2fs).\n   -> Best invalid fitness found: %.6f",
                MAX_RETRY_ATTEMPTS,
                totalTimeMs / 1000.0,
                lastSolution.getFitness()
        );

        // Rilanciamo l'eccezione per fermare il flusso nell'AppController
        throw new MaxAttemptsExceededException(failureDetails);
    }
}
