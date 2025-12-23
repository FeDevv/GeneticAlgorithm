package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
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
 * <p><strong>Static Analysis:</strong>
 * Suppresses {@code java:S106} as standard output usage is expected for this CLI component.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleExportView implements ExportViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view using the shared system scanner.
     * @param scanner The input source (System.in).
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

    /**
     * {@inheritDoc}
     * <p><strong>Input Strategy:</strong>
     * Uses a validation loop to ensure the user selects a valid ID from the table.
     * Handles '0' as an explicit exit signal (returning {@code Optional.empty()}).
     * </p>
     */
    @Override
    public Optional<ExportType> askForExportType(List<ExportType> availableTypes) {
        // Validation Loop
        while (true) {
            System.out.print("\n> Select ID: ");

            if (scanner.hasNextInt()) {
                int inputId = scanner.nextInt();
                scanner.nextLine(); // Consume newline buffer

                // CASE A: User Cancel
                if (inputId == 0) return Optional.empty();

                // CASE B: Valid Lookup
                Optional<ExportType> selection = ExportType.fromMenuId(inputId);

                // Verification: Exists AND is in the provided list
                if (selection.isPresent() && availableTypes.contains(selection.get())) {
                    return selection;
                }
            } else {
                scanner.nextLine(); // Flush invalid token
            }

            System.out.println("❌ Invalid ID. Please select a number from the table.");
        }
    }

    /**
     * {@inheritDoc}
     * <p><strong>Sanitization Logic:</strong>
     * Enforces strict filename rules to ensure cross-platform compatibility and security.
     * Rejects:
     * <ul>
     * <li>Empty strings or spaces (forces underscores/dashes).</li>
     * <li>Reserved characters: {@code < > : " / \ | ? *}</li>
     * </ul>
     * </p>
     */
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

    /**
     * {@inheritDoc}
     */
    @Override
    public void showSuccessMessage(String path) {
        System.out.println("\n✅ EXPORT SUCCESSFUL!");
        System.out.println("   File saved at: " + path);
        printSingleSeparator();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showErrorMessage(String error) {
        System.out.println("\n⛔ EXPORT FAILED:");
        System.out.println("   " + error);
        System.out.println("   Please try again with a different name or path.");
    }

    /**
     * {@inheritDoc}
     * <p><strong>UX Pattern:</strong> Binary Choice Loop (Overwrite vs Rename).</p>
     */
    public boolean askOverwriteOrRename(String filename) {
        System.out.println("⚠️ File '" + filename + "' already exists.");
        System.out.print("\n> Do you want to [O]verwrite or [R]ename? (o/r): ");

        while (true) {
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
