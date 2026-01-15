package org.agroplanner.persistence.views;

import org.agroplanner.gasystem.model.SolutionMetadata;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class PersistenceConsoleView implements PersistenceViewContract {

    private final Scanner scanner;

    public PersistenceConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    // Sostituisci il vecchio metodo askBoolean con questo:
    private boolean askBoolean() {
        while (true) {
            System.out.print("💾  Do you want to SAVE this solution to the database? [y/n]: ");
            // Uso nextLine per pulire tutto il buffer e trim per rimuovere spazi extra
            String input = scanner.nextLine().trim();

            if (input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("n")) return false;

            if (!input.isEmpty()) {
                System.out.println("  ❌ Invalid input. Type 'y' or 'n'.");
            }
        }
    }

    @Override
    public void showGuestSaveRestricted() {
        System.out.println("\n 🔒 SAVE DISABLED");
        System.out.println("    You are running in Guest/Demo mode.");
        System.out.println("    To save your solutions, please login with a standard account.");
    }

    @Override
    public boolean askIfSaveSolution() {
        System.out.println();
        printSingleSeparator();
        return askBoolean();
    }

    @Override
    public String askSolutionTitle() {
        System.out.print("📝  Enter a title for this solution (e.g., 'Tomato Field July'): ");

        String input = "";
        while (input.trim().isEmpty()) {
            input = scanner.nextLine();
        }

        return input.trim();
    }

    @Override
    public void showSaveSuccess() {
        System.out.println(" ✅  Solution saved successfully!");
        printSingleSeparator();
    }

    @Override
    public void showSaveError(String error) {
        System.out.println(" ❌  SAVE FAILED: " + error);
        printSingleSeparator();
    }

    @Override
    public void showNoSavedSolutions() {
        System.out.println(" ℹ️  No saved solutions found for your account.");
    }

    @Override
    public int askSelection(List<SolutionMetadata> list) {
        System.out.println("\n📂 SAVED SOLUTIONS:");
        printSingleSeparator();

        for (int i = 0; i < list.size(); i++) {
            SolutionMetadata meta = list.get(i);
            System.out.printf(" [%d] %s (Score: %.6f) - %s%n",
                    (i + 1),
                    meta.getTitle(),
                    meta.getFitness(),
                    meta.getCreationDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"))
            );
        }
        printSingleSeparator();
        System.out.println(" [0] Cancel / Go Back");

        while (true) {
            System.out.print("\n> Enter Number to load: ");
            if (scanner.hasNextInt()) {
                int inputIndex = scanner.nextInt();
                if (inputIndex == 0) return -1;
                if (inputIndex > 0 && inputIndex <= list.size()) {
                    return list.get(inputIndex - 1).getId();
                }
            } else {
                scanner.next();
            }
            System.out.println("  ❌ Invalid number. Choose from 1 to " + list.size());
        }
    }

    @Override
    public void showLoadError(String msg) {
        System.out.println("  ❌ LOAD ERROR: " + msg);
    }

    @Override
    public void showLoadingMessage(String msg) {
        System.out.println("\n ⏳ " + msg);
    }

}
