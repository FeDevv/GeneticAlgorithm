package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.*;


/**
 * <p><strong>Concrete View Implementation for CLI (Command Line Interface).</strong></p>
 *
 * <p>Handles user interaction for Domain definition.
 * Refactored for visual consistency, ASCII tables, and robust English input handling.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleDomainView implements DomainViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view sharing an existing Scanner instance.
     * @param scanner The shared scanner instance.
     */
    public ConsoleDomainView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- HELPER METHODS -------------------

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────");
    }

    // ------------------- INTERACTION LOGIC -------------------

    @Override
    public void showAvailableDomains(List<DomainType> types){
        printDoubleSeparator();
        System.out.println(" 📐  DOMAIN DEFINITION ");
        System.out.println("     Define the geometry of the working area.");
        printDoubleSeparator();

        // Render Menu Table
        System.out.println("\nAvailable Shapes:");
        System.out.println("┌─────┬──────────────────────┐");
        System.out.println("│ ID  │ SHAPE                │");
        System.out.println("├─────┼──────────────────────┤");

        for (DomainType type : types) {
            // Assumes getMenuId returns int and getDisplayName returns String
            System.out.printf("│ %-3d │ %-20s │%n", type.getMenuId(), type.getDisplayName().toUpperCase());
        }
        System.out.println("└─────┴──────────────────────┘");
        System.out.println("      (Enter 0 to Return/Exit)");

    };


    @Override
    public Optional<DomainType> askForDomainType(List<DomainType> types) {
        // Validation Loop
        while (true) {
            System.out.print("\n> Select ID: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();

                // CASE A: Exit
                if (choice == 0) return Optional.empty();

                // CASE B: Valid Selection
                Optional<DomainType> selection = DomainType.fromMenuId(choice);
                if (selection.isPresent() && types.contains(selection.get())) {
                    System.out.println("   Selected: " + selection.get().getDisplayName());
                    return selection;
                }
            } else {
                scanner.next(); // Flush invalid token
            }

            // CASE C: Invalid Input
            System.out.println("❌ Invalid selection. Please enter a valid ID from the table.");
        }
    }

    @Override
    public Map<String, Double> askForParameters(DomainType type) {
        Map<String, Double> params = new HashMap<>();

        System.out.println();
        printSingleSeparator();
        System.out.println(" ⚙️  PARAMETERS FOR: " + type.getDisplayName().toUpperCase());
        printSingleSeparator();

        // Dynamic Form Generation
        for (String key : type.getRequiredParameters()) {
            // Helper method handles the specific input loop for each parameter
            double value = readPositiveDouble(key);
            params.put(key, value);
        }
        return params;
    }

    @Override
    public void showSuccessMessage() {
        System.out.println("\n✅ DOMAIN SUCCESSFULLY CONFIGURED.");
    }

    @Override
    public void showErrorMessage(String message) {
        System.out.println("\n⛔ CONFIGURATION ERROR:");
        System.out.println("   " + message);
        System.out.println("   Please try again.");
    }

    // ------------------- INPUT HELPERS -------------------

    /**
     * Reads a double from the console for a specific parameter.
     * Enforces strictly positive values (> 0).
     */
    private double readPositiveDouble(String paramName) {
        // Format label nicely (e.g., "width" -> "WIDTH")
        String label = paramName.toUpperCase();

        while (true) {
            System.out.printf("%n> Enter %s (meters): ", label);

            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();

                if (value > 0) {
                    return value;
                } else {
                    System.out.println("⚠️  Value must be strictly positive (> 0).");
                }
            } else {
                String input = scanner.next();
                System.out.printf("❌ '%s' is not a valid number. Retry.%n", input);
            }
        }
    }
}