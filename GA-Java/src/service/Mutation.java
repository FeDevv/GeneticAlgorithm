package service;

import model.Individual;
import model.Point;
import model.domains.Domain;
import utils.RandomUtils;

public class Mutation {

    // ------------------- ATTRIBUTI (Parametri di Configurazione) -------------------

    // La probabilità che un singolo gene (Point) subisca una mutazione.
    private final double mutationProbability;

    // L'ampiezza massima della perturbazione applicata alle coordinate X e Y.
    private final double initialMutationStrength;

    // Riferimento al dominio per ottenere i limiti (Bounding Box) per il clamping.
    private final Domain domain;

    //
    private final int totalGenerations;

    // ------------------- COSTRUTTORE -------------------

    /**
     * Costruisce l'operatore di Mutazione con i parametri di controllo e il dominio.
     * @param mutationProbability La probabilità che la mutazione avvenga su un gene.
     * @param initialMutationStrength L'entità della perturbazione.
     * @param domain Il riferimento al dominio del problema.
     * * Scelta Implementativa: L'uso di 'final' per tutti i campi garantisce l'immutabilità della configurazione.
     */
    public Mutation(double mutationProbability, double initialMutationStrength, Domain domain, int totalGenerations) {
        this.mutationProbability = mutationProbability;
        this.initialMutationStrength = initialMutationStrength;
        this.domain = domain;
        this.totalGenerations = totalGenerations;
    }

    // ------------------- METODO PRINCIPALE -------------------

    /**
     * Esegue l'operazione di mutazione su un individuo.
     * Il processo è in-place, modificando l'individuo passato come parametro.
     * @param I L'individuo da mutare.
     */
    public void mutate(Individual I, int currentGeneration) {

        double adaptiveStrenght = calculateAdaptiveStrength(currentGeneration);

        // Estrae i limiti della Bounding Box (il rettangolo che contiene il dominio).
        // Questo viene fatto una volta per l'efficienza.
        java.awt.geom.Rectangle2D boundingBox = domain.getBoundingBox();
        double minX = boundingBox.getMinX();
        double minY = boundingBox.getMinY();
        double maxX = boundingBox.getMaxX();
        double maxY = boundingBox.getMaxY();

        // Cicla su ogni cromosoma (per ogni punto).
        for (int i = 0; i < I.getDimension(); i++) {

            // Controlla la probabilità di mutazione per questo gene.
            if (RandomUtils.randDouble() < mutationProbability) {

                Point oldPoint = I.getChromosomes().get(i);
                double radius = oldPoint.getRadius(); // Mantiene il raggio invariato.

                // Calcola la perturbazione casuale (Mutazione Gaussiana / Creep Mutation).
                // (RandomUtils.randDouble() * 2 - 1) genera un valore tra [-1.0, 1.0).
                double newX = oldPoint.getX() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;
                double newY = oldPoint.getY() + (RandomUtils.randDouble() * 2 - 1) * adaptiveStrenght;

                // 🌟 APPLICAZIONE DEL SOFT-CLAMPING
                // Forza le coordinate all'interno dei limiti della Bounding Box.
                // * Scelta Implementativa: Strategia ibrida per l'efficienza.
                // Previene la generazione di troppi individui palesemente fuori dominio,
                // riducendo il lavoro per la FitnessCalculator.
                double finalX = clamp(newX, minX, maxX);
                double finalY = clamp(newY, minY, maxY);

                // Crea un nuovo oggetto Point (necessario perché Point è immutabile).
                Point newPoint = new Point(finalX, finalY, radius);

                // Sostituisce il vecchio gene con il nuovo gene mutato (Mutazione in-place).
                // L'uso di setChromosome() è necessario per aggirare la vista immutabile del getter.
                I.setChromosome(i, newPoint);
            }
        }
        // * Scelta Implementativa: La mutazione avviene "in-place" (modificando il riferimento all'individuo I),
        // come è tipico per gli operatori genetici.
    }

    // ------------------- METODI UTILITY -------------------

    /**
     * Limita un valore 'value' all'interno di un intervallo chiuso [min, max].
     */
    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private double calculateAdaptiveStrength(int g) {
        // Per evitare divisione per zero se g=0, usiamo (g + 1).
        // Il fattore di decadimento (1.0 + (double)g / totalGenerations) è una scelta comune.

        // Esempio 1: Decadimento Lineare (Raggiunge 0 alla fine)
        // double decayFactor = 1.0 - ((double)g / this.totalGenerations);
        // return this.initialStrength * Math.max(0.01, decayFactor); // Minimo 0.01

        // Esempio 2: Decadimento Inverso (Mantiene un valore minimo)
        return this.initialMutationStrength / (1.0 + 5.0 * ((double)g / this.totalGenerations));
        // 5.0 è una costante che regola quanto velocemente la forza decade.
    }
}
