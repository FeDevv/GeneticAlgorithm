package org.agroplanner.access.controllers;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.access.model.CredentialsDTO;
import org.agroplanner.access.model.User;
import org.agroplanner.shared.exceptions.DataPersistenceException;
import org.agroplanner.shared.exceptions.DuplicateUserException;
import org.agroplanner.shared.exceptions.InvalidInputException;

/**
 * Service component handling the business logic for Authentication and Registration.
 * <p>
 * Responsible for enforcing domain rules (e.g., password strength, username uniqueness)
 * and interacting with the persistence layer via the DAO contract.
 * </p>
 */
public class AccessService {

    private final UserDAOContract userDAO;

    public AccessService(UserDAOContract userDAO) {
        this.userDAO = userDAO;
    }

    /**
     * Authenticates a user against stored credentials.
     * <p>
     * <strong>Security Note:</strong> Current implementation performs a direct comparison
     * of plaintext passwords.
     * </p>
     *
     * @param username The provided username.
     * @param password The provided password.
     * @return The authenticated {@link User} object, or {@code null} if credentials do not match.
     */
    public User login(String username, String password) {
        if (username == null || password == null) return null;

        User user = userDAO.findByUsername(username);
        // password is not being crypetd
        if (user != null && user.getPassword().equals(password)) {
            return user;
        }
        return null;
    }

    /**
     * Registers a new user in the system.
     * <p>
     * This process involves three steps:
     * <ol>
     * <li>Validation of input format constraints.</li>
     * <li>Verification of username uniqueness.</li>
     * <li>Persistence of the new entity.</li>
     * </ol>
     *
     * @param dto The data transfer object containing registration details.
     * @throws InvalidInputException    If the input data violates format rules.
     * @throws DuplicateUserException   If the username is already occupied.
     * @throws DataPersistenceException If the storage operation fails.
     */
    public void register(CredentialsDTO dto) throws InvalidInputException, DuplicateUserException {

        // 1. Business Validation
        validateRegistrationData(dto);

        // 2. Uniqueness Check
        if (userDAO.findByUsername(dto.getUsername()) != null) {
            throw new DuplicateUserException("Username '" + dto.getUsername() + "' already taken.");
        }

        // 3. Entity Creation (Builder)
        User newUser = new User.Builder(dto.getUsername(), dto.getPassword(), dto.getRequestedRole())
                .firstName(dto.getFirstName())
                .lastName(dto.getLastName())
                .email(dto.getEmail())
                .phone(dto.getPhone())
                .build();

        // 4. Persistence
        boolean success = userDAO.create(newUser);
        if (!success) {
            throw new DataPersistenceException("Database creation failed.");
        }
    }

    /**
     * Validates the structural integrity of registration data.
     *
     * @param dto The data to validate.
     * @throws InvalidInputException If any field fails the validation criteria.
     */
    private void validateRegistrationData(CredentialsDTO dto) throws InvalidInputException {
        if (dto.getUsername() == null || dto.getUsername().length() < 3)
            throw new InvalidInputException("Username must be at least 3 chars.");
        if (dto.getPassword() == null || dto.getPassword().length() < 4)
            throw new InvalidInputException("Password must be at least 4 chars.");
        if (dto.getRequestedRole() == null)
            throw new InvalidInputException("Role is required.");
    }
}
