package org.example;


import org.example.controllers.DomainController;
import org.example.controllers.EvolutionEngine;
import org.example.model.Individual;
import org.example.model.domains.Domain;
import org.example.view.DomainConsoleView;
import org.example.view.EvolutionConsoleView;

import java.awt.geom.Rectangle2D;
import java.util.Optional;
import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        int individualSize = 0;
        double pointRadius = 0.0;

        // inizializza il dominio
        DomainConsoleView domainConsoleView = new DomainConsoleView(scanner);

        // lascia scegliere il dominio all'utente
        DomainController controller = new DomainController(domainConsoleView);
        Domain problemDomain;

        // controlliamo il tipo di dominio ritornato
        Optional<Domain> domainOptional = controller.createDomain();
        if (domainOptional.isPresent()) {
            problemDomain = domainOptional.get();
        } else {
            System.out.println("\nDomain creation canceled or failed.");
            return;
        }


        // prendi la dimensione dell'individuo (quante piante da piantare?)
        // Il tuo codice aggiornato
        while (individualSize <= 0) {
            System.out.print("Define the genome length (number of Points, positive integer): ");

            // 1. Controlla se l'input è un intero
            if (scanner.hasNextInt()) {
                individualSize = scanner.nextInt();

                // **AGGIUNTA:** Controlla se il valore è positivo
                if (individualSize <= 0) {
                    System.out.println("❌ Error: The genome length must be a positive integer (> 0). Retry.");
                    // Il loop esterno 'while (individualSize <= 0)' si ripete
                }

            } else {
                System.out.println("❌ Invalid Input. Please enter a positive integer. Retry.");
                scanner.next(); // Scarta l'input non valido (non numerico)
            }
        }

        //Tutta questa parte poi andrà nel controller finale. Per ora la metto qua.
        Rectangle2D boundingBox = problemDomain.getBoundingBox();
        double boxWidth = boundingBox.getWidth();
        double boxHeight = boundingBox.getHeight();
        double maxRadiusLimit = Math.min(boxWidth, boxHeight) / 2.0;

        while (pointRadius <= 0 || pointRadius > maxRadiusLimit) {
            System.out.printf("Enter the RADIUS (r) of the points (> 0 e <= %.2f): ", maxRadiusLimit);

            if (scanner.hasNextDouble()) {
                pointRadius = scanner.nextDouble();

                if (pointRadius <= 0) {
                    System.out.println("❌ Error: The radius has to be strictly greater than 0.");
                } else if (pointRadius > maxRadiusLimit) {
                    System.out.printf("❌ Error: The radius (%.2f) cannot exceed the maximum limit (%.2f).\n", pointRadius, maxRadiusLimit);
                }

            } else {
                System.out.println("❌ Invalid Input. Retry. You must enter a number.");
                scanner.next(); // Scarta l'input non valido
            }
        }
        System.out.printf("\n✅ Valid radius entered: %.2f\n", pointRadius);

        scanner.close(); // Chiude lo scanner dopo aver finito di leggere l'input

        // l'evolution engine riceve delle coppie: [(numero punti, raggio punti), (numero punti, raggio punti)]
        // il totale dei punti sarà la dimensione dell'individuo, nei controlli, ogni punto avrà il suo raggio.

        // Creazione e Avvio del Motore
        EvolutionEngine engine = new EvolutionEngine(
                new EvolutionConsoleView(),
                problemDomain,
                individualSize, // Valore inserito dall'utente
                pointRadius // Valore inserito dall'utente
        );

        Individual bestSolution = engine.runEvolutionEngine();

        // Output dei Risultati

        System.out.printf("Best solution's Fitness: %.6f\n", bestSolution.getFitness());
        System.out.println(bestSolution);
    }
}

// RENDERE LA DOMAINFACTORY UNA SINGLETON
// aggiungere quindi l'attributo di tipo "singletonClass" e un check (if) per ritornare sempre lo stessa
// istanza.
/**
 * singletonClass singleton = null
 * ...
 * if (singleton == null) {
 *     return new singletonClass;
 * } else {
 *     return singleton;
 * }
 * */

// stampare il risultato in un file di testo, andando a chiamare il file : "Individual_17-11-2025_11:56:08"

// portare il progetto in MAVEN

// aggiungere la funzione per portare il progetto in un foglio di calcolo (CSV) con la libreria esterna : Apache Common CSV

// ricontrollare bene i commenti!!!! IMPORTANTE