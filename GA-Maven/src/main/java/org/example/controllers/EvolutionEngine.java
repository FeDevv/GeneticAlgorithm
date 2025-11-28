package org.example.controllers;

import org.example.exceptions.MaxAttemptsExceededException;
import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;
import org.example.services.ga.Crossover;
import org.example.services.ga.FitnessCalculator;
import org.example.services.ga.Mutation;
import org.example.services.ga.Selection;
import org.example.config.GeneticConfig;
import org.example.utils.RandomUtils;
import org.example.views.EvolutionConsoleView;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

/**
 * Motore centrale che gestisce il ciclo evolutivo di un Algoritmo Genetico (AG).
 * <p>
 * Questa classe incapsula tutte le costanti di configurazione dell'AG (dimensioni, probabilità, ecc.)
 * e orchestra l'esecuzione dei servizi (Fitness, Mutazione, Crossover, Selezione) per risolvere
 * un problema di ottimizzazione vincolato a un {@code Domain} geometrico.
 */
public class EvolutionEngine {

    // Il vincolo spaziale del problema. Definisce l'area valida per i punti degli individui.
    private final Domain domain;

    /** Lunghezza del cromosoma: numero di {@code Point} (geni) che compongono ciascun individuo. */
    private final int individualSize;

    /** Il raggio (dimensione) dei {@code Point} che compongono gli individui, rilevante per la validazione spaziale.
     * Indicherà poi il più grande raggio dei punti (se ho diversi raggi disponibili, quel valore sarà il raggio più grande)
     * */
    private final double pointRadius;

    // ------------------- SERVIZI E STATO -------------------

    // Servizi (Dipendenze): componenti funzionali dell'AG.

    // Servizio per il calcolo del valore di fitness di un individuo.
    private final FitnessCalculator fitnessCalculator;

    // Servizio per l'applicazione dell'operatore di Mutazione.
    private final Mutation gammaRays;

    // Servizio per l'applicazione dell'operatore di Crossover.
    private final Crossover mixer;

    // Servizio per l'applicazione dell'operatore di Selezione.
    private final Selection selector;

    // Componente View per la gestione dell'output e della visualizzazione dello stato evolutivo.
    private final EvolutionConsoleView view;

    // ==================================================================================
    // 🔨 COSTRUTTORE
    // ==================================================================================

    /**
     * Costruttore completo che inizializza il motore evolutivo e tutti i suoi servizi.
     * <p>
     * Le dipendenze essenziali (View e contesto spaziale) sono iniettate, mentre i servizi
     * funzionali vengono istanziati internamente utilizzando le costanti AG definite nella classe.
     *
     * @param view La View da utilizzare per l'interazione e la visualizzazione dello stato.
     * @param domain Il vincolo spaziale che definisce l'area valida per la soluzione.
     * @param individualSize La lunghezza (numero di punti) del cromosoma degli individui.
     * @param pointRadius La dimensione dei punti che compongono l'individuo.
     */
    public EvolutionEngine(EvolutionConsoleView view, Domain domain, int individualSize, double pointRadius) {
        // Inizializza tutti gli attributi finali di configurazione.
        this.view = view;
        this.domain = domain;
        this.individualSize = individualSize;
        this.pointRadius = pointRadius;

        // Inizializzazione dei servizi: sono istanze costanti (Singleton) per tutta l'esecuzione.
        // I servizi sono configurati con i parametri AG e le dipendenze necessarie.
        this.fitnessCalculator = new FitnessCalculator(domain, pointRadius);
        this.gammaRays = new Mutation(GeneticConfig.MUTATION_PROB, GeneticConfig.INITIAL_MUTATION_STRENGTH, domain, GeneticConfig.GENERATIONS);
        this.mixer = new Crossover(GeneticConfig.CROSSOVER_PROB);
        this.selector = new Selection(GeneticConfig.TOURNAMENT_SIZE, GeneticConfig.ELITES_PERCENTAGE);
    }

    // ==================================================================================
    // 🚀 STEP 1: INIZIALIZZAZIONE
    // ==================================================================================

    /**
     * Genera un singolo individuo (soluzione) con punti casuali all'interno della Bounding Box.
     * * Scelta Implementativa: Usa la Bounding Box per l'efficienza di inizializzazione.
     */
    private Individual buildIndividual() {
        List<Point> points = new ArrayList<>(individualSize);
        for (int i = 0; i < individualSize; i++) {
            // Usa il raggio del punto (this.pointRadius) per creare il Point.
            points.add(RandomUtils.insideBoxGenerator(domain.getBoundingBox(), pointRadius));
        }
        return new Individual(points);
    }

    /**
     * Crea la prima generazione di individui (popolazione iniziale) in modo casuale.
     */
    private List<Individual> firstGeneration() {
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildIndividual());
        }
        return firstGen;
    }

    // ==================================================================================
    // ♻️ STEP 2: MOTORE DI EVOLUZIONE (Metodo Pubblico)
    // ==================================================================================

    /**
     * Metodo di avvio: Esegue il ciclo evolutivo completo e restituisce la migliore soluzione trovata.
     * @return Una copia della migliore soluzione trovata globalmente.
     */
    private Individual runEvolutionCore() {
        // tengo traccia del miglior individuo
        Individual solution;

        // --- Fase 1: Inizializzazione ---
        List<Individual> oldGeneration = firstGeneration();

        // Esegui la valutazione della fitness su core CPU multipli
        // La lambda expression (ind -> ...) viene eseguita in parallelo
        oldGeneration.parallelStream().forEach(ind ->
            ind.setFitness(fitnessCalculator.getFitness(ind))
        );

        // Stabilisce la prima soluzione globale migliore.
        solution = currentBestSolution(oldGeneration, null);

        // --- Fase 2: Ciclo di Evoluzione ---
        for (int i = 0; i < GeneticConfig.GENERATIONS; i++) {

            final List<Individual> currentGeneration = oldGeneration;
            List<Individual> newGeneration = new ArrayList<>(GeneticConfig.POPULATION_SIZE);

            // 1. Elitismo: seleziona i migliori della generazione precedente.
            List<Individual> elites = selector.selectElites(oldGeneration);
            newGeneration.addAll(elites);

            // 2. Crossover e Mutazione: riempie il resto della popolazione.
            int childrenToGenerate = GeneticConfig.POPULATION_SIZE - elites.size();

            // Genera i figli in parallelo e raccoglili in una lista temporanea
            final int currentGenerationAge = i;
            List<Individual> children = IntStream.range(0, childrenToGenerate)
                    .parallel() // L'operazione chiave per eseguire i passi successivi in parallelo
                    .mapToObj(j -> {
                        // --- Operazioni di creazione del singolo figlio (Eseguite in parallelo su core diversi) ---

                        // a. Selezione di genitori distinti (Nota: il while non è efficiente in AG ma è thread-safe)
                        Individual dad = selector.tournament(currentGeneration);
                        Individual mom = selector.tournament(currentGeneration);
                        while (mom == dad) {
                            mom = selector.tournament(currentGeneration);
                        }

                        // b. Crossover
                        Individual child = mixer.uniformCrossover(mom, dad);

                        // c. Mutazione
                        gammaRays.mutate(child, currentGenerationAge);

                        // d. Calcolo Fitness (uso di fitnessCalculator.getFitness() thread-safe)
                        child.setFitness(fitnessCalculator.getFitness(child));

                        // Ritorna l'oggetto creato
                        return child;
                    })
                    // si potrebbe usare la funzione collect() che si occupa di raccogliere in modo thread-safe tutti i risultati
                    // anche toList() offre le stesse funzioni thread safe.
                    .toList(); //genera lista immutabile

            // Aggiungi tutti i figli generati in parallelo alla newGeneration
            newGeneration.addAll(children);

            // 3. Aggiornamento: Verifica il record globale (Elitismo Globale).
            solution = currentBestSolution(newGeneration, solution);

            // La nuova generazione diventa la base per la prossima iterazione.
            oldGeneration = newGeneration;
        }

        // Restituisce una copia profonda per garantire che il risultato finale sia immutabile per l'utente.
        return solution.copy();
    }

    public Individual runEvolutionEngine() {
        final int MAX_RETRY_ATTEMPTS = 3;
        int currentAttempt = 0;
        Individual lastAttemptSolution;
        double lastExecutionTimeMs;
        double totalExecutionTimeMs = 0;

        view.displayStartMessage(GeneticConfig.GENERATIONS, GeneticConfig.POPULATION_SIZE);

        do {
            Instant startTime = Instant.now();

            // aggiorna counter
            currentAttempt++;

            // 1. esecuzione del core
            lastAttemptSolution = runEvolutionCore();

            Instant endTime = Instant.now();
            lastExecutionTimeMs = Duration.between(startTime,endTime).toMillis();
            // se la soluzione viene trovata (e.g.) al secondo giro, devo indicare la somma dei tempi dei 2 giri come tempo di esecuzione
            totalExecutionTimeMs += lastExecutionTimeMs;

            // 2. verifica di validità
            if (domain.isValidIndividual(lastAttemptSolution)) {
                // mostra che una soluzione corretta è stata provata
                view.displaySuccess(currentAttempt, totalExecutionTimeMs / 1000);
                return  lastAttemptSolution.copy();
            }

            // individuo non valido, mostralo all'utente
            view.displayRetryWarning(currentAttempt, MAX_RETRY_ATTEMPTS, lastExecutionTimeMs / 1000);

        } while (currentAttempt < MAX_RETRY_ATTEMPTS);

        // 4. FALLIMENTO controllato, l'algoritmo non è riuscito a trovare una soluzione.

        view.displayCriticalFailure(MAX_RETRY_ATTEMPTS, lastAttemptSolution.getFitness(), totalExecutionTimeMs / 1000);

        throw new MaxAttemptsExceededException(
                String.format(
                        "The genetic algorithm failed after %d attempts (%.2f total seconds). " +
                                "Last Fitness value: %.4f. Try changing the parameters.",
                        MAX_RETRY_ATTEMPTS,
                        totalExecutionTimeMs / 1000.0,
                        lastAttemptSolution.getFitness()
                ));
    }

    // ==================================================================================
    // ℹ️ UTILITY
    // ==================================================================================

    /**
     * Determina e traccia il miglior individuo tra la generazione corrente e il record storico.
     * @param individuals La lista degli individui della generazione corrente.
     * @param currentSolution Il miglior individuo trovato fino a quel momento (record globale).
     * @return L'individuo con la fitness più alta (il nuovo record globale, se trovato).
     */
    private Individual currentBestSolution(List<Individual> individuals, Individual currentSolution) {
        // Trova il migliore della generazione corrente (KING)
        Individual king = individuals.getFirst();

        for (int i = 1; i < individuals.size(); i++) {
            if (king.getFitness() < individuals.get(i).getFitness()) {
                king = individuals.get(i);
            }
        }

        // Confronta con il record globale precedente.
        if (currentSolution == null) {
            return king;
        }

        // Restituisce il migliore tra il KING e il record storico.
        if (king.getFitness() > currentSolution.getFitness()) {
            return king;
        } else {
            return currentSolution;
        }
    }
}
