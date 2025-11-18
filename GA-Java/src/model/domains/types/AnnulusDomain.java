package model.domains.types;

import model.Individual;
import model.Point;
import model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Implementa l'interfaccia Domain definendo un'area non convessa a forma di corona circolare (Annulus).
 * * Il dominio è l'area tra due cerchi concentrici, entrambi centrati all'origine (0, 0).
 */
public class AnnulusDomain implements Domain {

    // ------------------- ATTRIBUTI -------------------

    private final double innerRadius;
    private final double outerRadius;

    // La Bounding Box è definita dal cerchio esterno.
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio Annulus.
     * @param innerRadius Raggio del buco centrale.
     * @param outerRadius Raggio del confine esterno.
     * @throws IllegalArgumentException Se i raggi non sono positivi o se innerRadius >= outerRadius.
     */
    public AnnulusDomain(double innerRadius, double outerRadius) {

        if (innerRadius <= 0 || outerRadius <= 0) {
            throw new IllegalArgumentException("Both radii must be strictly positive (> 0).");
        }
        // Condizione essenziale: il buco interno deve essere strettamente più piccolo del confine esterno.
        if (innerRadius >= outerRadius) {
            throw new IllegalArgumentException("Inner radius must be strictly smaller than outer radius to form an annulus.");
        }

        this.innerRadius = innerRadius;
        this.outerRadius = outerRadius;

        // La Bounding Box è il quadrato che contiene il cerchio esterno.
        this.boundingBox = new Rectangle2D.Double(
                -outerRadius,
                -outerRadius,
                outerRadius * 2,
                outerRadius * 2
        );
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) si trova al di fuori del dominio Annulus.
     * Il punto è fuori se: (È fuori dal cerchio esterno) OR (È dentro il cerchio interno/buco).
     * Complessità: O(1).
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // Distanza del punto dall'origine (centro del cerchio)
        double distanceSquared = x * x + y * y;

        // Quadrati dei raggi (per evitare la radice quadrata)
        double innerRadiusSq = innerRadius * innerRadius;
        double outerRadiusSq = outerRadius * outerRadius;

        // --- Logica d'inclusione/esclusione ---

        // 1. Controllo Contenitore Esterno: Il punto deve essere DENTRO il cerchio esterno.
        // Se distanceSquared > outerRadiusSq, è fuori dal contenitore.
        boolean isOutsideOuter = distanceSquared > outerRadiusSq;

        // 2. Controllo Buco Interno: Il punto deve essere FUORI dal cerchio interno.
        // Se distanceSquared < innerRadiusSq, è dentro il buco (quindi è fuori dal dominio Annulus).
        boolean isInsideHole = distanceSquared < innerRadiusSq;

        // Il punto è fuori dal dominio se è: (Fuori dal contenitore esterno) OR (Dentro il buco interno).
        return isOutsideOuter || isInsideHole;
    }

    /**
     * Verifica se un intero individuo rispetta il vincolo di confine.
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Ritorna la Bounding Box del dominio (il quadrato contenitore del cerchio esterno).
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }
}
