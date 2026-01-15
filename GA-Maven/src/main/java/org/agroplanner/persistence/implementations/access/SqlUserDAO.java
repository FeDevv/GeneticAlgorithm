package org.agroplanner.persistence.implementations.access;

import org.agroplanner.access.dao.UserDAOContract;
import org.agroplanner.access.model.Role;
import org.agroplanner.access.model.User;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.sql.*;

/**
 * JDBC implementation of the {@link UserDAOContract}.
 * <p>
 * Manages user persistence in a relational database (H2).
 * Handles schema initialization, parameterized queries to prevent SQL injection,
 * and mapping between {@link ResultSet} rows and domain objects.
 * </p>
 */
public class SqlUserDAO implements UserDAOContract {

    /**
     * initializes the 'users' table if it does not exist and seeds the default administrator.
     *
     * @return {@code true} if the initialization (and optional seeding) was successful.
     * @throws DataPersistenceException If the DDL execution fails.
     */
    @Override
    public boolean initStorage() {
        String sql = "CREATE TABLE IF NOT EXISTS users (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "username VARCHAR(50) NOT NULL UNIQUE, " +
                "password VARCHAR(255) NOT NULL, " +
                "role VARCHAR(20) NOT NULL, " +
                "first_name VARCHAR(50), " +
                "last_name VARCHAR(50), " +
                "email VARCHAR(100), " +
                "phone VARCHAR(20))";

        Connection conn = DBConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {

            stmt.execute(sql);
            return createDefaultAdminIfMissing();

        } catch (SQLException e) {
            throw new DataPersistenceException("Critical Error: Failed to initialize 'users' database table.", e);
        }
    }

    /**
     * Inserts a new user record into the database.
     *
     * @param user The user entity to persist.
     * @return {@code true} if the insert was successful; {@code false} if a duplicate key violation occurred.
     * @throws DataPersistenceException If a SQL error other than constraint violation occurs.
     */
    @Override
    public boolean create(User user) {
        String sql = "INSERT INTO users (username, password, role, first_name, last_name, email, phone) VALUES (?, ?, ?, ?, ?, ?, ?)";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, user.getUsername());
            pstmt.setString(2, user.getPassword());
            pstmt.setString(3, user.getRole().name());
            pstmt.setString(4, user.getFirstName());
            pstmt.setString(5, user.getLastName());
            pstmt.setString(6, user.getEmail());
            pstmt.setString(7, user.getPhone());

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) user.setId(rs.getInt(1));
                }
                return true;
            }
        } catch (SQLException e) {
            // Handle Duplicate Key (H2 error code 23505)
            if ("23505".equals(e.getSQLState())) {
                return false;
            }
            throw new DataPersistenceException("Database Error: Failed to create new user record.", e);
        }
        return false;
    }

    @Override
    public User findByUsername(String username) {
        String sql = "SELECT id, username, password, role, first_name, last_name, email, phone FROM users WHERE username = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, username);

            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToUser(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Database Error: Failed to search user by username: " + username, e);
        }
        return null;
    }

    private boolean createDefaultAdminIfMissing() {
        String countSql = "SELECT COUNT(*) FROM users";

        Connection conn = DBConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(countSql)) {

            if (rs.next() && rs.getInt(1) == 0) {
                // AGGIORNATO CON BUILDER
                User admin = new User.Builder("admin", "admin", Role.ADMINISTRATOR)
                        .firstName("System")
                        .lastName("Admin")
                        .email("admin@agro.org")
                        .phone("0000")
                        .build();
                return create(admin);
            }
        } catch (Exception e) {
            throw new DataPersistenceException("Critical Error: Failed to initialize default administrator account.", e);
        }
        return false;
    }

    private User mapRowToUser(ResultSet rs) throws SQLException {

        Role role;
        try {
            role = Role.valueOf(rs.getString("role"));
        } catch (IllegalArgumentException | NullPointerException _) {
            // Fallback to safe default if DB contains invalid enum value
            role = Role.USER;
        }

        User user = new User.Builder(
                rs.getString("username"),
                rs.getString("password"),
                role
        )
                .firstName(rs.getString("first_name"))
                .lastName(rs.getString("last_name"))
                .email(rs.getString("email"))
                .phone(rs.getString("phone"))
                .build();
        user.setId(rs.getInt("id"));

        return user;
    }
}
