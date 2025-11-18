package service;

import model.domains.Domain;
import model.Individual;
import model.Point;
import service.strategies.OverlapQuadratic;
import service.strategies.OverlapSpatial;
import service.strategies.OverlapStrategy;
import utils.DistanceCalculator;

import java.util.List;

import java.util.*;

public class FitnessCalculator {

    // ------------------- COSTANTI DI PENALIZZAZIONE -------------------

    // Penalità molto alta applicata per ogni violazione del confine del dominio.
    // Assicura che la soluzione venga scartata se i punti sono fuori dal dominio.
    private static final double DOMAIN_PENALTY = 10000.0;

    // Peso moltiplicativo applicato alla penalità di sovrapposizione.
    // L'uso di un peso alto spinge l'AG a risolvere prima le collisioni.
    private static final double OVERLAP_WEIGHT = 100.0;

    // Soglia critica per la commutazione di strategia.
    // Sotto questo numero di punti (N), il metodo O(N^2) è più veloce dell'O(N) a causa dell'overhead di setup.
    private static final int HASHING_THRESHOLD = 80;

    // ------------------- ATTRIBUTI -------------------

    // Riferimento al dominio geometrico (il vincolo di confine).
    private final Domain currentDomain;
    private final DistanceCalculator distanceCalculator;

    // Strategie di overlap (istanziamo una volta sola, risparmiando risorse)
    private final OverlapStrategy quadraticStrategy;
    private final OverlapStrategy spatialStrategy;

    // ------------------- COSTRUTTORE -------------------
    /**
     * Inizializza il calcolatore di fitness.
     * @param domain Il dominio geometrico.
     * @param maxRadius Il raggio massimo assoluto tra tutti i punti (costante per il problema).
     */
    public FitnessCalculator(Domain domain, double maxRadius) {
        this.currentDomain = domain;
        this.distanceCalculator = new DistanceCalculator(); // Helper per la distanza

        // Inizializzazione delle due strategie O(N^2) e O(N).
        // Passiamo maxRadius solo alla strategia spaziale che ne ha bisogno.
        this.quadraticStrategy = new OverlapQuadratic();
        this.spatialStrategy = new OverlapSpatial(maxRadius);
    }

    // ------------------- METODO PRINCIPALE -------------------
    /**
     * Calcola il valore di fitness per un dato individuo. La strategia è ibrida.
     * @param individual L'individuo da valutare.
     * @return Il valore di fitness (valore più alto è migliore, max 1.0).
     */
    public double getFitness(Individual individual) {
        List<Point> chromosomes = individual.getChromosomes();
        int n = chromosomes.size();
        double totalPenalty = 0.0;

        // 1️⃣ Penalità di dominio (Complessità O(N))
        // Controlla che ogni punto sia confinato all'interno del dominio.
        for (Point p : chromosomes) {
            if (currentDomain.isPointOutside(p.getX(), p.getY())) {
                totalPenalty += DOMAIN_PENALTY;
            }
        }

        // 2️⃣ Penalità di overlap: Logica di commutazione Ibrida
        if (n <= HASHING_THRESHOLD) {
            // Per N piccoli, l'overhead della struttura dati O(N) non ripaga.
            totalPenalty += quadraticStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        } else {
            // Per N grandi, l'efficienza O(N) medio è necessaria.
            totalPenalty += spatialStrategy.calculateOverlap(
                    chromosomes, OVERLAP_WEIGHT, distanceCalculator
            );
        }

        // 3️⃣ Conversione finale (Minimizzazione della Penalità -> Massimizzazione della Fitness)
        // Formula standard: F = 1 / (1 + Penalità Totale).
        return 1.0 / (1.0 + totalPenalty);
    }

}

