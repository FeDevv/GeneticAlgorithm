package org.agroplanner.persistence.implementations.access;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;

import java.util.HashMap;
import java.util.Map;

/**
 * Volatile implementation of the {@link UserDAOContract}.
 * <p>
 * Stores user data in an in-memory {@link HashMap}.
 * Data is lost when the application terminates.
 * </p>
 */
public class MemoryUserDAO implements UserDAOContract {

    private final Map<String, User> memoryDb = new HashMap<>();

    @Override
    public boolean initStorage() {
        memoryDb.clear();
        create(User.createGuestUser());

        User admin = new User.Builder("admin", "admin", Role.ADMINISTRATOR)
                .firstName("System")
                .lastName("Admin")
                .email("admin@agro.it")
                .phone("0000")
                .build();

        create(admin);
        return true;
    }

    @Override
    public boolean create(User user) {
        if (memoryDb.containsKey(user.getUsername())) return false;

        user.setId(memoryDb.size() + 1);
        memoryDb.put(user.getUsername(), user);
        return true;
    }

    @Override
    public User findByUsername(String username) {
        return memoryDb.get(username);
    }
}
