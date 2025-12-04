package org.agroplanner.overseer.views;

import java.util.Scanner;

@SuppressWarnings("java:S106")
public class ConsoleAppView implements AppViewContract {
    private final Scanner scanner;
    private static final String SEPARATOR = "=========================================";

    public ConsoleAppView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void showWelcomeMessage() {
        System.out.println(SEPARATOR);
        System.out.println("   \t\t🧬 TERRAIN OPTIMIZER 🧬   ");
        System.out.println(SEPARATOR);
    }

    @Override
    public void showNewSessionMessage() {
        System.out.println("\n-----------------------------------------");
        System.out.println("   \t\t\t🔄 NEW SESSION");
        System.out.println("-----------------------------------------");
    }

    @Override
    public void showExitMessage() {
        System.out.println("\n" + SEPARATOR);
        System.out.println("   \t👋 Session Terminated. See you!   ");
        System.out.println(SEPARATOR);
    }

    @Override
    public int askForIndividualSize() {
        int size = 0;
        while (size <= 0) {
            System.out.print("\nEnter genome length (number of points, > 0): ");
            if (scanner.hasNextInt()) {
                size = scanner.nextInt();
                if (size <= 0) System.out.print("\n❌ Error: The genome length must be a positive integer (> 0). Retry.");
            } else {
                System.out.print("\n❌ Invalid Input. Please enter a positive integer. Retry.");
                scanner.next();
            }
        }
        return size;
    }

    @Override
    public double askForPointRadius(double maxLimit) {
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

    @Override
    public void showSolutionValue(double fitness) {
        System.out.printf("Best solution's Fitness: %.6f%n%n", fitness);
    }

    @Override
    public boolean askIfPrintChromosome() {
        String input = "";
        while (true) {
            System.out.print("Do you wish to print chromosomes? [y/n]: ");
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

    @Override
    public void showSessionAborted(String reason) {
        System.out.println("\n" + "=".repeat(50));
        System.out.println("🛑 SESSION ABORTED");
        System.out.println("=".repeat(50));
        System.out.println("Reason: " + reason);
        System.out.println("\n🔄 Restarting session from domain selection...");
        System.out.println("-".repeat(50));

        try { Thread.sleep(2000); } catch (InterruptedException ignored) {}
    }

    @Override
    public void printSolutionDetails(String details) {
        System.out.println("\n------- 🧬 Solution Details --------");
        System.out.println(details);
        System.out.println("------------------------------------");
    }
}
