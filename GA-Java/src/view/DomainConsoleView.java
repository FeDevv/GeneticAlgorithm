package view;

import model.domains.DomainType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * Implementazione concreta della View per l'interfaccia a riga di comando (CLI).
 * <p>
 * Questa classe è responsabile esclusiva di tutta la logica di I/O (System.out e Scanner)
 * e di contenere tutte le stringhe di output visibili all'utente.
 */
public class DomainConsoleView {

    private final Scanner scanner;

    /**
     * Costruttore che inietta l'oggetto Scanner per l'input.
     * @param scanner L'oggetto {@code Scanner} utilizzato per leggere l'input utente.
     */
    public DomainConsoleView(Scanner scanner) {
        this.scanner = scanner;
    }


    //Visualizza un messaggio di successo dopo la creazione di un dominio.
    public void showSuccess() {
        System.out.println("\n✅ Domain successfully created!");
    }

    /**
     * Visualizza un messaggio di errore.
     * System.err e System.out non sono Sincronizzati!!!!
     * Questa funzione la tengo per errori deep, e non errori della view!
     * @param msg Il messaggio di errore da mostrare.
     */
    public void showError(String msg) {
        System.err.println("\n❌ Creation or validation error: " + msg);
    }

    /**
     * Visualizza il menu dei tipi di dominio e legge la scelta numerica dell'utente.
     * Gestisce e scarta gli input non numerici.
     *
     * @return L'ID numerico selezionato dall'utente (0 per uscire).
     */
    public int readMenuChoice() {
        int choice = -1; // Inizializzato a un valore non valido

        do {
            System.out.print("Enter your choice: ");

            // --- 1. Controllo del Tipo (Must be an integer) ---
            // Continua a ciclare finché lo scanner non trova un intero
            while (!scanner.hasNextInt()) {
                System.out.println("\n❌Non-numeric value. Please enter an integer.");
                scanner.next(); // Scarta l'input non valido (es. testo)
                System.out.print("Enter your choice: ");
            }

            // Legge il valore
            choice = scanner.nextInt();

            // --- 2. Controllo del Valore (Must be non-negative: >= 0) ---
            if (choice < 0) {
                System.out.println("\n❌Invalid choice. Please enter a non negative integer (>= 0).");
                // Il ciclo 'do-while' si ripeterà grazie alla condizione esterna.
            }

        } while (choice < 0); // Continua a ciclare finché la scelta non è strettamente positiva

        return choice;
    }

    /**
     * Legge sequenzialmente i valori per i parametri richiesti, gestendo gli input non numerici.
     *
     * @param keys La lista dei nomi dei parametri da richiedere (chiavi).
     * @param domainName Il nome amichevole del dominio per l'output.
     * @return Una {@code Map<String, Double>} contenente i parametri raccolti.
     */
    public Map<String, Double> readParameters(List<String> keys, String domainName) {
        Map<String, Double> params = new HashMap<>();
        System.out.println("\n--- Parameter Entry for " + domainName + " ---");

        for (String key : keys) {
            double value;

            while (true) { // Cicla finché non ottieni un valore strettamente positivo
                System.out.print("Enter the value for '" + key + "' (must be > 0): ");

                // 1. Controllo Tipo (Non numerico)
                if (scanner.hasNextDouble()) {
                    value = scanner.nextDouble();

                    // 2. Controllo Valore (Non positivo)
                    if (value <= 0) {
                        System.out.println("\n❌Invalid value. The parameter '" + key + "' must be strictly positive (> 0). Retry.");
                        // Il loop 'while (value <= 0)' si ripete
                    } else {
                        // Valore valido trovato
                        params.put(key, value);
                        break;
                    }
                } else {
                    // Errore di tipo
                    System.out.println("\n❌Non-numeric value. Retry.");
                    scanner.next(); // Scarta l'input non valido
                }
            }
        }
        return params;
    }

    /**
     * Visualizza il menu dei tipi di dominio disponibili.
     * @param types La lista di {@code DomainType} da mostrare.
     */
    public void showMenu(List<DomainType> types) {
        System.out.println("\n--- Select domain type ---");
        for (DomainType type : types) {
            System.out.println(type.getMenuId() + ") " + type.getDisplayName());
        }
        System.out.println("0) Quit");
    }

}
