package org.agroplanner.access.dao;

import org.agroplanner.access.model.User;

/**
 * Defines the contract for User persistence operations.
 * <p>
 * This interface decouples the high-level authentication service from the low-level
 * storage details (SQL, FileSystem, or Memory).
 * </p>
 */
public interface UserDAOContract {

    /**
     * Initializes the underlying storage structure (e.g., creates tables or files).
     * @return {@code true} if initialization was successful.
     */
    boolean initStorage();

    /**
     * Persists a new user entity to the storage.
     * @param user The user object to save.
     * @return {@code true} if the operation succeeded, {@code false} otherwise.
     */
    boolean create(User user);

    /**
     * Retrieves a user entity by its unique username.
     * @param username The identifier to search for.
     * @return The {@link User} object if found, or {@code null} if no match exists.
     */
    User findByUsername(String username);
}
