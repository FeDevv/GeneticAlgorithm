package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

/**
 * <p><strong>Concrete View Implementation for CLI (Command Line Interface).</strong></p>
 *
 * <p>This class handles the user interaction for the Export Wizard.
 * It is responsible for displaying menus, capturing filenames, and providing visual feedback
 * (success/error messages) using standard system I/O.</p>
 */
@SuppressWarnings("java:S106")
public class ConsoleExportView implements ExportViewContract {

    private final Scanner scanner;

    /**
     * Constructs the view with a shared Scanner instance.
     * @param scanner The system input scanner.
     */
    public ConsoleExportView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Optional<ExportType> askForExportType(List<ExportType> availableTypes) {

        // --- VALIDATION LOOP ---
        // Keeps the user trapped until a valid selection (or explicit exit) is made.
        while (true) {
            System.out.println("\n--- Select Export Format ---");
            for (ExportType type : availableTypes) {
                System.out.println(type.getMenuId() + ") " + type.getDisplayName());
            }
            System.out.println("0) Skip Export");

            // Robust integer reading
            int choice = readMenuChoice();

            // CASE 1: Explicit Exit (User chose 0)
            if (choice == 0) {
                return Optional.empty();
            }

            // CASE 2: ID Resolution
            Optional<ExportType> typeOpt = ExportType.fromMenuId(choice);

            if (typeOpt.isPresent()) {
                return typeOpt; // Valid ID mapped to an ExportType
            }

            // CASE 3: Unmapped ID (e.g., User entered 99)
            System.out.println("\n❌ Invalid selection ID. Please try again.");
            // Loop restarts...
        }
    }

    @Override
    public String askForFilename() {
        // --- SCANNER BUFFER FLUSH ---
        // When switching from token-based reading (nextInt) to line-based reading (nextLine),
        // a dangling newline character often remains in the buffer. We consume it here to prevent
        // the next input from being skipped automatically.
        scanner.nextLine();

        String filename;

        // --- INPUT LOOP ---
        while (true) {
            System.out.print("Enter file-name (without extension): ");

            // Read the full line and trim leading/trailing whitespace
            filename = scanner.nextLine().trim();

            // Validation Rules:
            if (filename.isEmpty()) {
                System.out.println("\n❌Error: The file-name cannot be empty.");
            } else if (filename.contains(" ")) {
                // Design Choice: We forbid spaces to ensure filesystem compatibility and simpler parsing.
                System.out.println("\n❌Error: The file-name can't contain empty spaces ' '. Retry.");
            } else {
                // Input is valid
                break;
            }
        }
        return filename;
    }

    @Override
    public void showSuccessMessage(String filePath) {
        System.out.println("\n✅ Export successful! File saved to:");
        System.out.println("   -> " + filePath);
    }

    @Override
    public void showErrorMessage(String message) {
        System.err.println("\n❌ Export Failed: " + message);
    }

    // --- Helper ---
    private int readMenuChoice() {
        int choice = -1;

        do {
            System.out.print("Enter your choice: ");

            // Syntax Check: Is it an integer?
            while (!scanner.hasNextInt()) {
                System.out.println("\n❌ Non-numeric value. Please enter an integer.");
                scanner.next(); // Discard invalid token
                System.out.print("Enter your choice: ");
            }

            choice = scanner.nextInt();

            // Range Check: Is it non-negative?
            if (choice < 0) {
                System.out.println("\n❌ Invalid choice. Please enter a non-negative integer.");
            }

        } while (choice < 0);

        return choice;
    }
}
