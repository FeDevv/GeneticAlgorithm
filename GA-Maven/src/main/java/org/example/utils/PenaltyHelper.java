package org.example.utils;

import org.example.model.Point;

public class PenaltyHelper {
    /**
     * Calcola la penalità di overlap tra due punti, se si sovrappongono.
     * @param point1 Primo punto.
     * @param point2 Secondo punto.
     * @param overlapWeight Il peso da applicare alla penalità.
     * @param distanceCalculator L'utility per la distanza.
     * @return La penalità (0.0 se non c'è overlap).
     */
    public static double calculatePairPenalty(
            Point point1, Point point2,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    ) {
        double radius1 = point1.getRadius();
        double radius2 = point2.getRadius();

        // Distanza minima richiesta.
        double requiredDistance = radius1 + radius2;
        // Distanza effettiva tra i centri.
        double actualDistance = distanceCalculator.getDistance(point1, point2);

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
