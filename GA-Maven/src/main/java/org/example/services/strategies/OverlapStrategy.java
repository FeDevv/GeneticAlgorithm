package org.example.services.strategies;

import org.example.model.Point;
import org.example.utils.DistanceCalculator;

import java.util.List;

/**
 * Definisce l'interfaccia per tutte le strategie di calcolo della penalità di overlap.
 */
public interface OverlapStrategy {

    /**
     * Calcola la penalità totale derivante dalla sovrapposizione tra i punti.
     * * @param chromosomes La lista dei punti (cromosomi) da valutare.
     * @param overlapWeight Il peso da applicare alla penalità di sovrapposizione.
     * /@param distanceCalculator Un riferimento al metodo che calcola la distanza euclidea.
     * @return La penalità totale calcolata.
     */
    double calculateOverlap(
            List<Point> chromosomes,
            double overlapWeight,
            DistanceCalculator distanceCalculator
    );
}
