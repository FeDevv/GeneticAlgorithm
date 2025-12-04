package org.agroplanner.domainsystem.views;

import org.agroplanner.domainsystem.model.DomainType;

import java.util.*;

@SuppressWarnings("java:S106")
public class ConsoleDomainView implements DomainViewContract {

    private final Scanner scanner;

    public ConsoleDomainView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public Optional<DomainType> askForDomainType(List<DomainType> types) {

        // --- CICLO DI VALIDAZIONE INTERNO ALLA VIEW ---
        while (true) {
            // 1. Stampa il menu
            System.out.println("\n--- Select domain type ---");
            for (DomainType type : types) {
                System.out.println(type.getMenuId() + ") " + type.getDisplayName());
            }
            System.out.println("0) Quit");

            // 2. Leggi input (già validato come intero >= 0)
            int choice = readMenuChoice();

            // CASO A: Uscita esplicita
            if (choice == 0) {
                return Optional.empty();
            }

            // CASO B: Verifica se l'ID mappa a qualcosa
            Optional<DomainType> result = DomainType.fromMenuId(choice);

            if (result.isPresent()) {
                return result; // ID Trovato e valido -> Ritorna al controller
            }

            // CASO C: ID non mappato (es. 8)
            // Non ritorniamo nulla, stampiamo errore e il while ricomincia
            System.out.println("\n❌ Invalid selection ID. Please try again.");
        }
    }

    @Override
    public Map<String, Double> askForParameters(DomainType type) {
        Map<String, Double> params = new HashMap<>();
        System.out.println("\n--- Parameter Entry for " + type.getDisplayName() + " ---");

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

    // --- Metodi privati di supporto (Helper per Scanner) ---

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
