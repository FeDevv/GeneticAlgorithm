package utils;

import model.Point;

import java.awt.geom.Rectangle2D;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class RandomUtils {

    // ------------------- METODI DI UTILITÀ -------------------

    /**
     * Genera un elenco di 'n' indici univoci compresi tra 0 (incluso) e 'maxExclusive' (escluso).
     *
     * @param n Il numero di indici univoci da generare (tournament size).
     * @param maxExclusive Il limite superiore del range (es. la dimensione della popolazione).
     * @return Una List<Integer> di indici univoci.
     */
    public static List<Integer> UniqueIndices(int n, int maxExclusive) {
        Set<Integer> uniqueIndices = new HashSet<>();
        // Usa ThreadLocalRandom per eliminare la contesa tra thread
        ThreadLocalRandom currentRandom = ThreadLocalRandom.current();

        while (uniqueIndices.size() < n) {
            uniqueIndices.add(currentRandom.nextInt(maxExclusive));
        }

        return new ArrayList<>(uniqueIndices);
    }

    /**
     * Simula un lancio di moneta.
     * @return 0 o 1.
     * Usato per decisioni binarie (es. Uniform Crossover).
     */
    public static int CoinToss() {
        // Usa ThreadLocalRandom
        return ThreadLocalRandom.current().nextInt(2);
    }

    /**
     * Genera un numero double casuale tra [0.0 (incluso) e 1.0 (escluso)).
     * Usato principalmente per controllare le probabilità (es. Probabilità di Crossover o Mutazione).
     */
    public static double randDouble() {
        // Usa ThreadLocalRandom
        return ThreadLocalRandom.current().nextDouble();
    }

    /**
     * Genera un nuovo oggetto Point posizionato casualmente all'interno del Bounding Box del dominio.
     * Questo metodo è cruciale per l'inizializzazione della prima generazione.
     * @param boundingBox Il rettangolo che definisce i limiti massimi (es. Rectangle2D).
     * @param radius Il raggio dell'oggetto Point da creare.
     * @return Un nuovo oggetto Point posizionato casualmente.
     * * Scelta Implementativa: Uso di ThreadLocalRandom.
     * Questa è una scelta **eccellente** per la thread safety e le prestazioni in ambienti concorrenti
     * (anche se l'AG non è parallelo, è una buona pratica per le utility statiche).
     */
    public static Point insideBoxGenerator(Rectangle2D boundingBox, double radius) {
        // Genera due fattori casuali per X e Y tra [0.0 e 1.0).
        double randomXfactor = ThreadLocalRandom.current().nextDouble();
        double randomYfactor = ThreadLocalRandom.current().nextDouble();

        // Mappatura lineare per ottenere la coordinata X all'interno del range [MinX, MaxX]
        double x = boundingBox.getMinX() + (randomXfactor * boundingBox.getWidth());
        // Mappatura lineare per ottenere la coordinata Y all'interno del range [MinY, MaxY]
        double y = boundingBox.getMinY() + (randomYfactor * boundingBox.getHeight());

        return new Point(x,y,radius);
    }
}
