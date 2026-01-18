package org.agroplanner.persistence.implementations.inventory;

import org.agroplanner.access.model.User;
import org.agroplanner.inventory.dao.PlantVarietyDAOContract;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.inventory.model.PlantVarietySheet;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * JDBC implementation of the Plant Inventory storage.
 * <p>
 * Handles the relational mapping between {@code plant_varieties} and {@code users}.
 * It enforces referential integrity via Foreign Keys and optimizes read operations
 * using a standardized JOIN query to eager-fetch author details.
 * </p>
 */
public class SqlPlantVarietyDAO implements PlantVarietyDAOContract {

    /**
     * Shared SQL projection to ensure Author data is always eagerly fetched.
     * Prevents the "N+1 Selects" problem when loading lists of plants.
     */
    private static final String BASE_QUERY =
            "SELECT pv.*, u.first_name, u.last_name, u.email, u.phone " +
                    "FROM plant_varieties pv " +
                    "JOIN users u ON pv.author_id = u.id ";

    @Override
    public void initStorage() {
        String sql = "CREATE TABLE IF NOT EXISTS plant_varieties (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "plant_type VARCHAR(50) NOT NULL, " +
                "variety_name VARCHAR(100) NOT NULL, " +
                "min_distance DOUBLE NOT NULL, " +
                "sowing_period VARCHAR(100), " +
                "notes VARCHAR(255), " +
                "author_id INT NOT NULL, " +
                "FOREIGN KEY (author_id) REFERENCES users(id) ON DELETE CASCADE" +
                ")";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DataPersistenceException("Critical Error: Unable to initialize table 'plant_varieties'.", e);
        }
    }

    @Override
    public boolean save(PlantVarietySheet sheet) {
        // Integrity Check
        if (sheet.getAuthor() == null) {
            throw new DataPersistenceException("Constraint Violation: Cannot save a plant without an Author.");
        }

        String sql = "INSERT INTO plant_varieties " +
                "(plant_type, variety_name, min_distance, sowing_period, notes, author_id) " +
                "VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, sheet.getType().name());
            pstmt.setString(2, sheet.getVarietyName());
            pstmt.setDouble(3, sheet.getMinDistance());
            pstmt.setString(4, sheet.getSowingPeriod());
            pstmt.setString(5, sheet.getNotes());
            pstmt.setInt(6, sheet.getAuthor().getId());

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet rs = pstmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        sheet.setId(rs.getInt(1));
                    }
                }
                return true;
            }
            return false;

        } catch (SQLException e) {
            throw new DataPersistenceException("Database Error: Failed to save plant variety '" + sheet.getVarietyName() + "'.", e);
        }
    }

    @Override
    public List<PlantVarietySheet> findByType(PlantType type) {
        String sql = BASE_QUERY + "WHERE pv.plant_type = ?";
        List<PlantVarietySheet> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, type.name());

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPlant(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Database Error: Search by type failed.", e);
        }
        return list;
    }

    @Override
    public List<PlantVarietySheet> findAll() {
        String sql = BASE_QUERY; // Fetches all records with joined authors
        List<PlantVarietySheet> list = new ArrayList<>();

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                list.add(mapRowToPlant(rs));
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Database Error: Failed to load global catalog.", e);
        }
        return list;
    }

    @Override
    public PlantVarietySheet findById(int id) {
        String sql = BASE_QUERY + "WHERE pv.id = ?";

        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    return mapRowToPlant(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Error finding plant by ID", e);
        }
        return null;
    }

    @Override
    public List<PlantVarietySheet> findAllByIds(Set<Integer> ids) {
        if (ids == null || ids.isEmpty()) return new ArrayList<>();

        // Construct dynamic query: SELECT ... WHERE id IN (?, ?, ?)
        StringBuilder sql = new StringBuilder(BASE_QUERY + "WHERE pv.id IN (");
        for (int i = 0; i < ids.size(); i++) {
            sql.append(i == 0 ? "?" : ", ?");
        }
        sql.append(")");

        List<PlantVarietySheet> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql.toString())) {

            int index = 1;
            for (Integer id : ids) {
                pstmt.setInt(index++, id);
            }

            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRowToPlant(rs));
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Error batch loading plants", e);
        }
        return list;
    }

    // --- HELPER ---

    private PlantVarietySheet mapRowToPlant(ResultSet rs) throws SQLException {
        PlantVarietySheet p = new PlantVarietySheet();
        p.setId(rs.getInt("id"));

        try {
            p.setType(PlantType.valueOf(rs.getString("plant_type")));
        } catch (IllegalArgumentException _) {
            p.setType(PlantType.GENERIC);
        }

        p.setVarietyName(rs.getString("variety_name"));
        p.setMinDistance(rs.getDouble("min_distance"));
        p.setSowingPeriod(rs.getString("sowing_period"));
        p.setNotes(rs.getString("notes"));

        // Reconstruct Author Object from JOIN columns
        User author = new User();
        author.setId(rs.getInt("author_id")); // Abbiamo anche l'ID grazie a pv.*
        author.setFirstName(rs.getString("first_name"));
        author.setLastName(rs.getString("last_name"));
        author.setEmail(rs.getString("email"));
        author.setPhone(rs.getString("phone"));

        p.setAuthor(author);

        return p;
    }
}
