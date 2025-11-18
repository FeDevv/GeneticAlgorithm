package model.domains.types;

import model.Individual;
import model.Point;
import model.domains.Domain;

import java.awt.geom.Rectangle2D;
import java.util.List;

public class CircleDomain implements Domain {

    // ------------------- ATTRIBUTI -------------------

    // Raggio del cerchio che definisce il dominio.
    private final double radius;

    // La Bounding Box: il quadrato che contiene perfettamente il cerchio, centrato su (0, 0).
    private final Rectangle2D boundingBox;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Crea un dominio circolare con il raggio specificato, centrato su (0, 0).
     * @param radius Il raggio del cerchio (deve essere positivo).
     * @throws IllegalArgumentException Se il raggio non è positivo.
     * * Scelta Implementativa: Il controllo all'inizio garantisce che l'oggetto sia sempre valido.
     */
    public CircleDomain(double radius) {

        // Validazione di Integrità (Deep Defense):
        // Nonostante i controlli "fail-fast" nel Factory/Controller,
        // il costruttore garantisce che l'oggetto non sia mai creato in uno stato illegale.
        if (radius <= 0) {
            throw new IllegalArgumentException("The radius must be positive");
        }
        // L'uso di 'final' garantisce l'immutabilità del dominio.
        this.radius = radius;

        // La Bounding Box va da [-radius, -radius] a [radius, radius],
        // con larghezza (radius * 2) e altezza (radius * 2).
        // Scelta Implementativa: Utilizzo di Rectangle2D per definire i limiti di inizializzazione e mutazione.
        this.boundingBox = new Rectangle2D.Double(-radius, -radius, radius*2, radius*2);
    }

    // ------------------- IMPLEMENTAZIONE INTERFACCIA DOMAIN -------------------

    /**
     * Verifica se un punto con coordinate (x, y) è all'interno del dominio circolare.
     * @param x Coordinata X del punto.
     * @param y Coordinata Y del punto.
     * @return True se il punto è all'interno o sul bordo del cerchio.
     * * Scelta Implementativa: Utilizzo dell'equazione del cerchio (distanza euclidea al quadrato).
     * Non calcolare la radice quadrata (Math.hypot o Math.sqrt) rende il controllo più veloce.
     */
    @Override
    public boolean isPointOutside(double x, double y) {
        // Equazione: x² + y² <= r²
        // sto facendo !(isPointInside)
        return !((x * x + y * y) <= (radius * radius));
    }

    /**
     * Verifica se un intero individuo (tutti i suoi punti) rispetta il vincolo di confine.
     * Complessità: O(N), dove N è il numero di punti nell'individuo.
     * @param individual L'individuo da validare.
     * @return True se tutti i punti sono all'interno del cerchio.
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
     * @return L'oggetto Rectangle2D che incapsula il cerchio.
     * * Ruolo nell'AG: Fornisce i limiti di coordinate per l'inizializzazione casuale dei Punti
     * e per l'applicazione del soft-clamping nella classe Mutation.
     */
    @Override
    public Rectangle2D getBoundingBox() {
        return this.boundingBox;
    }
}
