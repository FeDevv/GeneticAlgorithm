package model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Individual {
    // I cromosomi, ovvero la sequenza di geni (Punti).
    // È una lista mutabile INTERNA, ma il suo riferimento (final List) non cambia.
    private final List<Point> chromosomes;

    // Il valore di fitness (qualità) calcolato da FitnessCalculator.
    // È mutabile, poiché viene aggiornato dopo ogni ciclo di valutazione.
    private double fitness;

    // ------------------- COSTRUTTORI -------------------

    /**
     * Costruttore principale: crea un nuovo individuo partendo da una lista di cromosomi.
     * * @param chromosomes La lista di Point (geni) che definiscono la soluzione.
     * * Scelta Implementativa: Uso di new ArrayList<>(chromosomes).
     * Questa è una **copia superficiale** della lista. Poiché la classe Point è immutabile (i suoi campi sono final),
     * questa copia garantisce l'**isolamento genetico**: due individui non condividono la stessa lista di cromosomi,
     * prevenendo corruzioni se uno dei due viene modificato (ad esempio, durante la mutazione).
     */
    public Individual(List<Point> chromosomes) {
        // Copia superficiale per isolamento genetico
        this.chromosomes = new ArrayList<>(chromosomes);
        this.fitness = Double.NEGATIVE_INFINITY; // Inizializza la fitness a un valore pessimo
    }

    /**
     * Costruttore completo, usato spesso per operazioni di copia o inizializzazione post-calcolo.
     */
    public Individual(List<Point> chromosomes, double fitness) {
        // Uso diretto per semplicità, assumendo che i costruttori chiamanti passino già una copia sicura.
        this.chromosomes = new ArrayList<>(chromosomes);
        this.fitness = fitness;
    }

    // ------------------- GETTER & SETTER -------------------

    /**
     * Ritorna una vista immutabile della lista dei cromosomi.
     * @return Una lista non modificabile.
     * * Scelta Implementativa: Uso di Collections.unmodifiableList().
     * **Sicurezza:** Questo impedisce che codice esterno (come Crossover o EvolutionEngine)
     * possa accidentalmente o intenzionalmente modificare la lista interna (aggiungendo o rimuovendo elementi),
     * preservando l'integrità della dimensione dell'individuo.
     */
    public List<Point> getChromosomes() {
        return Collections.unmodifiableList(this.chromosomes);
    }

    /**
     * Permette la mutazione di un singolo gene (Punto).
     * @param index L'indice del cromosoma da sostituire.
     * @param point Il nuovo oggetto Point mutato.
     * * Scelta Implementativa: Setter mirato.
     * È il metodo **unico** che permette agli operatori genetici (come Mutation) di alterare lo stato genetico.
     * Permette di bypassare la vista immutabile del getter (necessario per Mutation.set(i, newPoint)).
     */
    public void setChromosome(int index, Point point) {
        this.chromosomes.set(index, point);
    }

    /**
     * Setter necessario per aggiornare il valore di qualità dopo il calcolo della fitness.
     */
    public void setFitness(double fitness) {
        this.fitness = fitness;
    }

    /** Ritorna il valore di fitness. */
    public double getFitness() {
        return this.fitness;
    }

    /** Ritorna il numero di geni (Punti) nell'individuo. */
    public int getDimension() {
        return chromosomes.size();
    }

    // ------------------- UTILITY -------------------

    /**
     * Crea una copia profonda dell'individuo (necessario per restituire il risultato finale
     * o per clonazioni esplicite se richieste).
     * @return Una nuova istanza di Individual con una nuova lista di cromosomi.
     */
    public Individual copy() {
        // Usa il getter per ottenere la lista di Point (non la lista interna mutabile)
        // e la copia nuovamente nel costruttore.
        return new Individual(new ArrayList<>(this.getChromosomes()), this.fitness);
    }

    /**
     * Rappresentazione testuale dell'individuo e dei suoi geni.
     */
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Individual : ").append("\n");
        for (Point p : chromosomes) {
            sb.append(p.toString()).append("\n");
        }
        return sb.toString();
    }
}
