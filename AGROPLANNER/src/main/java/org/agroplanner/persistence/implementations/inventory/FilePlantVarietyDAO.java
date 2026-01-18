package org.agroplanner.persistence.implementations.inventory;

import org.agroplanner.access.model.User;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * File-based implementation of the Plant Inventory.
 * <p>
 * Persists data in a custom CSV format. To maintain author attribution in a flat-file system
 * without complex relational lookups, relevant author details (Name, Email, Phone) are
 * denormalized and stored directly within the plant record.
 * </p>
 * <p><strong>File Format Schema:</strong><br>
 * {@code ID;TYPE;VARIETY_NAME;DISTANCE;SOWING;NOTES;AUTHOR_NAME;AUTHOR_EMAIL;AUTHOR_PHONE}
 * </p>
 */
public class FilePlantVarietyDAO implements PlantVarietyDAOContract {

    private static final String FILE_PATH = "data/plants.txt";
    private static final String SEPARATOR = ";";

    @Override
    public void initStorage() {
        try {
            // 1. Dir creation
            Path directory = Paths.get("data");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            // 2. File creation
            Path filePath = Paths.get(FILE_PATH);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

        } catch (IOException e) {
            throw new DataPersistenceException("Critical Error: Unable to initialize Plant File Storage.", e);
        }
    }

    @Override
    public boolean save(PlantVarietySheet sheet) {
        int newId = generateNewId();
        sheet.setId(newId);

        StringBuilder sb = new StringBuilder();
        sb.append(sheet.getId()).append(SEPARATOR);
        sb.append(sheet.getType().name()).append(SEPARATOR);
        sb.append(cleanText(sheet.getVarietyName())).append(SEPARATOR);
        sb.append(String.format(Locale.US, "%.2f", sheet.getMinDistance())).append(SEPARATOR);
        sb.append(cleanText(sheet.getSowingPeriod())).append(SEPARATOR);
        sb.append(cleanText(sheet.getNotes())).append(SEPARATOR);

        // --- Denormalize Author Info ---
        String authorName = "Unknown";
        String authorEmail = "N/A";
        String authorPhone = "N/A";

        if (sheet.getAuthor() != null) {
            authorName = sheet.getAuthor().getFullName();
            authorEmail = sheet.getAuthor().getEmail();
            if (sheet.getAuthor().getPhone() != null && !sheet.getAuthor().getPhone().isEmpty()) {
                authorPhone = sheet.getAuthor().getPhone();
            }
        }

        sb.append(cleanText(authorName)).append(SEPARATOR);
        sb.append(cleanText(authorEmail)).append(SEPARATOR);
        sb.append(cleanText(authorPhone));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(sb.toString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            throw new DataPersistenceException("Error writing plant variety to file storage.", e);
        }
    }

    @Override
    public PlantVarietySheet findById(int id) {
        List<PlantVarietySheet> all = loadAllFromFile();
        for (PlantVarietySheet p : all) {
            if (p.getId() == id) return p;
        }
        return null;
    }

    @Override
    public List<PlantVarietySheet> findByType(PlantType type) {
        List<PlantVarietySheet> results = new ArrayList<>();
        List<PlantVarietySheet> all = loadAllFromFile();
        for (PlantVarietySheet p : all) {
            if (p.getType() == type) results.add(p);
        }
        return results;
    }

    @Override
    public List<PlantVarietySheet> findAll() {
        return loadAllFromFile();
    }

    @Override
    public List<PlantVarietySheet> findAllByIds(Set<Integer> ids) {
        List<PlantVarietySheet> results = new ArrayList<>();
        List<PlantVarietySheet> all = loadAllFromFile();
        for (PlantVarietySheet p : all) {
            if (ids.contains(p.getId())) {
                results.add(p);
            }
        }
        return results;
    }

    // --- HELPER METHODS ---

    private List<PlantVarietySheet> loadAllFromFile() {
        List<PlantVarietySheet> list = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return list;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEPARATOR, -1);

                // Ensure there are at least the mandatory columns (first 7)
                // Schema: 0:ID, 1:TYPE, 2:NAME, 3:DIST, 4:SOWING, 5:NOTES, 6:AUTHOR_NAME, 7:EMAIL, 8:PHONE
                if (parts.length >= 7) {
                    PlantVarietySheet p = new PlantVarietySheet();
                    p.setId(Integer.parseInt(parts[0]));
                    p.setType(PlantType.valueOf(parts[1]));
                    p.setVarietyName(parts[2]);

                    String distStr = parts[3].replace(",", ".");
                    p.setMinDistance(Double.parseDouble(distStr));

                    p.setSowingPeriod(parts[4]);
                    p.setNotes(parts[5]);

                    // Reconstruct Author
                    User author = new User();
                    author.setFirstName(parts[6]);
                    author.setLastName("");

                    if (parts.length > 7) author.setEmail(parts[7]);

                    if (parts.length > 8) author.setPhone(parts[8]);

                    p.setAuthor(author);
                    list.add(p);
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Error reading from plant storage file.", e);
        } catch (Exception e) {
            throw new DataPersistenceException("Data Corruption in plants.txt.", e);
        }
        return list;
    }

    private int generateNewId() {
        List<PlantVarietySheet> all = loadAllFromFile();
        if (all.isEmpty()) return 1;
        int maxId = 0;
        for(PlantVarietySheet p : all) {
            if(p.getId() > maxId) maxId = p.getId();
        }
        return maxId + 1;
    }

    private String cleanText(String input) {
        if (input == null) return "";
        return input.replace(SEPARATOR, " ").trim();
    }
}