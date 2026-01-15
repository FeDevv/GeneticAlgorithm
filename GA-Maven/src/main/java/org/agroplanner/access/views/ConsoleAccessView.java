package org.agroplanner.access.views;

import org.agroplanner.access.model.CredentialsDTO;
import org.agroplanner.access.model.Role;

import java.util.Scanner;
import java.util.regex.Pattern;

/**
 * Console-based implementation of the Access View.
 * <p>
 * Handles standard I/O operations, input regex validation, and visual formatting
 * for the authentication workflow.
 * </p>
 */
public class ConsoleAccessView implements AccessViewInterface {

    private final Scanner scanner;

    public ConsoleAccessView(Scanner scanner) {
        this.scanner = scanner;
    }

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    @Override
    public void showWelcomeMessage() {
        System.out.println("\n");
        System.out.println(" █████╗  ██████╗  ██████╗███████╗███████╗███████╗    ██████╗████████╗██████╗ ██╗     ");
        System.out.println("██╔══██╗██╔════╝ ██╔════╝██╔════╝██╔════╝██╔════╝   ██╔════╝╚══██╔══╝██╔══██╗██║     ");
        System.out.println("███████║██║      ██║     █████╗  ███████╗███████╗   ██║        ██║   ██████╔╝██║     ");
        System.out.println("██╔══██║██║      ██║     ██╔══╝  ╚════██║╚════██║   ██║        ██║   ██╔══██╗██║     ");
        System.out.println("██║  ██║╚██████╗ ╚██████╗███████╗███████║███████║   ╚██████╗   ██║   ██║  ██║███████╗");
        System.out.println("╚═╝  ╚═╝ ╚═════╝  ╚═════╝╚══════╝╚══════╝╚══════╝    ╚═════╝   ╚═╝   ╚═╝  ╚═╝╚══════╝");
        System.out.println("                       Secure Authentication Gateway                                 ");
        printDoubleSeparator();
    }

    @Override
    public int askInitialChoice() {
        System.out.println("\nSELECT ACTION:");
        // Tabella Larga
        System.out.println("┌─────┬──────────────────────────┬──────────────────────────────────────┐");
        System.out.println("│ ID  │ ACTION                   │ DESCRIPTION                          │");
        System.out.println("├─────┼──────────────────────────┼──────────────────────────────────────┤");
        System.out.println("│  1  │ Login                    │ Access your personal dashboard       │");
        System.out.println("│  2  │ Registration             │ Create a new user account            │");
        System.out.println("│  0  │ Exit System              │ Terminate the session                │");
        System.out.println("└─────┴──────────────────────────┴──────────────────────────────────────┘");

        while (true) {
            System.out.print("\n> Select ID: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                scanner.nextLine();
                return choice;
            } else {
                String trash = scanner.nextLine();
                System.out.printf("  ❌ Invalid input '%s'. Please enter a number.%n", trash);
            }
        }
    }

    @Override
    public CredentialsDTO askLoginDetails() {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 👤  USER LOGIN");
        printSingleSeparator();

        String u = readString("Username");
        String p = readString("Password");

        System.out.println();
        return new CredentialsDTO(u, p);
    }

    @Override
    public CredentialsDTO askRegistrationDetails() {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 📝  NEW USER REGISTRATION");
        printSingleSeparator();
        System.out.println(" Please enter the required details below.\n");

        // REGEX DEFINITIONS
        String nameRegex = "^[a-zA-Z\\s]+$";
        String nameError = "Invalid format (use only letters, no numbers or symbols).";
        String emailRegex = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";
        String phoneRegex = "^\\d+$";

        // 1. DATA COLLECTION
        String fname = askValidatedInput("First Name",     nameRegex,   nameError);
        String lname = askValidatedInput("Last Name",      nameRegex,   nameError);
        String email = askValidatedInput("Email Address",  emailRegex,  "Invalid email format (must be user@domain.com).");
        String phone = askValidatedInput("Phone Number",   phoneRegex,  "Invalid format (digits only).");

        // Username & Password (min 3 chars)
        String username = askValidatedInput("Username", ".{3,}", "Username must be at least 3 characters long.");
        String password = askValidatedInput("Password", ".{3,}", "Password must be at least 3 characters long.");

        // 2. ROLE SELECTION TABLE
        System.out.println("\nSELECT ACCOUNT TYPE:");
        System.out.println("┌─────┬──────────────────────┬──────────────────────────────┐");
        System.out.println("│ ID  │ ROLE                 │ PRIVILEGES                   │");
        System.out.println("├─────┼──────────────────────┼──────────────────────────────┤");
        System.out.println("│  1  │ Standard User        │ Manage personal fields       │");
        System.out.println("│  2  │ Agronomist           │ Manage catalog & varieties   │");
        System.out.println("└─────┴──────────────────────┴──────────────────────────────┘");

        String roleChoice = askValidatedInput("Select Role ID", "^[1-2]$", "Please enter 1 or 2.");

        Role role = Role.USER;
        if (roleChoice.equals("2")) {
            role = Role.AGRONOMIST;
        }

        return new CredentialsDTO(username, password, fname, lname, email, phone, role);
    }

    private String askValidatedInput(String label, String regex, String errorMessage) {
        Pattern pattern = Pattern.compile(regex);

        while (true) {
            System.out.printf("> %-20s: ", label);
            String input = scanner.nextLine().trim();

            if (input.isEmpty()) {
                System.out.println("  ⚠️  Field cannot be empty.\n");
                continue;
            }

            if (!pattern.matcher(input).matches()) {
                System.out.println("  ❌ " + errorMessage + "\n");
                continue;
            }
            return input;
        }
    }

    private String readString(String label) {
        while (true) {
            System.out.printf("> %-20s: ", label);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) {
                return input;
            }
            // repeat w/o error message
        }
    }

    // ------------------- FEEDBACK & ANIMATIONS -------------------

    @Override
    public void showAgronomistValidationSequence() {
        System.out.println("\n[SYSTEM] 🔍 Agronomist registration request detected.");
        System.out.print("[SYSTEM] 📡 Contacting National Register");
        try {
            for(int i=0; i<6; i++) {
                Thread.sleep(600);
                System.out.print(".");
            }
            Thread.sleep(400);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
        System.out.println("\n[SYSTEM] ✅ LICENSE VERIFIED. Welcome, Doctor.\n");
    }

    @Override
    public void showSuccessMessage(String message) {
        System.out.println("  ✅ " + message);
    }

    @Override
    public void showErrorMessage(String message) {
        System.out.println("  ⛔ ERROR: " + message);
    }

}
