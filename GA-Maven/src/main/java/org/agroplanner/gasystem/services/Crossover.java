package org.agroplanner.gasystem.services;

import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * crossover function
 * given a crossover probability P_c two individuals will
 * cross their genes.
 * */
public class Crossover {

    // ------------------- ATTRIBUTI (Parametri di Configurazione) -------------------

    // La probabilità che l'operazione di crossover avvenga tra i due genitori selezionati.
    private final double crossoverProbability;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Costruisce l'operatore di Crossover con la probabilità specificata.
     * @param crossoverProbability La probabilità (tra 0.0 e 1.0) di eseguire il crossover.
     * * Scelta Implementativa: L'uso di 'final' garantisce l'immutabilità della probabilità di configurazione.
     */
    public Crossover(double crossoverProbability) {
        this.crossoverProbability = crossoverProbability;
    }

    // ------------------- METODO PRINCIPALE -------------------

    /**
     * Esegue il **Crossover Uniforme** tra due individui, generando un figlio.
     * @param i1 Il primo genitore.
     * @param i2 Il secondo genitore.
     * @return Una nuova istanza di Individual (il figlio).
     */
    public Individual uniformCrossover(Individual i1, Individual i2) {
        int lenght = i1.getDimension();
        List<Point> childChromosomes = new ArrayList<>(lenght);

        // 1. Controlla se il Crossover deve avvenire (probabilità di ricombinazione).
        if(RandomUtils.randDouble() < crossoverProbability) {

            // --- Crossover Uniforme ---
            // Scambia i geni uno per uno, con una probabilità del 50% per ogni gene.
            for (int i = 0; i < lenght; i++) {
                // Il CoinToss decide se prendere il gene da I1 (0) o I2 (1).
                if (RandomUtils.coinToss() == 0) {
                    // Si aggiunge un riferimento al Point (gene), che è sicuro perché Point è immutabile.
                    childChromosomes.add(i1.getChromosomes().get(i));
                } else {
                    childChromosomes.add(i2.getChromosomes().get(i));
                }
            }

            // Restituisce un nuovo individuo con il genoma misto.
            // Il costruttore di Individual garantisce la COPIA della lista per l'isolamento genetico.
            return new Individual(childChromosomes);

        } else {
            // 2. Crossover non avviene: Il figlio è una copia (clone) di uno dei genitori.

            // Sceglie casualmente quale dei due genitori clonare.
            if (RandomUtils.coinToss() == 0) {
                // Restituisce un nuovo individuo geneticamente identico a I1.
                return new Individual(i1.getChromosomes());
            } else {
                // Restituisce un nuovo individuo geneticamente identico a I2.
                return new Individual(i2.getChromosomes());
            }
            // * Scelta Implementativa: La creazione di un 'new Individual' assicura che anche se il crossover fallisce,
            // il figlio sia un oggetto separato, prevenendo riferimenti incrociati e permettendone la mutazione successiva.
        }
    }
}
