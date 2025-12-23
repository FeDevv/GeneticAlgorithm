package org.agroplanner.optimizer.views;

import java.util.Locale;
import java.util.Scanner;

/**
 * Concrete implementation of the Main Application View targeting the System Console.
 *
 * <p><strong>Architecture & UX:</strong></p>
 * <ul>
 * <li><strong>Visual Consistency:</strong> Establishes a strict visual grammar (Double lines for major states,
 * single lines for sections, Emojis for status) to reduce cognitive load.</li>
 * <li><strong>Cognitive Pacing:</strong> Implements artificial delays (e.g., during error reporting) to prevent
 * "Screen Flash" effects, ensuring the user has time to read critical feedback before the UI repaints.</li>
 * <li><strong>Localization Safety:</strong> Enforces {@link Locale#US} for all floating-point outputs to ensure
 * deterministic formatting (dots vs commas), regardless of the host OS settings.</li>
 * </ul>
 *
 * <p><strong>Static Analysis:</strong>
 * Suppresses {@code java:S106} because writing to {@code System.out}/{@code System.err} is the
 * core responsibility of this CLI adapter.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleOptimizerView implements OptimizerViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view with the shared system scanner.
     * @param scanner The input source.
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

    /**
     * {@inheritDoc}
     * <p>Displays the branding banner to establish system identity.</p>
     */
    @Override
    public void showWelcomeMessage() {
        System.out.println("\n\n");
        printDoubleSeparator();
        System.out.println("      🌿  A G R O   P L A N N E R   v 2 . 0  🌿");
        System.out.println("    Advanced Genetic Optimization System for Terrain");
        printDoubleSeparator();
        System.out.println();
    }

    /**
     * {@inheritDoc}
     * <p>Uses emojis and spacing to clearly delineate a new run context.</p>
     */
    @Override
    public void showNewSessionMessage() {
        System.out.println("\n🚀 STARTING NEW OPTIMIZATION SESSION...");
        printSingleSeparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showExitMessage() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println("   👋 SESSION TERMINATED. GOODBYE!");
        printDoubleSeparator();
    }

    /**
     * {@inheritDoc}
     * <p><strong>UX Pattern (Artificial Delay):</strong>
     * When a session aborts, we pause the thread for 1.5 seconds.
     * This prevents the error message from being instantly buried by the "New Session" header
     * of the restarting loop, ensuring the user actually sees <em>why</em> it failed.
     * </p>
     */
    @Override
    public void showSessionAborted(String reason) {
        System.out.println("\n⛔ SESSION ABORTED.");
        System.out.println("   Reason: " + reason);
        System.out.println("   System is restarting...\n");

        // UX: Small pause to let the user read the error
        try { Thread.sleep(1500); } catch (InterruptedException _) { Thread.currentThread().interrupt(); }
    }

    // ------------------- RESULTS & FEEDBACK -------------------

    /**
     * {@inheritDoc}
     * <p>Uses an ASCII box to frame the most important metric (Fitness).</p>
     */
    @Override
    public void showSolutionValue(double fitness) {
        System.out.println("\n🏆 OPTIMIZATION COMPLETE!");
        System.out.println("┌────────────────────────────────────────────────────────┐");
        // Force US Locale for consistent formatting (0.9855 instead of 0,9855)
        System.out.printf(Locale.US, "│  FINAL FITNESS SCORE:      %-27.6f │%n", fitness);
        System.out.println("└────────────────────────────────────────────────────────┘");
        System.out.println("   (Target: 1.000000 = No overlaps detected)");
    }

    /**
     * {@inheritDoc}
     * <p><strong>Input Strategy:</strong>
     * Uses a robust validation loop that rejects any input other than 'y' or 'n' (case-insensitive).
     * </p>
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void printSolutionDetails(String details) {
        printSingleSeparator();
        System.out.println("🧬 SOLUTION DETAILS:");
        System.out.println(details);
        printSingleSeparator();
    }

    /**
     * {@inheritDoc}
     */
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