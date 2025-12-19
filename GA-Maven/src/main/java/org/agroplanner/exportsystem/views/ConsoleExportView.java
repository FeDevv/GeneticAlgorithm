package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * <p><strong>Concrete View Implementation for Data Export.</strong></p>
 *
 * <p>Handles the user interaction for saving results to disk.
 * Features strict filename validation and a clear menu for format selection.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleExportView implements ExportViewContract {

    private final Scanner scanner;

    /**
     * Initializes the view with a shared Scanner.
     * @param scanner The system input source.
     */
    public ConsoleExportView(Scanner scanner) {
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
    public void showAvailableExports(List<ExportType> availableTypes) {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println(" 💾  EXPORT WIZARD ");
        System.out.println("     Save your solution to a file.");
        printDoubleSeparator();

        System.out.println("\nAvailable Formats:");
        System.out.println("┌─────┬──────────────┬──────────────────────────┐");
        System.out.println("│ ID  │ FORMAT       │ DESCRIPTION              │");
        System.out.println("├─────┼──────────────┼──────────────────────────┤");

        for (int i = 0; i < availableTypes.size(); i++) {
            ExportType type = availableTypes.get(i);
            // We map the description here for display purposes
            String desc = availableTypes.get(i).getExportInfo();

            // Adjust index + 1 if you want menu to start at 1, or keep i for 0-based.
            System.out.printf("│ %-3d │ %-12s │ %-24s │%n", availableTypes.get(i).getMenuId(), type.name(), desc);
        }
        System.out.println("└─────┴──────────────┴──────────────────────────┘");
        System.out.println("      (Enter 0 to Cancel)");
    }

    @Override
    public Optional<ExportType> askForExportType(List<ExportType> availableTypes) {
        // Validation Loop
        while (true) {
            // Stile standardizzato con \n
            System.out.print("\n> Select ID: ");

            if (scanner.hasNextInt()) {
                int inputId = scanner.nextInt();
                scanner.nextLine();

                // CASE A: Cancel
                if (inputId == 0) return Optional.empty();

                // CASE B: Lookup by ID (Usa il tuo metodo!)
                Optional<ExportType> selection = ExportType.fromMenuId(inputId);

                if (selection.isPresent() && availableTypes.contains(selection.get())) {
                    return selection;
                }
            } else {
                scanner.nextLine(); // Flush
            }

            System.out.println("❌ Invalid ID. Please select a number from the table.");
        }
    }

    @Override
    public String askForFilename() {
        printSingleSeparator();
        System.out.println("📝  FILE SETTINGS");

        while (true) {
            System.out.print("\n> Enter filename (without extension): ");

            String name = scanner.nextLine().trim();

            // Validation: Empty check and spaces check
            if (name.isEmpty() || name.contains(" ")) {
                System.out.println("⚠️ Filename cannot be empty or contain spaces.");
                System.out.println("   Please use underscores (_) or dashes (-) for spaces instead.");
                continue;
            }

            // Validation: Illegal characters (Windows/Linux safe)
            // Regex checks for: < > : " / \ | ? *
            if (name.matches(".*[<>:\"/\\\\|?*].*")) {
                System.out.println("❌ Invalid characters detected (<>:\"/\\|?*).");
                System.out.println("   Please use only letters, numbers, underscores, or dashes.");
            } else {
                return name;
            }
        }
    }

    @Override
    public void showSuccessMessage(String path) {
        System.out.println("\n✅ EXPORT SUCCESSFUL!");
        System.out.println("   File saved at: " + path);
        printSingleSeparator();
    }

    @Override
    public void showErrorMessage(String error) {
        System.out.println("\n⛔ EXPORT FAILED:");
        System.out.println("   " + error);
        System.out.println("   Please try again with a different name or path.");
    }

    /**
     * Asks the user how to handle a file conflict.
     * @param filename The name of the file causing the conflict.
     * @return true for Overwrite, false for Rename.
     */
    public boolean askOverwriteOrRename(String filename) {
        System.out.println("⚠️ File '" + filename + "' already exists.");
        System.out.print("\n> Do you want to [O]verwrite or [R]ename? (o/r): ");

        while (true) {
            // nextLine() per sicurezza sul buffer
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("o")) {
                System.out.println("🔄 Overwriting...");
                return true;
            } else if (input.equalsIgnoreCase("r")) {
                System.out.println("↩️ Returning to selection...");
                return false;
            } else {
                System.out.print("❌ Invalid choice. Enter 'o' or 'r': ");
            }
        }
    }
}
