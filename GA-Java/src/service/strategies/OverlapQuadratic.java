package service.strategies;

import model.Point;
import utils.DistanceCalculator;
import utils.PenaltyHelper;

import java.util.List;

/**
 * Strategia di calcolo dell'overlap con complessità quadratica O(N^2).
 * * Questa strategia è la più semplice da implementare ed è preferita
 * quando il numero di punti (N) è basso (sotto la soglia di commutazione),
 * perché evita l'overhead di costruzione delle strutture dati spaziali O(N).
 */
public class OverlapQuadratic implements OverlapStrategy{

    /**
     * Calcola la penalità totale derivante dalla sovrapposizione tra tutti i punti.
     * * Il metodo confronta ogni punto con tutti gli altri in un doppio ciclo annidato.
     * Complessità Totale: O(N^2).
     * * @param chromosomes La lista dei punti (cromosomi) da valutare.
     * @param overlapWeight Il peso da applicare alla penalità (costante).
     * @param distanceCalculator L'utility per calcolare la distanza euclidea.
     * @return La penalità totale di overlap.
     */
    @Override
    public double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double penalty = 0.0;
        int n = chromosomes.size();

        // Ciclo esterno: Seleziona il punto di riferimento p_i. Complessità O(N).
        for (int i = 0; i < n; i++) {
            Point p_i = chromosomes.get(i);

            // Ciclo interno: Confronta p_i con tutti i punti successivi p_j. Complessità O(N).
            // L'indice j = i + 1 è cruciale per due motivi:
            // 1. Evita l'auto-confronto (i vs i).
            // 2. Garantisce che ogni coppia (i, j) sia contata ESATTAMENTE una volta,
            //    impedendo il doppio conteggio della penalità (i vs j e j vs i).
            for (int j = i + 1; j < n; j++) {
                Point p_j = chromosomes.get(j);

                penalty += PenaltyHelper.calculatePairPenalty(p_i, p_j, overlapWeight, distanceCalculator);

            }
        }
        return penalty;
    }
}
