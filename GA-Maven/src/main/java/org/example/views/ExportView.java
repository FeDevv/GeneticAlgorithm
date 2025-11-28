package org.example.views;

import java.util.Scanner;

@SuppressWarnings("java:S106")
public class ExportView {

    private final Scanner scanner;

    public ExportView(Scanner scanner) { this.scanner = scanner; }

    public boolean askForExport() {

        while (true) {
            System.out.print("would you like to export the solution? [y/n]: ");
            String choice = scanner.nextLine().trim();
            if (choice.equalsIgnoreCase("y")) {
                return true;
            } else if (choice.equalsIgnoreCase("n")) {
                return false;
            } else {
                System.out.println("Invalid choice. Try again.");
            }
        }

    }

    public String askForFileName() {
        String filename;

        while (true) {
            System.out.print("Enter file name (e.g. run1): ");
            filename = scanner.nextLine().trim();

            if (filename.isEmpty()) {
                System.out.println("No file name has been entered.");
                System.out.println("Do you wish to rewrite the name? [y/n]: ");
                String choice = scanner.nextLine().trim();
                if (choice.equalsIgnoreCase("y")) {
                    continue;
                } else {
                    System.out.println("the defaul name 'export_default' will be used.");
                    filename = "export_default";
                    break;
                }
            }
        }

        System.out.println("✅file name has been successfully set.");

        return filename;
    }

    public void showSuccess(String filename, String filepath) {
        System.out.println("\n[EXPORT] ✅ file '" + filename + "' successfully exported.");
        System.out.println("This file has been saven in: " + filepath);
    }

    public void showError(String errorMessage) {
        System.out.println("\n[EXPORT] ❌ Error occurred while saving file.");
        System.out.println("Details: " + errorMessage);
    }



}
