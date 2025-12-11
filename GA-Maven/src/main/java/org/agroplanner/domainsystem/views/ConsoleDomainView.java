package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.*;

/**
 * <p><strong>Concrete View Implementation for CLI (Command Line Interface).</strong></p>
 *
 * <p>This class implements the {@link DomainViewContract} using standard system I/O.
 * It is responsible for:</p>
 * <ul>
 * <li>Rendering menus and messages to {@code System.out}.</li>
 * <li>Capturing and cleaning user input via {@link Scanner}.</li>
 * <li>Performing <strong>Syntactic Validation</strong> (ensuring inputs are numbers).</li>
 * <li>Performing <strong>Basic Validation</strong> (ensuring inputs are non-negative).</li>
 * </ul>
 */
public class ConsoleDomainView implements DomainViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view sharing an existing Scanner instance.
     * <p>
     * Dependency Injection of the Scanner is crucial to avoid closing the underlying
     * {@code System.in} stream when switching between different views/controllers.
     * </p>
     *
     * @param scanner The shared scanner instance.
     */
    public ConsoleDomainView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Optional<DomainType> askForDomainType(List<DomainType> types) {

        // --- VIEW-LEVEL VALIDATION LOOP ---
        // The View keeps the user here until a syntactically valid choice is made.
        while (true) {
            // Render Dynamic Menu
            System.out.println("\n--- Select domain type ---");
            for (DomainType type : types) {
                System.out.println(type.getMenuId() + ") " + type.getDisplayName());
            }
            System.out.println("0) Quit");

            // Capture Input (Guaranteed to be an integer >= 0)
            int choice = readMenuChoice();

            // CASE A: Explicit Exit
            if (choice == 0) {
                return Optional.empty();
            }

            // CASE B: Map Input to DomainType
            Optional<DomainType> result = DomainType.fromMenuId(choice);

            if (result.isPresent()) {
                return result; // Valid ID found -> Return control to Controller
            }

            // CASE C: Unmapped ID (e.g., user entered 99)
            // Feedback is given, and the loop restarts.
            System.out.println("\n❌ Invalid selection ID. Please try again.");
        }
    }

    @Override
    public Map<String, Double> askForParameters(DomainType type) {
        Map<String, Double> params = new HashMap<>();
        System.out.println("\n--- Parameter Entry for " + type.getDisplayName() + " ---");

        // Dynamic Form Generation:
        // Iterate over the requirements defined in the Enum and ask for each one.
        for (String key : type.getRequiredParameters()) {
            double value = readPositiveDouble(key);
            params.put(key, value);
        }
        return params;
    }

    @Override
    public void showSuccessMessage() {
        System.out.println("\n✅ Domain successfully created!");
    }

    @Override
    public void showErrorMessage(String message) {
        System.err.println("\n❌ Creation error: " + message);
    }

    /**
     * Reads an integer from the console with robust error handling.
     * Ensures the result is non-negative.
     */
    private int readMenuChoice() {
        int choice;
        do {
            System.out.print("Enter your choice: ");
            while (!scanner.hasNextInt()) {
                System.out.println("\n❌ Non-numeric value. Please enter an integer.");
                scanner.next();
                System.out.print("Enter your choice: ");
            }
            choice = scanner.nextInt();
            if (choice < 0) System.out.println("\n❌ Invalid choice. Please enter a non negative integer (>= 0).");
        } while (choice < 0);
        return choice;
    }

    /**
     * Reads a double from the console for a specific parameter.
     * Enforces strictly positive values (> 0) as physical dimensions cannot be zero or negative.
     *
     * @param paramName The name of the parameter being requested (for prompt context).
     */
    private double readPositiveDouble(String paramName) {
        double value;
        while (true) {
            System.out.print("Enter value for '" + paramName + "' (> 0): ");
            if (scanner.hasNextDouble()) {
                value = scanner.nextDouble();
                if (value > 0) return value;
                System.out.println("\n❌ Invalid value. The parameter '" + paramName + "' must be strictly positive (> 0). Retry.");
            } else {
                System.out.println("\n❌ Non-numeric value. Retry.");
                scanner.next();
            }
        }
    }
}
