package org.agroplanner.orchestrator.views;

import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;

import java.util.Scanner;

/**
 * Concrete implementation of the System View for the CLI.
 */
@SuppressWarnings("java:S106")
public class ConsoleSystemView implements SystemViewContract {

    private final Scanner scanner;

    public ConsoleSystemView(Scanner scanner) {
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
        System.out.println("\n\n");
        System.out.println(" █████╗  ██████╗ ██████╗  ██████╗ ██████╗ ██╗      █████╗ ███╗   ██╗███╗   ██╗███████╗██████╗ ");
        System.out.println("██╔══██╗██╔════╝ ██╔══██╗██╔═══██╗██╔══██╗██║     ██╔══██╗████╗  ██║████╗  ██║██╔════╝██╔══██╗");
        System.out.println("███████║██║  ███╗██████╔╝██║   ██║██████╔╝██║     ███████║██╔██╗ ██║██╔██╗ ██║█████╗  ██████╔╝");
        System.out.println("██╔══██║██║   ██║██╔══██╗██║   ██║██╔═══╝ ██║     ██╔══██║██║╚██╗██║██║╚██╗██║██╔══╝  ██╔══██╗");
        System.out.println("██║  ██║╚██████╔╝██║  ██║╚██████╔╝██║     ███████╗██║  ██║██║ ╚████║██║ ╚████║███████╗██║  ██║");
        System.out.println("╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═╝ ╚═════╝ ╚═╝     ╚══════╝╚═╝  ╚═╝╚═╝  ╚═══╝╚═╝  ╚═══╝╚══════╝╚═╝  ╚═╝");
        System.out.println("                               Integrated Management System v3.0                                  ");
        printDoubleSeparator();
    }

    @Override
    public int askMainDashboardChoice(User user) {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 🏠  MAIN DASHBOARD");
        printSingleSeparator();

        System.out.printf (" 👤 User Logged : %s%n", user.getFullName());
        System.out.printf (" 🛡️  Access Role : %s%n", user.getRole().getLabel());

        System.out.println("\nSELECT OPERATION:");
        System.out.println("┌─────┬──────────────────────────┬──────────────────────────────────────┐");
        System.out.println("│ ID  │ MODULE                   │ DESCRIPTION                          │");
        System.out.println("├─────┼──────────────────────────┼──────────────────────────────────────┤");
        System.out.println("│  1  │ Start New Optimization   │ Define terrain & execute algorithm   │");
        System.out.println("│  2  │ Load Saved Terrain       │ Resume work from stored sessions     │");

        if (user.getRole() == Role.AGRONOMIST) {
            System.out.println("│  3  │ Catalog Management       │ Create/Edit Plant Varieties          │");
        }

        System.out.println("├─────┼──────────────────────────┼──────────────────────────────────────┤");
        System.out.println("│  0  │ Logout                   │ Return to access screen              │");
        System.out.println("└─────┴──────────────────────────┴──────────────────────────────────────┘");

        while (true) {
            System.out.print("\n> Select Module ID: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                boolean isAgronomist = (user.getRole() == Role.AGRONOMIST);

                if (choice == 0 || choice == 1 || choice == 2) return choice;
                if (choice == 3 && isAgronomist) return choice;
            } else {
                scanner.next();
            }
            System.out.println("  ❌ Invalid selection.");
        }
    }

    @Override
    public void showNewSessionMessage() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println(" 🚀  INITIALIZING OPTIMIZATION SESSION...");
        printDoubleSeparator();
    }

    @Override
    public boolean askForNewSession() {
        System.out.println();
        printSingleSeparator();
        return askBoolean("> Start a new task with this user?");
    }

    @Override
    public void showBootstrapInfo(String message) {
        System.out.println(" [KERNEL] " + message);
    }

    @Override
    public void showDemoModeActive() {
        System.out.println(" [KERNEL] 🚀 DEMO MODE ACTIVATED: Auto-login as Guest.");
    }

    @Override
    public void showSessionAborted(String reason) {
        System.out.println("\n⛔ TASK ABORTED.");
        System.out.println("   Reason: " + reason);
        System.out.println("   Returning to dashboard...\n");
        try { Thread.sleep(1200); } catch (Exception ignored) {}
    }

    @Override
    public void showExitMessage() {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println("   👋 SYSTEM SHUTDOWN. GOODBYE!");
        printDoubleSeparator();
    }

    @Override
    public void showLogoutMessage() {
        System.out.println("\n 🔒 Logging out...");
        try { Thread.sleep(800); } catch (Exception ignored) {}
    }

    @Override
    public void showDemoSessionEnded() {
        System.out.println(" 👋 Demo session ended.");
    }

    @Override
    public void showUnknownCommand() {
        System.out.println(" ❌ Unknown command.");
    }

    @Override
    public boolean askIfExportWanted() {
        System.out.println("\n📤 Would you like to EXPORT this solution? [y/n]");
        return askBoolean("> ");
    }

    @Override
    public void showDemoFeatureDisabled() {
        System.out.println("\nℹ️  DEMO MODE RESTRICTION: Data exists only in RAM.");
    }

    @Override
    public void waitForUserConfirmation() {
        System.out.println("\nPress ENTER to return to dashboard...");
        try {
            // Consumes the previous newline and waits for a new one
            if(scanner.hasNextLine()) scanner.nextLine(); // flush
            scanner.nextLine(); // wait
        } catch (Exception ignored) {}
    }

    @Override
    public void showExportError(String message) {
        System.err.println("❌ Export Initialization Failed: " + message);
    }

    private boolean askBoolean(String prompt) {
        while (true) {
            System.out.print(prompt + " [y/n]: ");
            String input = scanner.next().trim();
            if (input.equalsIgnoreCase("y")) return true;
            if (input.equalsIgnoreCase("n")) return false;
            System.out.println("  ❌ Invalid input.");
        }
    }
}