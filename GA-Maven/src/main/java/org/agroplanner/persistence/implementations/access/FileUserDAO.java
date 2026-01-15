package org.agroplanner.persistence.implementations.access;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * File-based implementation of the {@link UserDAOContract}.
 * <p>
 * Persists user data to a flat text file using a custom delimiter.
 * Suitable for environments where a database engine is not available.
 * </p>
 */
public class FileUserDAO implements UserDAOContract {

    private static final String FILE_PATH = "data/users.txt";
    private static final String SEPARATOR = ";";

    /**
     * Ensures the data directory and storage file exist.
     * <p>
     * If the file is created for the first time, a default administrator account is injected.
     * </p>
     *
     * @return {@code true} if the storage is ready for use.
     * @throws DataPersistenceException If I/O operations fail.
     */
    @Override
    public boolean initStorage() {
        try {
            Path path = Paths.get("data");
            if (!Files.exists(path)) {
                Files.createDirectories(path);
            }
            File file = new File(FILE_PATH);
            if (!file.exists()) {
                boolean created = file.createNewFile();
                if (created) {
                    // AGGIORNATO CON BUILDER
                    User admin = new User.Builder("admin", "admin", Role.ADMINISTRATOR)
                            .firstName("System")
                            .lastName("Admin")
                            .email("admin@agro.it")
                            .phone("0000")
                            .build();
                    create(admin);
                    return true;
                }
            }
            return false;
        } catch (IOException e) {
            throw new DataPersistenceException("Critical Error: Unable to initialize User File Storage.", e);
        }
    }

    @Override
    public boolean create(User user) {
        int newId = generateNewId();
        user.setId(newId);

        StringBuilder sb = new StringBuilder();
        sb.append(user.getId()).append(SEPARATOR);
        sb.append(clean(user.getUsername())).append(SEPARATOR);
        sb.append(clean(user.getPassword())).append(SEPARATOR);
        sb.append(user.getRole().name()).append(SEPARATOR);
        sb.append(clean(user.getFirstName())).append(SEPARATOR);
        sb.append(clean(user.getLastName())).append(SEPARATOR);
        sb.append(clean(user.getEmail())).append(SEPARATOR);
        sb.append(clean(user.getPhone()));

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(sb.toString());
            writer.newLine();
            return true;
        } catch (IOException e) {
            throw new DataPersistenceException("Error writing user to file storage.", e);
        }
    }

    @Override
    public User findByUsername(String username) {
        File file = new File(FILE_PATH);
        if (!file.exists()) return null;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.trim().isEmpty()) continue;

                String[] parts = line.split(SEPARATOR, -1);

                // Basic structural validation
                if (parts.length >= 4) {
                    String storedUsername = parts[1].trim();
                    if (storedUsername.equalsIgnoreCase(username.trim())) {
                        return mapLineToUser(parts);
                    }
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Error reading from user storage.", e);
        }
        return null;
    }

    // --- PRIVATE HELPERS ---

    private User mapLineToUser(String[] parts) {
        try {
            int id = Integer.parseInt(parts[0]);
            String username = parts[1];
            String password = parts[2];
            Role role = Role.valueOf(parts[3]);

            // Handling optional fields that might be empty strings in CSV
            String fname = parts.length > 4 ? parts[4] : "";
            String lname = parts.length > 5 ? parts[5] : "";
            String email = parts.length > 6 ? parts[6] : "";
            String phone = parts.length > 7 ? parts[7] : "";

            User u = new User.Builder(username, password, role)
                    .firstName(fname)
                    .lastName(lname)
                    .email(email)
                    .phone(phone)
                    .build();

            u.setId(id);
            return u;

        } catch (Exception e) {
            throw new DataPersistenceException("Data Corruption Error: Unable to parse user record.", e);
        }
    }

    private int generateNewId() {
        int maxId = 0;
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (!line.trim().isEmpty()) {
                    String[] parts = line.split(SEPARATOR);
                    if (parts.length > 0) {
                        try {
                            int id = Integer.parseInt(parts[0]);
                            if (id > maxId) maxId = id;
                        } catch (NumberFormatException _) {}
                    }
                }
            }
        } catch (IOException e) {
            throw new DataPersistenceException("Unable to generate new ID: Storage access failed.", e);
        }
        return maxId + 1;
    }

    /**
     * Sanitizes input strings to ensure they do not contain the file separator.
     *
     * @param text The input text.
     * @return The sanitized string.
     */
    private String clean(String text) {
        if (text == null) return "";
        return text.replace(SEPARATOR, "");
    }
}
