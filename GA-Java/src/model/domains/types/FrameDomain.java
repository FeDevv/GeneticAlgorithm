package model.domains.types;

import model.Individual;
import model.Point;
import model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Implementa l'interfaccia Domain definendo un'area non convessa a forma di cornice (Frame).
 * * Il dominio è l'area tra due rettangoli concentrici, entrambi centrati all'origine (0, 0).
 */
public class FrameDomain implements Domain {

    // ------------------- ATTRIBUTI -------------------

    private final double outerWidth;
    private final double outerHeight;
    private final double innerWidth;
    private final double innerHeight;

    // La Bounding Box è il rettangolo esterno (Outer Frame).
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio Frame.
     * @param outerWidth Larghezza del rettangolo esterno.
     * @param outerHeight Altezza del rettangolo esterno.
     * @param innerWidth Larghezza del buco interno.
     * @param innerHeight Altezza del buco interno.
     * @throws IllegalArgumentException Se le dimensioni non sono positive o se il buco è più grande del frame.
     */
    public FrameDomain(double innerWidth, double innerHeight, double outerWidth, double outerHeight) {

        if (innerWidth <= 0 || innerHeight <= 0 || outerWidth <= 0 || outerHeight <= 0) {
            throw new IllegalArgumentException("All dimensions must be strictly positive (> 0).");
        }
        // Il buco interno deve essere più piccolo del frame esterno per creare una cornice valida.
        if (innerWidth >= outerWidth || innerHeight >= outerHeight) {
            throw new IllegalArgumentException("Inner dimensions must be strictly smaller than outer dimensions to form a frame.");
        }

        this.outerWidth = outerWidth;
        this.outerHeight = outerHeight;
        this.innerWidth = innerWidth;
        this.innerHeight = innerHeight;

        // La Bounding Box è definita dal frame esterno. Centrata su (0, 0).
        this.boundingBox = new Rectangle2D.Double(-outerWidth / 2.0, -outerHeight / 2.0, outerWidth, outerHeight);
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) si trova al di fuori del dominio Frame.
     * Il punto è fuori se: (È fuori dal rettangolo esterno) OR (È dentro il rettangolo interno/buco).
     * Complessità: O(1).
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // --- 1. Controllo Contenitore Esterno (Outside Frame) ---
        // Controlla se il punto è fuori dal rettangolo esterno (la Bounding Box).
        boolean isOutsideOuter = (x < -outerWidth / 2.0) || (x > outerWidth / 2.0) ||
                (y < -outerHeight / 2.0) || (y > outerHeight / 2.0);

        if (isOutsideOuter) {
            return true;
        }

        // --- 2. Controllo Buco Interno (Inside Hole) ---
        // Se il punto è arrivato qui, è all'interno del rettangolo esterno.
        // Ora controlliamo se è DENTRO il buco, il che lo rende fuori dal dominio Frame.

        return (x >= -innerWidth / 2.0) && (x <= innerWidth / 2.0) &&
                (y >= -innerHeight / 2.0) && (y <= innerHeight / 2.0);
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
     * Ritorna la Bounding Box del dominio (il rettangolo esterno).
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }
}
