package org.agroplanner.optimizer.views;

public interface OptimizerViewContract {
    void showWelcomeMessage();
    void showNewSessionMessage();
    void showExitMessage();

    // Input configurazione sessione
    int askForIndividualSize();
    double askForPointRadius(double maxLimit);

    // Visualizzazione Risultati
    void showSolutionValue(double fitness);
    boolean askIfPrintChromosome();
    void printSolutionDetails(String details);
    /**
     * Mostra un messaggio di interruzione di flusso e ritorno al menu principale.
     * @param reason Il motivo dell'interruzione.
     */
    void showSessionAborted(String reason);
}
