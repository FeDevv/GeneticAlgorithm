package utils;

import model.Point;

public class PenaltyHelper {
    /**
     * Calcola la penalità di overlap tra due punti, se si sovrappongono.
     * @param p_i Primo punto.
     * @param p_j Secondo punto.
     * @param overlapWeight Il peso da applicare alla penalità.
     * @param distanceCalculator L'utility per la distanza.
     * @return La penalità (0.0 se non c'è overlap).
     */
    public static double calculatePairPenalty(
            Point p_i, Point p_j,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double r_i = p_i.getRadius();
        double r_j = p_j.getRadius();

        // Distanza minima richiesta.
        double requiredDistance = r_i + r_j;
        // Distanza effettiva tra i centri.
        double actualDistance = distanceCalculator.getDistance(p_i, p_j);

        // Condizione di sovrapposizione
        if (actualDistance < requiredDistance) {
            // Calcola l'entità dell'overlap.
            double overlap = requiredDistance - actualDistance;

            // Applica la Penalità Quadratica: (overlap * overlap) * peso
            return (overlap * overlap) * overlapWeight;
        }

        return 0.0;
    }
}
