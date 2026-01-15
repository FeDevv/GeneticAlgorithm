package org.agroplanner.gasystem.views;

import org.agroplanner.gasystem.model.Individual;

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

    void showEvolutionStart();

    void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec);

    void showSuccess(int attempt, double executionTimeSec);

    // --- NUOVI METODI (Spostati da SystemView) ---

    /** Chiede all'utente se vuole vedere il fenotipo dettagliato (Tabella). */
    boolean askIfPrintDetails();

    /** Stampa la tabella formattata. */
    void printDetailedReport(Individual individual);

    void showSolutionValue(double fitness, int totalPlants);
}
