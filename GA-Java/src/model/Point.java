package model;

public class Point {
    // ------------------- ATTRIBUTI (Stato del Gene) -------------------

    // Le coordinate spaziali del centro dell'oggetto.
    private final double x;
    private final double y;

    // Il raggio (dimensione) dell'oggetto.
    private final double radius;

    /*
     * Scelta Implementativa: Uso di 'final' per tutti i campi.
     * Questo rende la classe Point **totalmente immutabile**.
     * * Beneficio: Quando un individuo è sottoposto a Mutazione o Crossover,
     * il gene originale non viene mai modificato; viene sempre **creato un nuovo Point**.
     * Questo previene bug di riferimento e semplifica notevolmente la gestione dell'isolamento genetico,
     * specialmente nella classe Crossover.
     */

    // ------------------- COSTRUTTORI -------------------

    /**
     * Costruttore parziale usato per il solo posizionamento (es. test o calcoli di distanza
     * dove il raggio non è rilevante per l'overlap).
     * * @param x Coordinata X.
     * @param y Coordinata Y.
     * * Nota: Imposta il raggio a un valore massimo come segnaposto.
     */
    public Point(double x, double y) {
        this.x = x;
        this.y = y;
        this.radius = Double.MAX_VALUE; // Valore di placeholder, potenzialmente pericoloso se usato in calcolo fitness/overlap.
    }

    /**
     * Costruttore totale: Usato per creare i geni completi utilizzati nell'algoritmo genetico.
     * * @param x Coordinata X.
     * @param y Coordinata Y.
     * @param radius Raggio dell'oggetto.
     */
    public Point(double x, double y, double radius) {
        this.x = x;
        this.y = y;
        this.radius = radius;
    }

    // ------------------- GETTER PUBBLICI -------------------

    /** Ritorna la coordinata X. */
    public double getX() { return x; }

    /** Ritorna la coordinata Y. */
    public double getY() { return y; }

    /** Ritorna il raggio dell'oggetto. */
    public double getRadius() { return radius; }

    // ------------------- UTILITY -------------------

    /**
     * Ritorna una rappresentazione testuale formattata del punto.
     */
    @Override
    public String toString() {
        return String.format("(%.4f, %.4f)", this.x, this.y);
    }
}
