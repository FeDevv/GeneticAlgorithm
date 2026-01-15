package org.agroplanner.persistence.implementations.domainsystem;

import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.domainsystem.model.DomainType;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * JDBC implementation for Domain persistence.
 * <p>
 * <strong>Storage Strategy:</strong>
 * Since domains have variable parameters based on their type, this DAO employs a
 * serialization strategy where the parameter map is flattened into a single
 * formatted string (e.g., "key=value;key2=value2") and stored in a VARCHAR column.
 * </p>
 */
public class SqlDomainDAO implements DomainDAOContract {

    @Override
    public void initStorage() {
        String sql = "CREATE TABLE IF NOT EXISTS domains (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "type VARCHAR(50) NOT NULL, " +
                "params VARCHAR(512) NOT NULL)";

        Connection conn = DBConnection.getInstance().getConnection();

        try (Statement stmt = conn.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            throw new DataPersistenceException("Init domains failed", e);
        }
    }

    /**
     * Persists the domain definition.
     * <p>
     * Note: The connection is retrieved from the Singleton and is <strong>not</strong> closed
     * here, allowing the pool/manager to keep it alive for subsequent operations.
     * </p>
     *
     * @param def The domain definition DTO.
     * @return The auto-generated primary key (ID).
     */
    @Override
    public int save(DomainDefinition def) {
        String sql = "INSERT INTO domains (type, params) VALUES (?, ?)";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setString(1, def.getType().name());
            pstmt.setString(2, mapToString(def.getParameters()));

            pstmt.executeUpdate();

            try (ResultSet rs = pstmt.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }

        } catch (SQLException e) {
            throw new DataPersistenceException("Save domain failed", e);
        }

        return -1;
    }

    @Override
    public DomainDefinition load(int id) {
        String sql = "SELECT * FROM domains WHERE id = ?";

        Connection conn = DBConnection.getInstance().getConnection();

        try (PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, id);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    DomainType type = DomainType.valueOf(rs.getString("type"));
                    Map<String, Double> params = stringToMap(rs.getString("params"));
                    return new DomainDefinition(type, params);
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Load domain failed", e);
        }
        return null;
    }

    // --- SERIALIZATION HELPERS ---

    private String mapToString(Map<String, Double> map) {
        return map.entrySet().stream()
                .map(e -> e.getKey() + "=" + e.getValue())
                .collect(Collectors.joining(";"));
    }

    private Map<String, Double> stringToMap(String s) {
        Map<String, Double> map = new HashMap<>();
        if (s == null || s.isEmpty()) return map;

        String[] pairs = s.split(";");
        for (String pair : pairs) {
            String[] kv = pair.split("=");
            if (kv.length == 2) {
                try {
                    map.put(kv[0], Double.parseDouble(kv[1]));
                } catch (NumberFormatException ignored) {
                    // Ignore malformed numbers to prevent crash during read
                }
            }
        }
        return map;
    }
}
