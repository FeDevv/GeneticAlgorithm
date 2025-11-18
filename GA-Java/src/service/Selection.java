package service;

import model.Individual;
import utils.RandomUtils;

import java.util.*;

/**
 * Selezionare i migliori individui per dare il via
 * a una nuova generazione
 * */
public class Selection {

    // ------------------- ATTRIBUTI (Parametri di Configurazione) -------------------

    // Il numero di individui che partecipano a ogni torneo.
    private final int tournamentSize;

    // La percentuale della popolazione da considerare élite (es. 0.05 per 5%).
    private final double elitesPercentage;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Costruttore: prepara l'oggetto Selezione con la popolazione su cui operare e i parametri.
     * @param tournamentSize La dimensione N del torneo.
     * @param elitesPercentage La percentuale di individui da preservare come élite.
     * * Scelta Implementativa: Uso di Collections.unmodifiableList().
     * **Sicurezza:** Questo garantisce che la popolazione passata sia trattata come **immutabile** * all'interno di questa classe, impedendo modifiche esterne durante l'esecuzione della selezione.
     */
    public Selection(int tournamentSize, double elitesPercentage) {
        this.tournamentSize = tournamentSize;
        this.elitesPercentage = elitesPercentage;
    }

    // ------------------- METODI DI SELEZIONE -------------------

    /**
     * Implementa la strategia di **Elitismo**: seleziona i migliori X% degli individui
     * da copiare direttamente nella prossima generazione (non-degeneration strategy).
     * @return Una lista contenente i riferimenti agli individui élite.
     * * Scelta Implementativa: Uso di PriorityQueue.
     * La PriorityQueue mantiene solo i k migliori (dove k è eliteSize), con una complessità efficiente O(N log k).
     */
    public List<Individual> selectElites(List<Individual> oldPopulation) {
        int populationSize = oldPopulation.size();
        // Calcola la dimensione degli élite, garantendo che sia almeno 1.
        int eliteSize = Math.max(1 , (int)Math.floor(populationSize * elitesPercentage));

        // Inizializza la coda di priorità. Il Comparator garantisce che il 'peggiore tra i migliori'
        // sia in testa (elites.peek()) per essere rimosso se ne subentra uno migliore.
        PriorityQueue<Individual> elites = new PriorityQueue<>(eliteSize, Comparator.comparingDouble(Individual::getFitness));

        for (Individual ind : oldPopulation) {
            if (elites.size() < eliteSize) {
                elites.offer(ind);
            } else if (ind.getFitness() > Objects.requireNonNull(elites.peek()).getFitness()) {
                // Rimuovi il peggiore tra gli élite (che è in testa alla coda)
                elites.poll();
                // Inserisci il nuovo individuo migliore
                elites.offer(ind);
            }
        }

        // Converte la PriorityQueue in una List e la restituisce.
        return elites.stream().toList();
    }

    /**
     * Implementa la **Selezione per Torneo**: estrae N individui a caso e sceglie il migliore.
     * Questo metodo viene usato per selezionare i due genitori (padre e madre) per il crossover.
     * @return L'individuo vincitore del torneo (quello con la fitness più alta).
     * * Scelta Implementativa: Utilizzo di RandomUtils.UniqueIndices.
     * Assicura che la selezione degli N partecipanti sia casuale e senza duplicati.
     */
    public Individual tournament(List<Individual> oldPopulation) {
        // Estrae N indici univoci dalla popolazione
        List<Integer> indices = RandomUtils.UniqueIndices(tournamentSize, oldPopulation.size());
        List<Individual> participants = new ArrayList<>();

        for (int i : indices) {
            participants.add(oldPopulation.get(i));
        }

        // Trova il vincitore tra i partecipanti (il migliore ha la fitness più alta)
        Individual winner = participants.getFirst();
        for (Individual individual : participants) {
            if (winner.getFitness() < individual.getFitness()) {
                winner = individual;
            }
        }
        return winner;
    }
}
