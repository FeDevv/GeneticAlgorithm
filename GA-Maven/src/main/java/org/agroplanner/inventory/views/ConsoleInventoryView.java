package org.agroplanner.inventory.views;

import org.agroplanner.access.model.User;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Scanner;

/**
 * CLI implementation of the Inventory View.
 * <p>
 * Handles standard I/O operations, tabular data rendering, and input validation loops.
 * Enforces {@link Locale#US} for all numeric I/O to ensure decimal separator consistency.
 * </p>
 */
@SuppressWarnings("java:S106")
public class ConsoleInventoryView implements InventoryViewContract {

    private final Scanner scanner;

    public ConsoleInventoryView(Scanner scanner) {
        this.scanner = scanner;
    }

    // ------------------- VISUAL HELPERS -------------------

    private void printDoubleSeparator() {
        System.out.println("══════════════════════════════════════════════════════════════════════════════════════");
    }

    private void printSingleSeparator() {
        System.out.println("──────────────────────────────────────────────────────────────────────────────────────");
    }

    // ------------------- WIZARD FLOW -------------------

    @Override
    public void showWizardStart() {
        System.out.println("\n");
        System.out.println("██╗███╗   ██╗██╗   ██╗███████╗███╗   ██╗████████╗ ██████╗ ██████╗ ██╗   ██╗");
        System.out.println("██║████╗  ██║██║   ██║██╔════╝████╗  ██║╚══██╔══╝██╔═══██╗██╔══██╗╚██╗ ██╔╝");
        System.out.println("██║██╔██╗ ██║██║   ██║█████╗  ██╔██╗ ██║   ██║   ██║   ██║██████╔╝ ╚████╔╝ ");
        System.out.println("██║██║╚██╗██║╚██╗ ██╔╝██╔══╝  ██║╚██╗██║   ██║   ██║   ██║██╔══██╗  ╚██╔╝  ");
        System.out.println("██║██║ ╚████║ ╚████╔╝ ███████╗██║ ╚████║   ██║   ╚██████╔╝██║  ██║   ██║   ");
        System.out.println("╚═╝╚═╝  ╚═══╝  ╚═══╝  ╚══════╝╚═╝  ╚═══╝   ╚═╝    ╚═════╝ ╚═╝  ╚═╝   ╚═╝   ");
        System.out.println("                  AgroPlanner™ Management System                          ");
        printDoubleSeparator();
        System.out.println(" Welcome to the Inventory Wizard. Compose the plant mix for the optimization.");
        printDoubleSeparator();
    }

    @Override
    public void showAvailablePlantTypes(PlantType[] types) {
        System.out.println("\nSELECT PLANT CATEGORY:");
        System.out.println("┌─────┬──────────────────────────┬──────┐");
        System.out.println("│ ID  │ CATEGORY NAME            │ ICON │");
        System.out.println("├─────┼──────────────────────────┼──────┤");

        for (PlantType type : types) {
            System.out.printf("│ %-3d │ %-24s │  %s  │%n",
                    type.getId(),
                    type.name(),
                    type.getLabel()
            );
        }
        System.out.println("└─────┴──────────────────────────┴──────┘");
        System.out.println("────────────────────────────────────────");
        System.out.println("  0. 🔙 GO BACK / FINISH SELECTION");
        System.out.println("────────────────────────────────────────");
    }

    @Override
    public Optional<PlantType> askForPlantType(PlantType[] types) {
        while (true) {
            System.out.print("Select ID > ");
            String input = scanner.next();

            try {
                int id = Integer.parseInt(input);

                if (id == 0) {
                    return java.util.Optional.empty();
                }

                java.util.Optional<PlantType> type = PlantType.getById(id);
                if (type.isPresent()) {
                    return type;
                }

                System.out.println("❌ Invalid ID. Please try again.");

            } catch (NumberFormatException _) {
                System.out.println("❌ Invalid input. Please enter a number.");
            }
        }
    }

    @Override
    public PlantVarietySheet askForVarietySelection(List<PlantVarietySheet> varieties) {
        System.out.println("\nAVAILABLE VARIETIES FOR: " + varieties.get(0).getType().getLabel());

        System.out.println("┌─────┬──────────────────────┬──────────┬──────────────────────┬───────────────────────────┐");
        System.out.println("│ ID  │ VARIETY NAME         │ RADIUS(m)│ AUTHOR               │ CONTACT EMAIL             │");
        System.out.println("├─────┼──────────────────────┼──────────┼──────────────────────┼───────────────────────────┤");

        for (int i = 0; i < varieties.size(); i++) {
            PlantVarietySheet p = varieties.get(i);

            String authorName = (p.getAuthor() != null) ? p.getAuthor().getFullName() : "Unknown";
            String authorEmail = (p.getAuthor() != null) ? p.getAuthor().getEmail() : "N/A";

            System.out.printf("│ %-3d │ %-20s │ %-8.2f │ %-20s │ %-25s │%n",
                    (i + 1),
                    truncate(p.getVarietyName(), 20),
                    p.getMinDistance(),
                    truncate(authorName, 20),
                    truncate(authorEmail, 25)
            );
        }
        System.out.println("└─────┴──────────────────────┴──────────┴──────────────────────┴───────────────────────────┘");

        while (true) {
            System.out.print("\n> Select Variety ID: ");
            if (scanner.hasNextInt()) {
                int choice = scanner.nextInt();
                if (choice >= 1 && choice <= varieties.size()) {
                    PlantVarietySheet selected = varieties.get(choice - 1);
                    System.out.printf("  ✅ Variety Selected: %s (%.2fm)%n", selected.getVarietyName(), selected.getMinDistance());
                    return selected;
                }
            } else {
                scanner.next();
            }
            System.out.println("  ❌ Invalid ID. Please select from the table.");
        }
    }

    @Override
    public int askForQuantity(String varietyName) {
        System.out.println();
        while (true) {
            System.out.printf("🌱 Quantity for '%s': ", varietyName);

            if (scanner.hasNextInt()) {
                int qty = scanner.nextInt();
                if (qty > 0) return qty;
                System.out.println("  ⚠️  Quantity must be at least 1.");
            } else {
                String trash = scanner.next();
                System.out.printf("  ❌ '%s' is not a number.%n", trash);
            }
            System.out.println();
        }
    }

    @Override
    public PlantVarietySheet askForNewSheetData(PlantType type, double maxDomainRadius) {
        System.out.println("\n📝 CREATE NEW VARIETY: " + type.getLabel());
        printSingleSeparator();

        PlantVarietySheet sheet = new PlantVarietySheet();
        sheet.setType(type);

        if (scanner.hasNextLine()) scanner.nextLine();

        sheet.setVarietyName(readString("Variety Name"));

        while (true) {
            System.out.printf(Locale.US, "> %-20s: ", "Min Distance (m)");
            if (scanner.hasNextDouble()) {
                double val = scanner.nextDouble();
                scanner.nextLine();
                if (val > 0 && val <= maxDomainRadius) {
                    sheet.setMinDistance(val);
                    break;
                }
                System.out.println("  ⚠️  Invalid distance (Must be > 0 and fit in domain, < " + maxDomainRadius + ").");
            } else {
                scanner.next();
                System.out.println("  ❌ Not a number.");
            }
        }

        sheet.setSowingPeriod(readString("Sowing Period"));
        sheet.setNotes(readString("Tech Notes"));
        return sheet;
    }

    // ------------------- FEEDBACK & UTILS -------------------

    @Override
    public void showNoVarietiesFound(PlantType type) {
        System.out.println("\n⚠️  NO VARIETIES FOUND.");
        System.out.printf("   There are no '%s' sheets saved in the database yet.%n", type.getName());
    }

    @Override
    public void showCurrentStatus(int totalItems, double maxCurrentRadius) {
        System.out.println("\n");
        printSingleSeparator();
        System.out.println(" 📊  CURRENT INVENTORY STATUS");
        printSingleSeparator();
        System.out.printf("   • Total Plants : %d%n", totalItems);
        System.out.printf(Locale.US, "   • Max Radius   : %.2f m%n", maxCurrentRadius);
        printSingleSeparator();
    }

    @Override
    public boolean askIfAddMore() {
        System.out.print("\n> Add another species? [y/n]: ");
        String input = scanner.next().trim();
        return input.equalsIgnoreCase("y");
    }

    @Override
    public void showSuccessMessage(String message) {
        System.out.println("  ✅ SUCCESS: " + message);
    }

    @Override
    public void showErrorMessage(String message) {
        System.out.println("  ⛔ ERROR: " + message);
    }

    private String truncate(String str, int width) {
        if (str == null) return "";
        if (str.length() > width) {
            return str.substring(0, width - 2) + "..";
        }
        return str;
    }

    private String readString(String label) {
        while (true) {
            System.out.printf("> %-20s: ", label);
            String input = scanner.nextLine().trim();
            if (!input.isEmpty()) return input;
        }
    }

    @Override
    public void displayDetailedManifest(List<PlantVarietySheet> sheets) {
        System.out.println("\n");
        printDoubleSeparator();
        System.out.println(" 📜  PROJECT MANIFEST: USED VARIETIES");
        printDoubleSeparator();

        for (PlantVarietySheet p : sheets) {
            System.out.printf(" 🌱 %-30s [%s]%n", p.getVarietyName().toUpperCase(), p.getType().name());
            System.out.printf("    • Constraint: Min Distance %.2fm | Sowing: %s%n", p.getMinDistance(), p.getSowingPeriod());
            System.out.printf("    • Notes: %s%n", (p.getNotes() == null || p.getNotes().isEmpty()) ? "N/A" : p.getNotes());

            // SEZIONE AUTORE (AGRONOMO)
            System.out.println("    ──────────────────────────────────────────────────────────────────");
            if (p.getAuthor() != null) {
                User u = p.getAuthor();
                System.out.printf("    🎓 AGRONOMIST: %s %s%n", u.getFirstName(), u.getLastName());
                System.out.printf("    📧 %-30s  📞 %s%n", u.getEmail(), u.getPhone());
            } else {
                System.out.println("    👤 AUTHOR: Unknown / Legacy Data");
            }
            printSingleSeparator();
        }
    }

}
