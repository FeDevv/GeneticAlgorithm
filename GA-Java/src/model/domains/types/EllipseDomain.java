package model.domains.types;

import model.Individual;
import model.Point;
import model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Implementa l'interfaccia Domain definendo un'area di vincolo di forma ellittica.
 * * Il dominio è centrato sulle coordinate (0, 0).
 * L'ellisse è definita dai suoi due semiassi: semi-larghezza (a) e semi-altezza (b).
 */
public class EllipseDomain implements Domain {

// ------------------- ATTRIBUTI  -------------------

    // Semi-larghezza (raggio sull'asse X). Corrisponde al semiasse 'a'.
    private final double semiWidth;

    // Semi-altezza (raggio sull'asse Y). Corrisponde al semiasse 'b'.
    private final double semiHeight;

    // La Bounding Box: il rettangolo che contiene perfettamente l'ellisse.
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio ellittico centrato su (0, 0) con i due semiassi specificati.
     * @param semiWidth La semi-larghezza (a) dell'ellisse.
     * @param semiHeight La semi-altezza (b) dell'ellisse.
     * @throws IllegalArgumentException Se uno dei due semiassi non è positivo.
     */
    public EllipseDomain(double semiWidth, double semiHeight) {
        // Validazione di Integrità (Deep Defense): Entrambi i semiassi devono essere strettamente positivi.
        if (semiWidth <= 0 || semiHeight <= 0) {
            throw new IllegalArgumentException("Both semi-width and semi-height must be strictly positive (> 0).");
        }

        this.semiWidth = semiWidth;
        this.semiHeight = semiHeight;

        // La Bounding Box è un rettangolo di larghezza 2*a e altezza 2*b, centrato su (0, 0).
        this.boundingBox = new Rectangle2D.Double(
                -semiWidth,
                -semiHeight,
                semiWidth * 2,
                semiHeight * 2
        );
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) si trova al di fuori del dominio ellittico.
     * Complessità: O(1).
     * @param x Coordinata X del punto.
     * @param y Coordinata Y del punto.
     * @return True se il punto è fuori dall'ellisse.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Equazione dell'ellisse: (x/a)² + (y/b)² <= 1
        // Per velocizzare il calcolo, usiamo i quadrati (similmente al CircleDomain).
        double result = (x * x / (semiWidth * semiWidth)) + (y * y / (semiHeight * semiHeight));

        // stiamo verificando !(isPointInside)
        return !(result <= 1.0);
    }

    /**
     * Verifica se un intero individuo rispetta il vincolo di confine.
     * Complessità: O(N), dove N è il numero di punti.
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
     * Ritorna la Bounding Box del dominio.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

}
