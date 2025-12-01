package org.example.views;

import org.example.services.export.ExportType;

import java.util.List;
import java.util.Scanner;

@SuppressWarnings("java:S106")
public class ExportConsoleView {

    private final Scanner scanner;

    /**
     * Costruttore con iniezione dello Scanner per mantenere coerenza con l'architettura.
     *
     * @param scanner L'oggetto condiviso per l'input.
     */
    public ExportConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- OUTPUT METHODS -------------------

    /**
     * Visualizza il menu dei formati di export disponibili.
     *
     * @param types La lista di enum ExportType da mostrare.
     */
    public void showMenu(List<ExportType> types) {
        System.out.println("\n--- Select Export Format ---");
        for (ExportType type : types) {
            System.out.println(type.getMenuId() + ") " + type.getDisplayName());
        }
        System.out.println("0) Skip Export");
    }

    /**
     * Messaggio di successo.
     *
     * @param path Il percorso dove è stato salvato il file.
     */
    public void showSuccess(String path) {
        System.out.println("\n✅ Export successful! File saved to:");
        System.out.println("   -> " + path);
    }

    /**
     * Messaggio di errore (utilizza System.err come da tua convenzione).
     *
     * @param msg Il messaggio di dettaglio.
     */
    public void showError(String msg) {
        System.err.println("\n❌ Export Failed: " + msg);
    }

    // ------------------- INPUT METHODS -------------------

    /**
     * Legge la scelta numerica dell'utente dal menu.
     * Identico alla logica di DomainConsoleView per coerenza UX.
     *
     * @return L'ID scelto (0 per uscire/saltare).
     */
    public int readMenuChoice() {
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

    /**
     * Richiede all'utente il nome del file.
     * Impedisce stringhe vuote.
     *
     * @return Il nome del file inserito dall'utente.
     */
    public String readFilename() {
        String filename;

        // Consumiamo eventuali residui di "a capo" nel buffer se necessario.
        scanner.nextLine();

        while (true) {
            System.out.print("Enter filename (without extension): ");

            // Leggiamo tutta la riga inserita dall'utente
            filename = scanner.nextLine().trim();

            if (filename.isEmpty()) {
                System.out.println("\n❌Errore: Il nome del file non può essere vuoto.");
            } else if (filename.contains(" ")) {
                System.out.println("\n❌Errore: Il nome del file non deve contenere spazi. Riprova.");
            } else {
                // Se non è vuoto e non ha spazi, è valido
                break;
            }
        }
        return filename;
    }
}
