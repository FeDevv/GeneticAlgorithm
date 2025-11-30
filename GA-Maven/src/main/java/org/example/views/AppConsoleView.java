package org.example.views;

import java.util.Scanner;

/**
 * View principale dell'applicazione.
 * Gestisce l'interazione utente di alto livello (benvenuto, configurazione parametri globali).
 */
@SuppressWarnings("java:S106")
public class AppConsoleView {

    private final Scanner scanner;
    private final static String SEPARATOR = "=========================================";

    public AppConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }

    public void showWelcomeMessage() {
        System.out.println(SEPARATOR);
        System.out.println("   \t\t🧬 TERRAIN OPTIMIZER 🧬   ");
        System.out.println(SEPARATOR);
    }

    public void showNewSessionMessage() {
        System.out.println("\n-----------------------------------------");
        System.out.println("   \t\t\t🔄 NEW SESSION");
        System.out.println("-----------------------------------------");
    }

    public void exitOnDemand() {
        System.out.println("\n🚫 Domain creation canceled. Exiting session.");
        System.out.println("\n" + SEPARATOR);
        System.out.println("   \t👋 Session Terminated. See you!   ");
        System.out.println(SEPARATOR);
    }

    /**
     * Richiede la dimensione dell'individuo (numero di punti).
     * @return un intero positivo strettamente maggiore di 0.
     */
    public int readIndividualSize() {
        int size = 0;
        while (size <= 0) {
            System.out.print("\nEnter genome length (number of points, > 0): ");

            if (scanner.hasNextInt()) {
                size = scanner.nextInt();
                if (size <= 0) {
                    System.out.print("\n❌ Error: The genome length must be a positive integer (> 0). Retry.");
                }
            } else {
                System.out.print("\n❌ Invalid Input. Please enter a positive integer. Retry.");
                scanner.next(); // Pulisce il buffer
            }
        }
        return size;
    }

    /**
     * Richiede il raggio dei punti, validandolo rispetto al limite imposto dal dominio.
     * @param maxLimit Il raggio massimo consentito (calcolato dal controller in base al dominio).
     * @return un double valido (0 < r <= maxLimit).
     */
    public double readPointRadius(double maxLimit) {
        double radius = 0.0;

        System.out.println("\n--- Configuration: Point Radius ---");
        System.out.printf("ℹ️  Based on the domain, max valid radius is: %.2f%n", maxLimit);

        while (radius <= 0 || radius > maxLimit) {
            System.out.printf("Enter the RADIUS (r) of the points (> 0 and <= %.2f): ", maxLimit);

            if (scanner.hasNextDouble()) {
                radius = scanner.nextDouble();

                if (radius <= 0) {
                    System.out.println("\n❌ Error: The radius has to be strictly greater than 0.");
                } else if (radius > maxLimit) {
                    System.out.printf("%n❌ Error: The radius (%.2f) cannot exceed the maximum limit (%.2f).%n", radius, maxLimit);
                }
            } else {
                System.out.println("\n❌ Invalid Input. You must enter a number.");
                scanner.next(); // Pulisce il buffer
            }
        }
        System.out.printf("%n✅ Valid radius entered: %.2f%n", radius);
        return radius;
    }

    /**
     * Metodo di utility per domande Si/No.
     * @param question La domanda da porre all'utente.
     * @return true se 'y' o 'yes', false se 'n' o 'no'.
     */
    public boolean askYesNo(String question) {
        String input = "";
        while (true) {
            System.out.print(question + " [y/n]: ");
            if (scanner.hasNext()) {
                input = scanner.next().trim();
            }

            if (input.equalsIgnoreCase("y")) {
                return true;
            } else if (input.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out.println("\n❌ Invalid input. Please enter 'y' or 'n'.");
            }
        }
    }

    /**
     * Stampa i dettagli dell'individuo (il toString).
     * @param details La stringa restituita da individual.toString()
     */
    public void printSolutionDetails(String details) {
        System.out.println("\n------- 🧬 Solution  --------");
        System.out.println(details);
        System.out.println("-------------------------------");
    }

    public void showEvolutionStart() {
        System.out.println("\n🚀 Starting Evolution Engine...");
    }

    public void showSolutionValue(double fitness) {
        System.out.printf("Best solution's Fitness: %.6f%n%n", fitness);
    }

}
