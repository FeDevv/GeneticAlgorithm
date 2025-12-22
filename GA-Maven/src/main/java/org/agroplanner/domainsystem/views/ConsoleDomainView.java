package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.*;


/**
 * Concrete implementation of the {@link DomainViewContract} targeting the Command Line Interface (CLI).
 *
 * <p><strong>Architecture & Design:</strong></p>
 * <ul>
 * <li><strong>Responsibility:</strong> Handles the Presentation Layer for the domain subsystem. It translates
 * abstract requests (e.g., "Ask for parameters") into low-level {@code System.in} / {@code System.out} operations.</li>
 * <li><strong>Dynamic Form Generation:</strong> Implements a <strong>Metadata-Driven UI</strong> approach.
 * Instead of hard-coding prompts for every shape (e.g., {@code askForRadius}), this class iterates over the
 * parameter schema provided by {@link DomainType#getRequiredParameters()} to generate input forms dynamically at runtime.</li>
 * <li><strong>Robustness:</strong> Utilizes "Blocking Input Loops" to enforce data integrity at the entry point.
 * The control flow prevents the user from proceeding until syntactically valid data is provided, ensuring
 * that the Controller layer receives only clean data.</li>
 * </ul>
 */
@SuppressWarnings("java:S106")
public class ConsoleDomainView implements DomainViewContract {

    /**
     * Shared scanner instance for reading standard input.
     */
    private final Scanner scanner;

    /**
     * Constructs the view using a shared input source.
     *
     * @param scanner The {@link Scanner} instance injected via Dependency Injection.
     * Sharing a single scanner across views prevents resource leaks and stream closure issues.
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

    /**
     * {@inheritDoc}
     * <p>Renders a formatted ASCII table listing the available geometric shapes.</p>
     */
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

    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Input Strategy:</strong>
     * Enters a validation loop expecting an integer ID.
     * Handles the special case {@code 0} as an explicit "Exit" command.
     * </p>
     */
    @Override
    public Optional<DomainType> askForDomainType(List<DomainType> types) {
        // Validation Loop
        while (true) {
            System.out.print("\n> Select ID: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();

                // CASE A: Explicit Exit Signal
                if (choice == 0) return Optional.empty();

                // CASE B: Valid Selection Lookup
                Optional<DomainType> selection = DomainType.fromMenuId(choice);

                // Verification: Exists in Enum AND is in the allowed list provided
                if (selection.isPresent() && types.contains(selection.get())) {
                    System.out.println("   Selected: " + selection.get().getDisplayName());
                    return selection;
                }
            } else {
                // Stream Cleaning: Flush invalid token (e.g., user typed "abc")
                scanner.next();
            }

            // CASE C: Invalid Input Feedback
            System.out.println("❌ Invalid selection. Please enter a valid ID from the table.");
        }
    }

    /**
     * {@inheritDoc}
     *
     * <p><strong>Dynamic Execution:</strong></p>
     * Iterates through the {@code requiredParameters} list defined in the {@link DomainType} metadata.
     * For each parameter key, it invokes a helper method to robustly collect a positive double value.
     * This allows the View to support any new DomainType added to the Model without requiring code changes here (OCP).
     */
    @Override
    public Map<String, Double> askForParameters(DomainType type) {
        Map<String, Double> params = new HashMap<>();

        System.out.println();
        printSingleSeparator();
        System.out.println(" ⚙️  PARAMETERS FOR: " + type.getDisplayName().toUpperCase());
        printSingleSeparator();

        // Dynamic Form Generation Loop
        for (String key : type.getRequiredParameters()) {
            // Helper method handles the input sanitization for each specific field
            double value = readPositiveDouble(key);
            params.put(key, value);
        }
        return params;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSuccessMessage() {
        System.out.println("\n✅ DOMAIN SUCCESSFULLY CONFIGURED.");
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

    // ------------------- INPUT HELPERS -------------------

    /**
     * Helper method to capture a strictly positive double value from the console.
     *
     * <p><strong>Input Sanitization:</strong></p>
     * Implements a blocking loop that rejects non-numeric inputs and values {@code <= 0}.
     * This ensures that the generated map in {@link #askForParameters(DomainType)} contains
     * only syntactically valid data.
     *
     * @param paramName The name of the parameter to prompt for (used in the UI label).
     * @return A validated, positive double.
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
                // Flush garbage input
                String input = scanner.next();
                System.out.printf("❌ '%s' is not a valid number. Retry.%n", input);
            }
        }
    }
}