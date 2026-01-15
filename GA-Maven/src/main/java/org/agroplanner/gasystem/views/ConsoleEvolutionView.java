package org.agroplanner.gasystem.views;


import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.Point;

import java.util.List;
import java.util.Locale;
import java.util.Scanner;

import static org.apache.commons.lang3.StringUtils.truncate;

/**
 * Concrete implementation of the Evolutionary View targeting the Command Line Interface (CLI).
 *
 * <p><strong>Architecture & UX:</strong></p>
 * <ul>
 * <li><strong>Responsibility:</strong> Translates the internal state of the {@link org.agroplanner.gasystem.controllers.EvolutionConsoleController}
 * into human-readable text feedback.</li>
 * <li><strong>Visual Grammar:</strong> Utilizes distinct visual cues (ASCII separators, Emojis) to create a
 * "Scannable" log output. This allows the user to instantly recognize Success, Failure, or Processing states
 * without reading every word.</li>
 * <li><strong>Localization Safety:</strong> Enforces {@link Locale#US} for numerical formatting. This ensures
 * that floating-point metrics (e.g., execution time) are displayed consistently (using dots for decimals),
 * preventing confusion or parsing errors on systems with different regional settings.</li>
 * </ul>
 *
 * <p><strong>Static Analysis:</strong>
 * Suppresses {@code java:S106} because writing to Standard Output is the intended behavior for this CLI component.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleEvolutionView implements EvolutionViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view with the shared input scanner.
     * @param scanner The input source (System.in).
     */
    public ConsoleEvolutionView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- HELPER METHODS -------------------

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    // ------------------- STATUS UPDATES -------------------

    @Override
    public void showEvolutionStart() {
        System.out.println("\n");
        // ASCII ART: EVOLUTION ENGINE
        System.out.println("███████╗██╗   ██╗ ██████╗ ██╗     ██╗   ██╗████████╗██╗ ██████╗ ███╗   ██╗");
        System.out.println("██╔════╝██║   ██║██╔═══██╗██║     ██║   ██║╚══██╔══╝██║██╔═══██╗████╗  ██║");
        System.out.println("█████╗  ██║   ██║██║   ██║██║     ██║   ██║   ██║   ██║██║   ██║██╔██╗ ██║");
        System.out.println("██╔══╝  ╚██╗ ██╔╝██║   ██║██║     ██║   ██║   ██║   ██║██║   ██║██║╚██╗██║");
        System.out.println("███████╗ ╚████╔╝ ╚██████╔╝███████╗╚██████╔╝   ██║   ██║╚██████╔╝██║ ╚████║");
        System.out.println("╚══════╝  ╚═══╝   ╚═════╝ ╚══════╝ ╚═════╝    ╚═╝   ╚═╝ ╚═════╝ ╚═╝  ╚═══╝");
        System.out.println("                          Genetic Algorithm Engine                            ");
        printSingleSeparator();
        System.out.println(" 🧬  PROCESSING GENERATIONS... PLEASE WAIT.");
        printSingleSeparator();
    }

    @Override
    public void showRetryWarning(int currentAttempt, int maxAttempts, double lastExecutionTimeSec) {
        System.out.println("\n ⚠️  CONVERGENCE ISSUE DETECTED");
        System.out.printf(Locale.US, "    Attempt %d of %d failed to find a valid solution (Time: %.2fs).%n",
                currentAttempt, maxAttempts, lastExecutionTimeSec);

        if (currentAttempt < maxAttempts) {
            System.out.println("    🔄 Restarting simulation with new random seed...");
        } else {
            System.out.println("    🛑 Max attempts reached. Returning best effort result.");
        }
    }

    @Override
    public void showSuccess(int attempt, double executionTimeSec) {
        System.out.println("\n\n🏆 OPTIMIZATION COMPLETE!");
        System.out.println("\n ✅ VALID SOLUTION FOUND!");
        // Output tabellare pulito
        System.out.printf("    • Attempts Needed : %d%n", attempt);
        System.out.printf(Locale.US, "    • Total CPU Time  : %.2f seconds%n", executionTimeSec);
        printSingleSeparator();
    }

    @Override
    public boolean askIfPrintDetails() {
        System.out.println();
        System.out.println("⚠️  NOTE: Detailed output contains a row for every single plant.");

        while (true) {
            System.out.print("👁️  View detailed chromosome data? [y/n]: ");
            String input = scanner.next().trim();
            if (input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("n")) return false;
            System.out.println("  ❌ Invalid input.");
        }
    }

    /**
     * Now responsible for BOTH formatting AND printing the detailed table.
     */
    @Override
    public void printDetailedReport(Individual individual) {
        System.out.println("\n🧬 CHROMOSOME DETAILS (PHENOTYPE):");
        printSingleSeparator();

        // --- LOGICA SPOSTATA DAL FORMATTER ---
        StringBuilder sb = new StringBuilder();
        List<Point> genes = individual.getChromosomes();

        // Header Tabella
        // Larghezza Totale: 1+5+1+22+1+11+1+27+1 = 70 chars (approx)
        sb.append(String.format("┌─────┬──────────────────────┬───────────┬───────────────────────────┐%n"));
        sb.append(String.format("│ ID  │ VARIETY NAME         │ TYPE      │ COORDINATES (X, Y)        │%n"));
        sb.append(String.format("├─────┼──────────────────────┼───────────┼───────────────────────────┤%n"));

        for (int i = 0; i < genes.size(); i++) {
            Point p = genes.get(i);

            String name = p.getVarietyName();
            String type = p.getType().name();

            sb.append(String.format(Locale.US, "│ %-3d │ %-20s │ %-9s │ x=%-6.2f y=%-6.2f         │%n",
                    (i + 1),
                    truncate(name, 20),
                    truncate(type, 9),
                    p.getX(),
                    p.getY()
            ));
        }
        sb.append(String.format("└─────┴──────────────────────┴───────────┴───────────────────────────┘%n"));
        // -------------------------------------

        System.out.print(sb);
        printSingleSeparator();
    }

    @Override
    public void showSolutionValue(double fitness, int totalPlants) {
        System.out.println("┌────────────────────────────────────────────────────────────┐");
        System.out.printf(Locale.US, "│  FINAL FITNESS SCORE:          %-27.6f │%n", fitness);
        System.out.printf(Locale.US, "│  TOTAL PLANTS PLACED:          %-27d │%n", totalPlants);
        System.out.println("└────────────────────────────────────────────────────────────┘");
    }
}

