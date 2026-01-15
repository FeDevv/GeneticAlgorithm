package org.agroplanner.boot.view;

import org.agroplanner.boot.model.StartupMode;

import java.util.Scanner;

/**
 * CLI implementation of the Boot View.
 * <p>
 * Handles all standard output ({@code System.out}) and input ({@code System.in}) operations
 * during the startup phase.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleBootView implements BootViewContract {

    private final Scanner scanner;

    public ConsoleBootView(Scanner scanner) {
        this.scanner = scanner;
    }

    @Override
    public void showWelcomeBanner() {
        System.out.println("\n");
        System.out.println("██████╗  ██████╗  ██████╗ ████████╗");
        System.out.println("██╔══██╗██╔═══██╗██╔═══██╗╚══██╔══╝");
        System.out.println("██████╔╝██║   ██║██║   ██║   ██║   ");
        System.out.println("██╔══██╗██║   ██║██║   ██║   ██║   ");
        System.out.println("██████╔╝╚██████╔╝╚██████╔╝   ██║   ");
        System.out.println("╚═════╝  ╚═════╝  ╚═════╝    ╚═╝   ");
        System.out.println("     AgroPlanner System Loader     ");
        System.out.println();
    }

    @Override
    public StartupMode askForStartupMode(StartupMode[] modes) {
        System.out.println("Select System Configuration:");

        // Table Header
        System.out.println("┌─────┬──────────────────────────┬──────────────────────────────────────────┬──────────┐");
        System.out.println("│ ID  │ MODE NAME                │ STORAGE ENGINE                           │ UI       │");
        System.out.println("├─────┼──────────────────────────┼──────────────────────────────────────────┼──────────┤");

        // Table Rows
        for (StartupMode mode : modes) {
            String uiLabel = mode.isGuiActive() ? "JAVAFX" : "CLI";

            String storageDisplay = mode.getPersistenceType().getIcon() + " " + mode.getPersistenceType().getLabel();

            System.out.printf("│ %-3d │ %-24s │ %-40s │ %-8s │%n",
                    mode.getId(),
                    mode.getLabel(),
                    storageDisplay,
                    uiLabel
            );
        }
        // Table Footer
        System.out.println("└─────┴──────────────────────────┴──────────────────────────────────────────┴──────────┘");

        // Input Loop
        while (true) {
            System.out.print("\n> Select Boot ID: ");
            String input = scanner.nextLine().trim();

            try {
                int id = Integer.parseInt(input);
                StartupMode selected = StartupMode.fromId(id);
                if (selected != null) {
                    printConfirmation(selected);
                    return selected;
                }
            } catch (NumberFormatException ignored) {
                // Ignore parsing errors, loop again
            }

            System.out.println("  ❌ Invalid Boot ID.");
        }
    }

    /**
     * Visual confirmation of the selection with a short UX pause.
     */
    private void printConfirmation(StartupMode mode) {
        System.out.println("\n✅ CONFIGURATION LOADED: " + mode.name());
        System.out.println("   ────────────────────────────────────────────────");
        System.out.println("   • Engine : " + mode.getPersistenceType().getLabel());
        System.out.println("   • UI     : " + (mode.isGuiActive() ? "Graphical" : "Console"));
        System.out.println("   ────────────────────────────────────────────────\n");
        try {
            // UX Pause: gives the user a moment to read
            Thread.sleep(800);
        } catch (Exception ignored) {
            Thread.currentThread().interrupt();
        }
    }
}
