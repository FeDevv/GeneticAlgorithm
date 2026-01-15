package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * Concrete implementation of the Export View targeting the Command Line Interface (CLI).
 *
 * <p><strong>Architecture & UX:</strong></p>
 * <ul>
 * <li><strong>Role:</strong> Handles the user interaction for the "Export Wizard". It translates high-level requests
 * (e.g., "Select Format") into concrete console inputs/outputs.</li>
 * <li><strong>Defensive UI:</strong> Implements aggressive <strong>Input Sanitization</strong> at the source.
 * It validates filenames against OS-specific illegal characters <em>before</em> sending them to the Controller,
 * providing immediate feedback to the user.</li>
 * <li><strong>Data Safety:</strong> Manages the "Conflict Resolution" dialog (Overwrite vs Rename), acting as
 * a safety gate to prevent accidental data loss.</li>
 * </ul>
 *
 */
@SuppressWarnings("java:S106")
public class ConsoleExportView implements ExportViewContract {

    private final Scanner scanner;

    public ConsoleExportView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- HELPER METHODS -------------------

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    // ------------------- INTERACTION LOGIC -------------------

    @Override
    public void showAvailableExports(List<ExportType> availableTypes) {
        System.out.println("\n");
        // ASCII ART: EXPORT SYSTEM
        System.out.println("███████╗██╗  ██╗██████╗  ██████╗ ██████╗ ████████╗");
        System.out.println("██╔════╝╚██╗██╔╝██╔══██╗██╔═══██╗██╔══██╗╚══██╔══╝");
        System.out.println("█████╗   ╚███╔╝ ██████╔╝██║   ██║██████╔╝   ██║   ");
        System.out.println("██╔══╝   ██╔██╗ ██╔═══╝ ██║   ██║██╔══██╗   ██║   ");
        System.out.println("███████╗██╔╝ ██╗██║     ╚██████╔╝██║  ██║   ██║   ");
        System.out.println("╚══════╝╚═╝  ╚═╝╚═╝      ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ");
        System.out.println("                  Report Generation Module            ");
        printSingleSeparator();

        System.out.println(" 💾  EXPORT WIZARD");
        System.out.println("     Select a format to save your optimization results.");
        System.out.println();

        // Header Tabella
        System.out.println("┌──────┬────────────┬────────────────────────────────────────────────────────────────┐");
        System.out.println("│  ID  │ FORMAT     │ DESCRIPTION                                                    │");
        System.out.println("├──────┼────────────┼────────────────────────────────────────────────────────────────┤");

        for (ExportType type : availableTypes) {
            System.out.printf(Locale.US, "│  %-2d  │ %-10s │ %-62s │%n",
                    type.getMenuId(),
                    type.name(),
                    type.getExportInfo()
            );
        }
        System.out.println("└──────┴────────────┴────────────────────────────────────────────────────────────────┘");
        System.out.println("    (Enter 0 to Cancel)");
    }

    @Override
    public Optional<ExportType> askForExportType(List<ExportType> availableTypes) {
        while (true) {
            System.out.print("\n👉 Select Format ID: ");

            if (scanner.hasNextInt()) {
                int inputId = scanner.nextInt();
                scanner.nextLine(); // Consume newline buffer

                // CASE A: User Cancel
                if (inputId == 0) return Optional.empty();

                // CASE B: Valid Lookup
                Optional<ExportType> selection = ExportType.fromMenuId(inputId);

                if (selection.isPresent() && availableTypes.contains(selection.get())) {
                    return selection;
                }
            } else {
                scanner.nextLine();
            }

            System.out.println("  ❌ Invalid Selection. Please check the ID table above.");
        }
    }

    @Override
    public String askForFilename() {
        System.out.println();
        System.out.println("📝  FILE SETTINGS");
        printSingleSeparator();

        while (true) {
            System.out.print("👉 Enter filename (without extension): ");

            String name = scanner.nextLine().trim();

            // Validation: Empty check and spaces check
            if (name.isEmpty() || name.contains(" ")) {
                System.out.println("  ⚠️  Filename cannot be empty or contain spaces.");
                System.out.println("      Use underscores (_) or dashes (-) instead.");
                continue;
            }

            // Validation: Illegal characters (Windows/Linux safe)
            if (name.matches(".*[<>:\"/\\\\|?*].*")) {
                System.out.println("  ❌ Invalid characters detected (<>:\"/\\|?*).");
                System.out.println("      Please use only letters, numbers, underscores, or dashes.");
            } else {
                return name;
            }
        }
    }

    @Override
    public void showSuccessMessage(String path) {
        System.out.println("\n ✅  EXPORT SUCCESSFUL!");
        System.out.println("     File saved at: " + path);
        printSingleSeparator();
    }

    @Override
    public void showErrorMessage(String error) {
        System.out.println("\n ⛔  EXPORT FAILED");
        System.out.println("     Error: " + error);
        System.out.println("     Please try again with a different name or path.");
        printSingleSeparator();
    }

    @Override
    public boolean askOverwriteOrRename(String filename) {
        System.out.println("\n ⚠️  FILE CONFLICT");
        System.out.println("    The file '" + filename + "' already exists.");

        while (true) {
            System.out.print("👉 Do you want to [O]verwrite or [R]ename? (o/r): ");
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("o")) {
                System.out.println("    🔄 Overwriting existing file...");
                return true;
            } else if (input.equalsIgnoreCase("r")) {
                System.out.println("    ↩️  Returning to filename selection...");
                return false;
            }
            System.out.println("  ❌ Invalid input. Type 'o' or 'r'.");
        }
    }

    @Override
    public void showGuestExportRestricted() {
        System.out.println("\n 🔒 EXPORT DISABLED");
        System.out.println("    You are running in Guest/Demo mode.");
        System.out.println("    To export professional reports (PDF/Excel), please login with a standard account.");
        printSingleSeparator();
    }
}
