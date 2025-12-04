package org.agroplanner.gasystem.controllers;

import org.agroplanner.gasystem.model.GeneticConfig;
import org.agroplanner.shared.exceptions.DomainConstraintException;
import org.agroplanner.shared.exceptions.InvalidInputException;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.domainsystem.model.Domain;
import org.agroplanner.gasystem.services.Crossover;
import org.agroplanner.gasystem.services.FitnessCalculator;
import org.agroplanner.gasystem.services.Mutation;
import org.agroplanner.gasystem.services.Selection;
import org.agroplanner.shared.utils.RandomUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class EvolutionService {

    private final Domain domain;
    private final int individualSize;
    private final double pointRadius;

    // Servizi interni
    private final FitnessCalculator fitnessCalculator;
    private final Mutation gammaRays;
    private final Crossover mixer;
    private final Selection selector;

    public EvolutionService(Domain domain, int individualSize, double pointRadius) {

        // DEEP PROTECTION
        if (individualSize <= 0) {
            throw new InvalidInputException("Individual size must be strictly positive.");
        }
        if (pointRadius <= 0) {
            throw new InvalidInputException("Point radius must be strictly positive.");
        }
        // Validazione Cross-Field: Il raggio del punto non può essere più grande del dominio stesso!
        double minDomainDim = Math.min(domain.getBoundingBox().getWidth(), domain.getBoundingBox().getHeight());
        if (pointRadius > minDomainDim / 2.0) {
            throw new DomainConstraintException("pointRadius", "Point is too big for this domain.");
        }

        this.domain = domain;
        this.individualSize = individualSize;
        this.pointRadius = pointRadius;

        // Inizializzazione servizi interni
        this.fitnessCalculator = new FitnessCalculator(domain, pointRadius);
        this.gammaRays = new Mutation(GeneticConfig.MUTATION_PROB, GeneticConfig.INITIAL_MUTATION_STRENGTH, domain, GeneticConfig.GENERATIONS);
        this.mixer = new Crossover(GeneticConfig.CROSSOVER_PROB);
        this.selector = new Selection(GeneticConfig.TOURNAMENT_SIZE, GeneticConfig.ELITES_PERCENTAGE);
    }

    /**
     * Esegue UN ciclo evolutivo completo (es. 1000 generazioni).
     * Contiene tutta la logica matematica e parallela che prima era in runEvolutionCore.
     * * @return Il miglior individuo trovato in questo ciclo.
     */
    public Individual executeEvolutionCycle() {
        // --- Fase 1: Inizializzazione ---
        // Generiamo la popolazione iniziale
        List<Individual> population = firstGeneration();

        // Calcolo fitness iniziale in parallelo
        population.parallelStream().forEach(ind ->
                ind.setFitness(fitnessCalculator.getFitness(ind))
        );

        // Identifichiamo il miglior individuo iniziale
        Individual bestSoFar = currentBestSolution(population, null);

        // --- Fase 2: Ciclo Generazionale ---
        for (int i = 0; i < GeneticConfig.GENERATIONS; i++) {

            // Snapshot della generazione corrente (effettivamente immutabile per il calcolo parallelo)
            final List<Individual> currentGeneration = population;

            // Lista per la nuova generazione
            List<Individual> newGeneration = new ArrayList<>(GeneticConfig.POPULATION_SIZE);

            // 1. Elitismo: preserviamo i migliori
            List<Individual> elites = selector.selectElites(currentGeneration);
            newGeneration.addAll(elites);

            // 2. Generazione Figli (Crossover + Mutazione) in Parallelo
            int childrenToGenerate = GeneticConfig.POPULATION_SIZE - elites.size();
            final int currentGenerationAge = i;

            List<Individual> children = IntStream.range(0, childrenToGenerate)
                    .parallel()
                    .mapToObj(j -> {
                        // a. Selezione Genitori
                        Individual dad = selector.tournament(currentGeneration);
                        Individual mom = selector.tournament(currentGeneration);
                        while (mom == dad) {
                            mom = selector.tournament(currentGeneration);
                        }

                        // b. Crossover
                        Individual child = mixer.uniformCrossover(mom, dad);

                        // c. Mutazione
                        gammaRays.mutate(child, currentGenerationAge);

                        // d. Calcolo Fitness immediato
                        child.setFitness(fitnessCalculator.getFitness(child));

                        return child;
                    })
                    .toList(); // Collettore Java 16+ (genera lista immutabile)

            newGeneration.addAll(children);

            // 3. Aggiornamento del record globale (Best So Far)
            bestSoFar = currentBestSolution(newGeneration, bestSoFar);

            // La nuova generazione diventa la corrente per il prossimo giro
            population = newGeneration;
        }

        // Ritorniamo una copia per sicurezza, così il controller ha un oggetto sganciato dalla logica
        return bestSoFar.copy();
    }

    /**
     * Verifica se un individuo è valido nel dominio.
     * Serve al Controller per decidere se accettare il risultato o riprovare.
     */
    public boolean isValidSolution(Individual ind) {
        return domain.isValidIndividual(ind);
    }

    // ==================================================================================
    // 🔒 METODI PRIVATI (Helper originali)
    // ==================================================================================

    private Individual buildIndividual() {
        List<Point> points = new ArrayList<>(individualSize);
        for (int i = 0; i < individualSize; i++) {
            points.add(RandomUtils.insideBoxGenerator(domain.getBoundingBox(), pointRadius));
        }
        return new Individual(points);
    }

    private List<Individual> firstGeneration() {
        List<Individual> firstGen = new ArrayList<>(GeneticConfig.POPULATION_SIZE);
        for (int i = 0; i < GeneticConfig.POPULATION_SIZE; i++) {
            firstGen.add(buildIndividual());
        }
        return firstGen;
    }

    private Individual currentBestSolution(List<Individual> individuals, Individual currentSolution) {
        Individual king = individuals.getFirst();
        for (int i = 1; i < individuals.size(); i++) {
            if (king.getFitness() < individuals.get(i).getFitness()) {
                king = individuals.get(i);
            }
        }

        if (currentSolution == null) {
            return king;
        }

        if (king.getFitness() > currentSolution.getFitness()) {
            return king;
        } else {
            return currentSolution;
        }
    }
}
