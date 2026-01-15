package org.agroplanner.persistence.implementations.domainsystem;

import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * File-based implementation for Domain persistence.
 * <p>
 * Stores definitions in a flat text file using a custom delimiter schema.
 * Ensures locale-independent serialization (dot as decimal separator) to maintain data portability.
 * </p>
 */
public class FileDomainDAO implements DomainDAOContract {

    private static final String FILE_PATH = "data/domains.txt";
    private static final String FIELD_SEPARATOR = ";";
    private static final String PARAM_SEPARATOR = ",";
    private static final String KV_SEPARATOR = "=";

    @Override
    public void initStorage() {
        try {
            Path directory = Paths.get("data");
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            Path filePath = Paths.get(FILE_PATH);
            if (!Files.exists(filePath)) {
                Files.createFile(filePath);
            }

        } catch (IOException e) {
            throw new DataPersistenceException("Critical Error: Unable to initialize Domain File Storage.", e);
        }
    }

    @Override
    public int save(DomainDefinition def) {
        int newId = generateNewId();

        // Format: ID;TYPE;key=val,key2=val2
        StringBuilder sb = new StringBuilder();
        sb.append(newId).append(FIELD_SEPARATOR);
        sb.append(def.getType().name()).append(FIELD_SEPARATOR);
        sb.append(mapToString(def.getParameters()));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(sb.toString());
            writer.newLine();
            return newId;
        } catch (IOException e) {
            throw new DataPersistenceException("Error writing domain to file.", e);
        }
    }

    @Override
    public DomainDefinition load(int id) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(FIELD_SEPARATOR);
                if (parts.length >= 3) {
                    int currentId = Integer.parseInt(parts[0]);

                    if (currentId == id) {
                        DomainType type = DomainType.valueOf(parts[1]);
                        Map<String, Double> params = stringToMap(parts[2]);

                        return new DomainDefinition(type, params);
                    }
                }
            }
        } catch (IOException | IllegalArgumentException e) {
            throw new DataPersistenceException("Error reading domain definition from file.", e);
        }

        return null;
    }

    // --- SERIALIZATION HELPERS ---

    private String mapToString(Map<String, Double> map) {
        // Enforce US Locale to ensure '5.5' is not written as '5,5', which would break the comma separator.
        return map.entrySet().stream()
                .map(e -> String.format(Locale.US, "%s%s%.2f", e.getKey(), KV_SEPARATOR, e.getValue()))
                .collect(Collectors.joining(PARAM_SEPARATOR));
    }

    private Map<String, Double> stringToMap(String s) {
        Map<String, Double> map = new HashMap<>();
        if (s == null || s.isEmpty()) return map;

        String[] pairs = s.split(PARAM_SEPARATOR);
        for (String pair : pairs) {
            String[] kv = pair.split(KV_SEPARATOR);
            if (kv.length == 2) {
                try {
                    map.put(kv[0], Double.parseDouble(kv[1]));
                } catch (NumberFormatException _) {
                    // Ignore malformed values
                }
            }
        }
        return map;
    }

    private int generateNewId() {
        int maxId = 0;
        File file = new File(FILE_PATH);
        if (!file.exists()) return 1;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(FIELD_SEPARATOR);
                    if (parts.length > 0) {
                        try {
                            int id = Integer.parseInt(parts[0]);
                            if (id > maxId) maxId = id;
                        } catch (NumberFormatException _) {}
                    }
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Error generating ID for domain.", e);
        }
        return maxId + 1;
    }
}
