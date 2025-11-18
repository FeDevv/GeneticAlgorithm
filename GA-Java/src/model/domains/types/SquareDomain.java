package model.domains.types;

import model.Individual;
import model.Point;
import model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

/**
 * Implementa l'interfaccia Domain definendo un'area di vincolo di forma quadrata.
 * * Il dominio è centrato sulle coordinate (0, 0) per semplicità geometrica.
 * Questo dominio è immutabile e funge da vincolo spaziale per l'Algoritmo Genetico.
 */
public class SquareDomain implements Domain {

    // ------------------- ATTRIBUTI -------------------

    // Lato (larghezza = altezza) del dominio quadrato.
    private final double side;

    // La Bounding Box è il quadrato stesso, immutabile e centrale.
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio quadrato centrato su (0, 0) con il lato specificato.
     * @param side La lunghezza del lato del quadrato (deve essere > 0).
     */
    public SquareDomain(double side) {

        // Validazione di Integrità (Deep Defense):
        // Nonostante i controlli "fail-fast" nel Factory/Controller,
        // il costruttore garantisce che l'oggetto non sia mai creato in uno stato illegale.
        if (side <= 0) {
            throw new IllegalArgumentException("The side must be positive");
        }

        this.side = side;

        // La Bounding Box definisce l'area:
        // Inizia a [-side/2, -side/2] (angolo superiore sinistro) con larghezza=side e altezza=side.
        this.boundingBox = new Rectangle2D.Double(-side/2 , -side/2 , side, side);
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) è al di fuori dei confini del dominio.
     * Complessità: O(1).
     * @param x Coordinata X del punto.
     * @param y Coordinata Y del punto.
     * @return True se il punto è fuori dal quadrato.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // La condizione controlla se il punto è all'interno della metà positiva e negativa
        // del lato su entrambi gli assi (x e y).
        // !((isPointInside))
        return !((x >= -side/2) && (x <= side/2) && (y >= -side/2) && (y <= side/2));
    }

    /**
     * Verifica se un intero individuo (tutti i suoi punti) rispetta il vincolo di confine.
     * Complessità: O(N), dove N è il numero di punti nell'individuo.
     * @param individual L'individuo da validare.
     * @return True se tutti i punti sono all'interno del quadrato.
     */
    @Override
    public boolean isValidIndividual(Individual individual) {
        List<Point> points = individual.getChromosomes();
        for (Point p : points) {
            // Delega la verifica della posizione al metodo isPointOutside().
            if (isPointOutside(p.getX(), p.getY())) { return false; }
        }
        return true;
    }

    /**
     * Restituisce la Bounding Box del dominio.
     * * Questo metodo è fondamentale per l'inizializzazione casuale dei Punti
     * e per il soft-clamping della Mutazione, fornendo i limiti geometrici.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }
}
