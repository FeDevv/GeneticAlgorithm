package org.agroplanner.gasystem.model;

/**
 * Classe di configurazione globale per i parametri dell'Algoritmo Genetico.
 * Contiene costanti utilizzate per il tuning dell'evoluzione.
 */
public final class GeneticConfig {

    /**
     * Costruttore privato per prevenire l'istanziazione della classe utility.
     * SonarCloud richiede questo per le classi che contengono solo static members.
     */
    private GeneticConfig() {
        throw new IllegalStateException("Utility class cannot be instantiated");
    }

    /** Numero massimo di generazioni da eseguire. */
    public static final int GENERATIONS = 800;

    /** Dimensione fissa di ogni popolazione in ogni generazione. */
    public static final int POPULATION_SIZE = 100;

    /** Numero di individui selezionati per il torneo durante la selezione. */
    public static final int TOURNAMENT_SIZE = 3;

    /** Percentuale della popolazione (gli individui migliori) da preservare tramite elitismo. */
    public static final double ELITES_PERCENTAGE = 0.05;

    /** Probabilità di eseguire l'operatore di Crossover su una coppia di genitori. */
    public static final double CROSSOVER_PROB = 0.9;

    /**
     * Forza iniziale dell'operatore di Mutazione.
     * Utilizzata, ad esempio, per ricottura simulata o riduzione progressiva.
     */
    public static final double INITIAL_MUTATION_STRENGTH = 1.0;

    /** Probabilità di eseguire l'operatore di Mutazione su un gene (Point) di un nuovo individuo. */
    public static final double MUTATION_PROB = 0.02;
}
