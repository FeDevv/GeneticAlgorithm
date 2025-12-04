package org.agroplanner.exportsystem.views;

import org.agroplanner.exportsystem.model.ExportType;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;
@SuppressWarnings("java:S106")
public class ConsoleExportView implements ExportViewContract {

    private final Scanner scanner;

    public ConsoleExportView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Optional<ExportType> askForExportType(List<ExportType> availableTypes) {

        // Aggiungiamo il while-true per intrappolare l'utente finché non sceglie bene
        while (true) {
            System.out.println("\n--- Select Export Format ---");
            for (ExportType type : availableTypes) {
                System.out.println(type.getMenuId() + ") " + type.getDisplayName());
            }
            System.out.println("0) Skip Export");

            int choice = readMenuChoice();

            // CASO 1: Uscita volontaria
            if (choice == 0) {
                return Optional.empty();
            }

            // CASO 2: Verifica validità ID
            Optional<ExportType> typeOpt = ExportType.fromMenuId(choice);

            if (typeOpt.isPresent()) {
                return typeOpt; // Ritorna solo se è un tipo valido
            }

            // CASO 3: ID numerico positivo ma non mappato (es. 8)
            System.out.println("\n❌ Invalid selection ID. Please try again.");
            // Il ciclo ricomincia
        }
    }

    @Override
    public String askForFilename() {
        // Consuma newline pendenti se necessario, logica identica alla tua precedente
        // (Assumendo scanner condiviso pulito, o aggiungendo nextLine() di sicurezza)
        scanner.nextLine();

        String filename;

        while (true) {
            System.out.print("Enter file-name (without extension): ");

            // Leggiamo tutta la riga inserita dall'utente
            filename = scanner.nextLine().trim();

            if (filename.isEmpty()) {
                System.out.println("\n❌Error: The file-name cannot be empty.");
            } else if (filename.contains(" ")) {
                System.out.println("\n❌Error: The file-name can't contain empty spaces ' '. Retry.");
            } else {
                // Se non è vuoto e non ha spazi, è valido
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

            // 1. Controllo che sia un intero
            while (!scanner.hasNextInt()) {
                System.out.println("\n❌ Non-numeric value. Please enter an integer.");
                scanner.next(); // Scarta l'input sporco
                System.out.print("Enter your choice: ");
            }

            choice = scanner.nextInt();

            // 2. Controllo che non sia negativo
            if (choice < 0) {
                System.out.println("\n❌ Invalid choice. Please enter a non-negative integer.");
            }

        } while (choice < 0);

        return choice;
    }
}
