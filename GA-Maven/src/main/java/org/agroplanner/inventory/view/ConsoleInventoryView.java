package org.agroplanner.inventory.view;

import org.agroplanner.inventory.model.PlantType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * Concrete implementation of the {@link InventoryViewContract} based on the Command Line Interface (CLI).
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Responsibility:</strong> Handles the Presentation Layer. It translates abstract data requests
 * into low-level {@code System.in} / {@code System.out} operations.</li>
 * <li><strong>Robustness:</strong> Implements "Blocking Input Loops". The control flow prevents the user
 * from proceeding until syntactically valid data is provided, handling type mismatches (e.g., entering
 * text where a number is expected) by flushing the input buffer.</li>
 * <li><strong>Localization:</strong> Enforces {@link Locale#US} for floating-point operations to ensure
 * consistency in decimal separators (dot vs comma) regardless of the host OS settings.</li>
 * </ul>
 *
 * <p><strong>Static Analysis Note:</strong>
 * Suppresses {@code java:S106} (Standard Output usage) as this is inherently a Console view.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleInventoryView implements InventoryViewContract {

    /**
     * Wrapper around the standard input stream.
     */
    private final Scanner scanner;

    /**
     * Constructs the view with a specific input scanner.
     *
     * @param scanner The input source (typically {@code new Scanner(System.in)}), injected for testability.
     */
    public ConsoleInventoryView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- HELPER METHODS -------------------

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────");
    }

    // ------------------- WIZARD FLOW -------------------

    /**
     * {@inheritDoc}
     * <p>Renders the ASCII header for the configuration wizard.</p>
     */
    @Override
    public void showWizardStart() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println(" 🌱  CROP CONFIGURATION WIZARD  🌱 ");
        System.out.println("     Compose the plant mix for the optimization.");
        printDoubleSeparator();
    }

    /**
     * {@inheritDoc}
     * <p>Renders a formatted ASCII table displaying the available plant catalog.</p>
     */
    @Override
    public void showAvailablePlants(PlantType[] types) {
        System.out.println("\nAvailable Catalog:");
        System.out.println("┌─────┬──────────────┬──────┐");
        System.out.println("│ ID  │ NAME         │ ICON │");
        System.out.println("├─────┼──────────────┼──────┤");

        for (PlantType type : types) {
            System.out.printf("│ %-3d │ %-12s │  %s  │%n",
                    type.getId(), type.name(), type.getLabel());
        }
        System.out.println("└─────┴──────────────┴──────┘");
    }

    /**
     * {@inheritDoc}
     * <p><strong>Input Strategy:</strong>
     * Enters a validation loop that requires the user to input a valid integer corresponding to
     * a known {@link PlantType} ID. Non-integer inputs are discarded to prevent crashes.
     * </p>
     */
    @Override
    public PlantType askForPlantSelection(PlantType[] types) {
        while (true) {
            System.out.print("\n> Select Plant ID: ");

            if (scanner.hasNextInt()) {
                int inputId = scanner.nextInt();

                // Functional lookup
                Optional<PlantType> selection = PlantType.getById(inputId);

                // Verification: ID exists AND is in the provided allowed list
                if (selection.isPresent() && Arrays.asList(types).contains(selection.get())) {
                    PlantType p = selection.get();
                    System.out.printf("  Selected: %s %s%n", p.getLabel(), p.name());
                    return p;
                }
            } else {
                // Stream Cleaning: Flush the invalid token from the buffer
                scanner.next();
            }

            System.out.println("❌ Invalid ID. Please select a number from the table.");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int askForQuantity(PlantType selectedType) {
        while (true) {
            System.out.printf("%n> Quantity for %s: ", selectedType.name());

            if (scanner.hasNextInt()) {
                int qty = scanner.nextInt();
                if (qty > 0) return qty;
                System.out.println("⚠️ Quantity must be strictly positive (> 0).");
            } else {
                scanner.next();
                System.out.println("❌ Invalid input. Please enter an integer.");
            }
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Validation Logic:</strong>
     * Performs a "Pre-Flight Check" against {@code maxAllowedRadius}. If the user inputs a radius
     * physically larger than the domain, it is rejected immediately at the UI level to prevent
     * logical errors downstream.
     * </p>
     */
    @Override
    public double askForRadius(PlantType selectedType, double maxAllowedRadius) {
        while (true) {
            // Force US Locale to standardise decimal separator handling (avoiding "1,5" vs "1.5" ambiguity)
            System.out.printf(Locale.US, "%n> Radius (meters) for %s (Max %.2f): ", selectedType.name(), maxAllowedRadius);

            if (scanner.hasNextDouble()) {
                double r = scanner.nextDouble();

                if (r <= 0) {
                    System.out.println("⚠️ Radius must be positive.");
                } else if (r > maxAllowedRadius) {
                    System.out.printf(Locale.US, "⛔ ERROR: %.2f exceeds the domain limit (Max: %.2f)%n", r, maxAllowedRadius);
                } else {
                    return r;
                }
            } else {
                String trash = scanner.next();
                System.out.printf("❌ '%s' is not a valid number. Use format 1.5%n", trash);
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean askIfAddMore() {
        System.out.println(); // Spacing
        while (true) {
            System.out.print("> Add another species? [y/n]: ");
            String input = scanner.next().trim();

            if (input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("n")) return false;

            System.out.println("❌ Invalid input. Please type 'y' (Yes) or 'n' (No).");
            System.out.println();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showCurrentStatus(int totalItems, double maxCurrentRadius) {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 📊  CURRENT INVENTORY STATUS");
        printSingleSeparator();
        System.out.printf("   Total Plants : %d%n", totalItems);
        System.out.printf(Locale.US, "   Max Radius   : %.2f m%n", maxCurrentRadius);
        printSingleSeparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorMessage(String message) {
        System.out.println("\n⛔ CONFIGURATION ERROR:");
        System.out.println("   " + message);
        System.out.println("   Please try again.");
    }
}
