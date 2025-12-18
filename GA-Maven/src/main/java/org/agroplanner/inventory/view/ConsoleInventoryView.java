package org.agroplanner.inventory.view;

import org.agroplanner.inventory.model.PlantType;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * <p><strong>Concrete View for Inventory Configuration.</strong></p>
 *
 * <p>Handles the user interaction for selecting plant types, quantities, and specific dimensions.
 * Refactored for English localization and consistent CLI styling.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleInventoryView implements InventoryViewContract {

    private final Scanner scanner;

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

    @Override
    public void showWizardStart() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println(" 🌱  CROP CONFIGURATION WIZARD  🌱 ");
        System.out.println("     Compose the plant mix for the optimization.");
        printDoubleSeparator();
    }

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

    @Override
    public PlantType askForPlantSelection(PlantType[] types) {
        while (true) {
            System.out.print("\n> Select Plant ID: ");

            if (scanner.hasNextInt()) {
                int inputId = scanner.nextInt();

                Optional<PlantType> selection = PlantType.getById(inputId);

                if (selection.isPresent() && Arrays.asList(types).contains(selection.get())) {
                    PlantType p = selection.get();
                    System.out.printf("  Selected: %s %s%n", p.getLabel(), p.name());
                    return p;
                }
            } else {
                scanner.next(); // Flush garbage
            }

            // Messaggio di errore standardizzato
            System.out.println("❌ Invalid ID. Please select a number from the table.");
        }
    }

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

    @Override
    public double askForRadius(PlantType selectedType, double maxAllowedRadius) {
        while (true) {
            // Locale.US ensures dot separator (1.5 instead of 1,5)
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

    @Override
    public void showErrorMessage(String message) {
        System.out.println("\n⛔ CONFIGURATION ERROR:");
        System.out.println("   " + message);
        System.out.println("   Please try again.");
    }
}
