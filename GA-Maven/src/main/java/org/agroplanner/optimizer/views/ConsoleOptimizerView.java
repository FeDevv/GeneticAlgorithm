package org.agroplanner.optimizer.views;

import java.util.Locale;
import java.util.Scanner;

/**
 * <p><strong>Concrete View Implementation for the Main UC Flow.</strong></p>
 *
 * <p>Implements the global interaction layer using the System Console.
 * Refactored for professional aesthetic, English localization, and strict architectural alignment.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleOptimizerView implements OptimizerViewContract {

    private final Scanner scanner;

    /**
     * Initializes the view with a shared Scanner.
     * @param scanner The system input source.
     */
    public ConsoleOptimizerView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- HELPER METHODS -------------------

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────");
    }

    // ------------------- LIFECYCLE MESSAGES -------------------

    @Override
    public void showWelcomeMessage() {
        System.out.println("\n\n");
        printDoubleSeparator();
        System.out.println("      🌿  A G R O   P L A N N E R   v 2 . 0  🌿");
        System.out.println("    Advanced Genetic Optimization System for Terrain");
        printDoubleSeparator();
        System.out.println();
    }

    @Override
    public void showNewSessionMessage() {
        System.out.println("\n🚀 STARTING NEW OPTIMIZATION SESSION...");
        printSingleSeparator();
    }

    @Override
    public void showExitMessage() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println("   👋 SESSION TERMINATED. GOODBYE!");
        printDoubleSeparator();
    }

    @Override
    public void showSessionAborted(String reason) {
        System.out.println("\n⛔ SESSION ABORTED.");
        System.out.println("   Reason: " + reason);
        System.out.println("   System is restarting...\n");

        // UX: Small pause to let the user read the error
        try { Thread.sleep(1500); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // ------------------- RESULTS & FEEDBACK -------------------

    @Override
    public void showSolutionValue(double fitness) {
        System.out.println("\n🏆 OPTIMIZATION COMPLETE!");
        System.out.println("┌────────────────────────────────────────────────────────┐");
        // Using Locale.US to ensure dot separator (e.g., 0.9855)
        System.out.printf(Locale.US, "│  FINAL FITNESS SCORE:      %-27.6f │%n", fitness);
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("   (Target: 1.000000 = No overlaps detected)");
    }

    @Override
    public boolean askIfPrintChromosome() {
        while (true) {
            System.out.print("\n👁️ View detailed chromosome data? [y/n]: ");
            String input = scanner.next().trim();

            if (input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("n")) return false;

            System.out.println("❌ Invalid input. Please type 'y' for Yes or 'n' for No.");
        }
    }

    @Override
    public void printSolutionDetails(String details) {
        printSingleSeparator();
        System.out.println("🧬 SOLUTION DETAILS:");
        System.out.println(details);
        printSingleSeparator();
    }

    @Override
    public boolean askForNewSession() {
        System.out.println(); // Spazio per respirare
        printSingleSeparator(); // Linea sottile

        while (true) {
            System.out.print("\n> Start a new optimization session? [y/n]: ");
            String input = scanner.next().trim();

            if (input.equalsIgnoreCase("y")) {
                return true;
            }
            if (input.equalsIgnoreCase("n")) {
                return false;
            }

            System.out.println("❌ Invalid input. Please type 'y' (Yes) or 'n' (No).");
        }
    }
}