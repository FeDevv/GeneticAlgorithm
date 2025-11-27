package org.example.services.strategies;

import org.example.model.Point;
import org.example.utils.DistanceCalculator;
import org.example.utils.PenaltyHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Strategia di calcolo dell'overlap basata sull'Hashing Spaziale (Griglia Uniforme).
 * <p>
 * Questa implementazione riduce la complessità temporale media da O(N^2) a O(N).
 * È ideale per popolazioni numerose dove la densità locale non è eccessiva.
 */
public class OverlapSpatial implements OverlapStrategy {

    /**
     * Record immutabile per la chiave della mappa (coordinate discrete della griglia).
     * I record forniscono implementazioni ottimizzate di equals() e hashCode().
     */
    private record Cell(int i, int j) {}

    // Dimensione del lato di una cella. Calcolata per garantire che i check siano limitati ai vicini.
    private final double cellSize;

    /**
     * Costruttore della strategia.
     *
     * @param maxRadius Il raggio massimo possibile di un punto (o cerchio) nel problema.
     */
    public OverlapSpatial(double maxRadius) {
        // Impostiamo la cella a 2 * R_max.
        // Questo garantisce matematicamente che un cerchio possa sovrapporsi solo
        // con elementi presenti nella sua cella o nelle 8 celle adiacenti (Area 3x3).
        this.cellSize = 2.0 * maxRadius;
    }

    /**
     * Calcola la penalità totale di sovrapposizione.
     * <p>
     * L'algoritmo costruisce prima una griglia temporanea (Map) e poi itera sui punti
     * controllando solo le celle vicine, evitando il confronto quadratico "tutti contro tutti".
     *
     * @param chromosomes Lista dei punti (genoma) da valutare.
     * @param overlapWeight Fattore di penalità per ogni sovrapposizione rilevata.
     * @param distanceCalculator Utility per il calcolo delle distanze euclidee.
     * @return La penalità totale accumulata.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        // Fase 1: Indicizzazione spaziale dei punti
        Map<Cell, List<Point>> grid = populateGrid(chromosomes);

        double penalty = 0.0;

        // Fase 2: Calcolo delle collisioni usando la griglia
        for (Point referencePoint : chromosomes) {
            penalty += calculatePenaltyForPoint(referencePoint, grid, overlapWeight, distanceCalculator);
        }

        return penalty;
    }

    /**
     * Converte una coordinata continua (double) in un indice discreto (int).
     */
    private int getCellIndex(double coordinate) {
        return (int) Math.floor(coordinate / cellSize);
    }

    // Costruisce la Hash Map spaziale associando ogni punto alla sua cella di appartenenza.
    private Map<Cell, List<Point>> populateGrid(List<Point> chromosomes) {
        Map<Cell, List<Point>> grid = new HashMap<>();
        for (Point p : chromosomes) {
            int i = getCellIndex(p.getX());
            int j = getCellIndex(p.getY());
            grid.computeIfAbsent(new Cell(i, j), k -> new ArrayList<>()).add(p);
        }
        return grid;
    }

    // Calcola le penalità per un singolo punto ispezionando solo le 9 celle limitrofe (3x3).
    private double calculatePenaltyForPoint(
            Point referencePoint,
            Map<Cell, List<Point>> grid,
            double overlapWeight,
            DistanceCalculator distCalc
    ) {
        double localPenalty = 0.0;
        int iCell = getCellIndex(referencePoint.getX());
        int jCell = getCellIndex(referencePoint.getY());

        for (int di = -1; di <= 1; di++) {
            for (int dj = -1; dj <= 1; dj++) {
                List<Point> neighbors = grid.get(new Cell(iCell + di, jCell + dj));

                if (neighbors == null) continue;

                localPenalty += processNeighbors(referencePoint, neighbors, overlapWeight, distCalc);
            }
        }
        return localPenalty;
    }

    // Itera sui vicini trovati nella cella specifica e applica la penalità se necessario.
    private double processNeighbors(
            Point referencePoint,
            List<Point> neighbors,
            double overlapWeight,
            DistanceCalculator distCalc
    ) {
        double penalty = 0.0;
        for (Point neighborPoint : neighbors) {
            if (shouldSkipPair(referencePoint, neighborPoint)) continue;

            penalty += PenaltyHelper.calculatePairPenalty(referencePoint, neighborPoint, overlapWeight, distCalc);
        }
        return penalty;
    }

    // Determina se una coppia deve essere saltata per evitare auto-confronti o doppi conteggi.
    private boolean shouldSkipPair(Point p1, Point p2) {
        // 1. p1 == p2: Evita di confrontare il punto con se stesso.
        // 2. Hash > Hash: Impone un ordinamento deterministico per processare la coppia (A, B) una volta sola,
        //    evitando di contare la penalità due volte (una per A->B e una per B->A).
        return p1 == p2 || System.identityHashCode(p1) > System.identityHashCode(p2);
    }
}
