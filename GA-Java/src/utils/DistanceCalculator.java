package utils;

import model.Point;

/**
 * Utilità per calcolare la distanza Euclidea tra due oggetti Point.
 * Spostata in un package di utilità per il riutilizzo in tutto il progetto.
 */
public class DistanceCalculator {

    /**
     * Calcola la distanza euclidea tra i centri di due punti.
     * @param p1 Centro del primo punto.
     * @param p2 Centro del secondo punto.
     * @return La distanza tra i centri.
     */
    public double getDistance(Point p1, Point p2) {
        double dx = p2.getX() - p1.getX();
        double dy = p2.getY() - p1.getY();
        // Math.hypot è usato per una maggiore stabilità numerica.
        return Math.hypot(dx, dy);
    }
}
