package service.strategies;

import model.Point;
import utils.DistanceCalculator;
import utils.PenaltyHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategia di calcolo dell'overlap basata sull'Hashing Spaziale (Griglia Uniforme).
 * Questa implementazione riduce la complessità media da O(N^2) a O(N) ed è usata per popolazioni grandi.
 */
public class OverlapSpatial implements OverlapStrategy{

    // Record per rappresentare la chiave della cella nella griglia.
    // Essendo un record, fornisce automaticamente un'implementazione corretta di
    // hashCode() e equals(), essenziale per l'uso come chiave in una HashMap.
    private record Cell(int i, int j) {}

    // Dimensione fissa del lato di una cella quadrata.
    // Viene calcolata una sola volta nel costruttore.
    private final double cellSize;

    /**
     * Costruttore della strategia di Hashing Spaziale.
     * @param maxRadius Il raggio massimo assoluto tra tutti i punti nel problema.
     */
    public OverlapSpatial(double maxRadius) {
        // La dimensione della cella è fissata a 2 * R_max.
        // Questo garantisce che un cerchio con raggio r_i possa interagire solo con i punti
        // nelle celle immediatamente adiacenti (un'area 3x3).
        this.cellSize = 2.0 * maxRadius;
    }

    /**
     * Mappa una coordinata continua (X o Y) all'indice intero discreto della cella (i o j).
     * Si basa sulla CELL_SIZE fissa.
     */
    private int getCellIndex(double coordinate) {
        // Utilizza Math.floor per mappare in modo coerente i punti all'indice intero corretto.
        return (int) Math.floor(coordinate / cellSize);
    }

    /**
     * Calcola la penalità totale derivante dalla sovrapposizione utilizzando la griglia spaziale.
     * Complessità: O(N) per la costruzione + O(N) per il controllo = O(N) totale medio.
     * * @param chromosomes La lista dei punti da valutare.
     * @param overlapWeight Il peso da applicare alla penalità.
     * @param distanceCalculator L'utility per calcolare la distanza.
     * @return La penalità totale di overlap.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double penalty = 0.0;

        // La griglia Map<Cell, List<Point>> è locale al metodo e viene ricostruita
        // ad ogni chiamata O(N), poiché le posizioni dei punti cambiano di continuo (mutazione/crossover).
        Map<Cell, List<Point>> grid = new HashMap<>();

        // Fase 1: Popolamento griglia (Complessità O(N))
        // Mappa ogni punto alla sua cella (i, j).
        for (Point p : chromosomes) {
            int i = getCellIndex(p.getX());
            int j = getCellIndex(p.getY());
            // Aggiunge il punto alla lista associata alla chiave Cell(i, j), creando la lista se necessario.
            grid.computeIfAbsent(new Cell(i, j), k -> new ArrayList<>()).add(p);
        }

        // Fase 2: Controllo overlap locale (Complessità O(N) medio)
        // Per ogni punto, controlla solo un numero costante (9) di celle.
        for (Point p_i : chromosomes) {
            int iCell = getCellIndex(p_i.getX());
            int jCell = getCellIndex(p_i.getY());

            // Ciclo 3x3: Itera attraverso gli offset [-1, 0, 1] per gli indici i e j.
            for (int di = -1; di <= 1; di++) {
                for (int dj = -1; dj <= 1; dj++) {

                    // Recupera i vicini nella cella corrente/adiacente (iCell + di, jCell + dj)
                    List<Point> neighbors = grid.get(new Cell(iCell + di, jCell + dj));
                    if (neighbors == null) continue;

                    for (Point p_j : neighbors) {

                        // 1. Evita l'auto-confronto (p_i con sé stesso).
                        if (p_i == p_j) continue;

                        // 2. Evita il doppio conteggio: Forziamo un ordinamento (p_i vs p_j, ma non p_j vs p_i).
                        // System.identityHashCode fornisce un ID univoco basato sull'oggetto, garantendo che l'ordine
                        // sia deterministico e che ogni coppia venga processata una sola volta.
                        if (System.identityHashCode(p_i) > System.identityHashCode(p_j)) continue;

                        penalty += PenaltyHelper.calculatePairPenalty(p_i, p_j, overlapWeight, distanceCalculator);

                    }
                }
            }
        }
        return penalty;
    }

}
