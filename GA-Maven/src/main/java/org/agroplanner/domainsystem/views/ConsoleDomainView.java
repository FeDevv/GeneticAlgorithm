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

    private final Scanner scanner;

    public ConsoleDomainView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- RENDERERS -------------------

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    @Override
    public void showAvailableDomains(List<DomainType> types){
        System.out.println("\n");
        // ASCII ART: TERRAIN
        System.out.println("████████╗███████╗██████╗ ██████╗  █████╗ ██╗███╗   ██╗    ███████╗███████╗████████╗██╗   ██╗██████╗ ");
        System.out.println("╚══██╔══╝██╔════╝██╔══██╗██╔══██╗██╔══██╗██║████╗  ██║    ██╔════╝██╔════╝╚══██╔══╝██║   ██║██╔══██╗");
        System.out.println("   ██║   █████╗  ██████╔╝██████╔╝███████║██║██╔██╗ ██║    ███████╗█████╗     ██║   ██║   ██║██████╔╝");
        System.out.println("   ██║   ██╔══╝  ██╔══██╗██╔══██╗██╔══██║██║██║╚██╗██║    ╚════██║██╔══╝     ██║   ██║   ██║██╔═══╝ ");
        System.out.println("   ██║   ███████╗██║  ██║██║  ██║██║  ██║██║██║ ╚████║    ███████║███████╗   ██║   ╚██████╔╝██║     ");
        System.out.println("   ╚═╝   ╚══════╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝  ╚═╝╚═╝╚═╝  ╚═══╝    ╚══════╝╚══════╝   ╚═╝    ╚═════╝ ╚═╝     ");
        System.out.println("                                Geometric Modeling Engine                                    ");
        printDoubleSeparator();
        System.out.println(" Define the geometry of the working area (Field Boundary).");
        printDoubleSeparator();

        // Render Menu Table (Box Drawing)
        System.out.println("\nSELECT GEOMETRY SHAPE:");
        System.out.println("┌─────┬──────────────────────────┬──────────────────────────────────────┐");
        System.out.println("│ ID  │ SHAPE TYPE               │ PARAMETERS REQUIRED                  │");
        System.out.println("├─────┼──────────────────────────┼──────────────────────────────────────┤");

        for (DomainType type : types) {
            String paramsList = String.join(", ", type.getRequiredParameters());
            if (paramsList.isEmpty()) paramsList = "None";

            paramsList = paramsList.substring(0, 1).toUpperCase() + paramsList.substring(1);

            System.out.printf("│ %-3d │ %-24s │ %-36s │%n",
                    type.getMenuId(),
                    type.getDisplayName().toUpperCase(),
                    paramsList
            );
        }
        System.out.println("└─────┴──────────────────────────┴──────────────────────────────────────┘");
        System.out.println("      (Enter 0 to Return/Exit)");
    }

    @Override
    public Optional<DomainType> askForDomainType(List<DomainType> types) {
        while (true) {
            System.out.print("\n> Select ID: ");

            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();

                scanner.nextLine();

                if (choice == 0) return Optional.empty();

                Optional<DomainType> selection = DomainType.fromMenuId(choice);

                if (selection.isPresent() && types.contains(selection.get())) {
                    System.out.println("  ✅ Selected: " + selection.get().getDisplayName());
                    return selection;
                }
            } else {
                scanner.next();
            }

            System.out.println("  ❌ Invalid selection. Please enter a valid ID from the table.");
        }
    }

    @Override
    public Map<String, Double> askForParameters(DomainType type) {
        Map<String, Double> params = new HashMap<>();

        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" ⚙️  CONFIGURE PARAMETERS: " + type.getDisplayName().toUpperCase());
        printSingleSeparator();

        for (String key : type.getRequiredParameters()) {
            double value = readPositiveDouble(key);
            params.put(key, value);
        }
        return params;
    }

    @Override
    public void showSuccessMessage() {
        System.out.println("\n✅ DOMAIN CREATED SUCCESSFULLY.");
        printSingleSeparator();
        try { Thread.sleep(500); } catch (Exception ignored) {}
    }

    @Override
    public void showErrorMessage(String message) {
        System.out.println("\n⛔ CONFIGURATION ERROR:");
        System.out.println("   " + message);
        System.out.println("   Please check your inputs and try again.");
    }

    // ------------------- INPUT HELPERS -------------------

    private double readPositiveDouble(String paramName) {
        // formattingh
        String label = paramName.substring(0, 1).toUpperCase() + paramName.substring(1).toLowerCase();

        while (true) {
            System.out.printf(Locale.US, "> %-20s: ", label + " (m)");

            if (scanner.hasNextDouble()) {
                double value = scanner.nextDouble();
                scanner.nextLine();

                if (value > 0) {
                    return value;
                } else {
                    System.out.println("  ⚠️  Value must be strictly positive (> 0).\n");
                }
            } else {
                String trash = scanner.next();
                System.out.printf("  ❌ '%s' is not a valid number.%n%n", trash);
            }
        }
    }
}