package org.example.model.domains.types;

import org.example.model.Individual;
import org.example.model.Point;
import org.example.model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

// RAT -> Right Angled Triangle
/**
 * Implementa l'interfaccia Domain definendo un'area di vincolo di forma triangolare rettangola.
 * * Il triangolo è posizionato nel primo quadrante con l'angolo retto all'origine (0, 0).
 */
public class RATDomain implements Domain {

    // ------------------- ATTRIBUTI -------------------

    private final double base;   // Lunghezza del cateto sull'asse X (base)
    private final double height; // Lunghezza del cateto sull'asse Y (altezza)

    // La Bounding Box: il rettangolo che contiene il triangolo.
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio triangolare rettangolo con le dimensioni specificate.
     * @param base La base (cateto X).
     * @param height L'altezza (cateto Y).
     * @throws IllegalArgumentException Se base o altezza non sono positivi.
     */
    public RATDomain(double base, double height) {

        if (base <= 0 || height <= 0) {
            throw new IllegalArgumentException("Base and height must be strictly positive (> 0).");
        }

        this.base = base;
        this.height = height;

        // La Bounding Box va da [0, 0] a [base, height].
        this.boundingBox = new Rectangle2D.Double(0, 0, base, height);
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) si trova al di fuori del dominio triangolare.
     * Complessità: O(1).
     */
    @Override
    public boolean isPointOutside(double x, double y) {

        // 1. Condizione di Base 1: deve essere a destra o sul bordo dell'asse Y (x >= 0)
        // 2. Condizione di Base 2: deve essere sopra o sul bordo dell'asse X (y >= 0)
        boolean outsideAxes = (x < 0) || (y < 0);
        if (outsideAxes) return true;

        // 3. Condizione dell'Ipotenusa: Il punto deve essere sopra la retta che collega (B, 0) a (0, H).
        // L'equazione della retta è y = H - (H/B) * x.
        // La condizione d'inclusione è: y > H - (H/B) * x

        // Il punto è fuori se non soddisfa la condizione dell'ipotenusa.
        // NOTA: Poiché l'intero dominio è nel primo quadrante, usiamo
        // la Bounding Box per la prima parte del controllo.

        // il punto è fuori se:
        // si trova in valori di x negativi (x < 0)
        // si trova in valori di y negativi (y < 0)
        // si trova sopra l'ipotenusa (y > H - (H/B)x)
        // le prime due condizioni sono gia controllate, manca la terza
        return (y > this.height - (this.height / this.base) * x);
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
     * Ritorna la Bounding Box del dominio.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }

    @Override
    public String toString() {
        return "Right Angled Triangle { base = " + base + ", height = " + height + " }";
    }
}
