package org.agroplanner.persistence.implementations.gasystem;

import org.agroplanner.access.model.User;
import org.agroplanner.domainsystem.dao.DomainDAOContract;
import org.agroplanner.domainsystem.model.DomainDefinition;
import org.agroplanner.gasystem.dao.SolutionDAOContract;
import org.agroplanner.gasystem.model.Individual;
import org.agroplanner.gasystem.model.LoadedSession;
import org.agroplanner.gasystem.model.Point;
import org.agroplanner.gasystem.model.SolutionMetadata;
import org.agroplanner.inventory.model.PlantType;
import org.agroplanner.persistence.DBConnection;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * JDBC implementation for storing Evolutionary Solutions.
 * <p>
 * Implements a complex transaction spanning multiple tables:
 * <ol>
 * <li>{@code domains}: Stores the geometric context (Width, Height, Type).</li>
 * <li>{@code solutions}: Stores session metadata (User, Title, Fitness).</li>
 * <li>{@code solution_items}: Stores the phenotype (Individual Plant coordinates).</li>
 * </ol>
 * Uses {@link Connection#rollback()} to ensure atomicity: either the full session is saved, or nothing is.
 * </p>
 */
public class SqlSolutionDAO implements SolutionDAOContract {

    private final DomainDAOContract domainDAO;
    private static final Logger LOGGER = Logger.getLogger(SqlSolutionDAO.class.getName());

    /**
     * @param domainDAO Used to delegate the persistence of geometric data.
     */
    public SqlSolutionDAO(DomainDAOContract domainDAO) {
        this.domainDAO = domainDAO;
    }

    @Override
    public void initStorage() {
        // 1. Dependency: Domains table must exist first
        domainDAO.initStorage();

        // 2. Solutions Tables
        String sqlHeader = "CREATE TABLE IF NOT EXISTS solutions (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "user_id INT NOT NULL, " +
                "domain_id INT NOT NULL, " +
                "title VARCHAR(100) NOT NULL, " +
                "fitness DOUBLE NOT NULL, " +
                "total_plants INT NOT NULL, " +
                "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, " +
                "FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE, " +
                "FOREIGN KEY (domain_id) REFERENCES domains(id))";

        String sqlItems = "CREATE TABLE IF NOT EXISTS solution_items (" +
                "id INT AUTO_INCREMENT PRIMARY KEY, " +
                "solution_id INT NOT NULL, " +
                "variety_id INT NOT NULL, " +
                "variety_name VARCHAR(100), " +
                "plant_type VARCHAR(50), " +
                "coord_x DOUBLE NOT NULL, " +
                "coord_y DOUBLE NOT NULL, " +
                "radius DOUBLE NOT NULL, " +
                "FOREIGN KEY (solution_id) REFERENCES solutions(id) ON DELETE CASCADE)";

        try (Connection conn = DBConnection.getInstance().getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute(sqlHeader);
            stmt.execute(sqlItems);
        } catch (SQLException e) {
            throw new DataPersistenceException("Critical: Unable to init solution tables.", e);
        }
    }

    @Override
    public boolean saveSolution(Individual solution, User owner, String title, DomainDefinition domainDef) {

        // Step 1: Persist Context (Domain)
        // Note: domainDAO handles its own connection scope internally.
        int domainId = domainDAO.save(domainDef);

        if (domainId == -1) {
            throw new DataPersistenceException("Failed to save Domain configuration. Aborting solution save.");
        }

        // Step 2: Persist Session (Transactional)
        Connection conn = DBConnection.getInstance().getConnection();

        String insertHeader = "INSERT INTO solutions (user_id, domain_id, title, fitness, total_plants) VALUES (?, ?, ?, ?, ?)";
        String insertItem = "INSERT INTO solution_items (solution_id, variety_id, variety_name, plant_type, coord_x, coord_y, radius) VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            // A. Start Transaction
            conn.setAutoCommit(false);

            int solutionId;

            // B. Header Insert
            try (PreparedStatement pstmt = conn.prepareStatement(insertHeader, Statement.RETURN_GENERATED_KEYS)) {
                pstmt.setInt(1, owner.getId());
                pstmt.setInt(2, domainId);
                pstmt.setString(3, title);
                pstmt.setDouble(4, solution.getFitness());
                pstmt.setInt(5, solution.getDimension());

                int affected = pstmt.executeUpdate();
                if (affected == 0) throw new SQLException("Saving header failed.");

                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) solutionId = generatedKeys.getInt(1);
                    else throw new SQLException("No ID obtained.");
                }
            }

            // C. Items Batch Insert
            try (PreparedStatement pstmt = conn.prepareStatement(insertItem)) {
                pstmt.setInt(1, solutionId);
                for (Point p : solution.getChromosomes()) {
                    pstmt.setInt(2, p.getVarietyId());
                    pstmt.setString(3, p.getVarietyName());
                    pstmt.setString(4, p.getType().name());
                    pstmt.setDouble(5, p.getX());
                    pstmt.setDouble(6, p.getY());
                    pstmt.setDouble(7, p.getRadius());
                    pstmt.addBatch();
                }
                pstmt.executeBatch();
            }

            // D. Commit
            conn.commit();
            return true;

        } catch (SQLException e) {
            // E. Rollback on failure
            try {
                conn.rollback();
            } catch (SQLException _) {
                // ignore errors during rollback
            }
            throw new DataPersistenceException("Transaction Failed: " + e.getMessage(), e);

        } finally {
            // F. Reset Connection State
            try {
                conn.setAutoCommit(true);
            } catch (SQLException e) {
                LOGGER.log(Level.WARNING, "Warning: Failed to reset auto-commit.", e);
            }

        }
    }

    @Override
    public Optional<LoadedSession> loadSolution(int solutionId) {
        String sqlItems = "SELECT coord_x, coord_y, radius, plant_type, variety_id, variety_name FROM solution_items WHERE solution_id = ?";
        String sqlHeader = "SELECT fitness, domain_id FROM solutions WHERE id = ?";

        List<Point> points = new ArrayList<>();
        double loadedFitness = 0;
        DomainDefinition loadedDomain = null;

        Connection conn = DBConnection.getInstance().getConnection();

        try {
            // 1. Load Header & Context Link
            int domainId = -1;
            try(PreparedStatement pstmt = conn.prepareStatement(sqlHeader)) {
                pstmt.setInt(1, solutionId);
                try(ResultSet rs = pstmt.executeQuery()) {
                    if(rs.next()) {
                        loadedFitness = rs.getDouble("fitness");
                        domainId = rs.getInt("domain_id");
                    } else {
                        return Optional.empty();
                    }
                }
            }

            // 2. Load Geometric Context
            loadedDomain = domainDAO.load(domainId);
            if (loadedDomain == null) {
                throw new DataPersistenceException("Consistency Error: Domain ID " + domainId + " not found for solution " + solutionId);
            }

            // 3. Load Phenotype (Points)
            try (PreparedStatement pstmt = conn.prepareStatement(sqlItems)) {
                pstmt.setInt(1, solutionId);
                try (ResultSet rs = pstmt.executeQuery()) {
                    while (rs.next()) {
                        points.add(new Point(
                                rs.getDouble("coord_x"),
                                rs.getDouble("coord_y"),
                                rs.getDouble("radius"),
                                safePlantType(rs.getString("plant_type")),
                                rs.getInt("variety_id"),
                                rs.getString("variety_name")
                        ));
                    }
                }
            }

            Individual ind = new Individual(points, loadedFitness);
            return Optional.of(new LoadedSession(ind, loadedDomain));

        } catch (SQLException e) {
            throw new DataPersistenceException("Error loading solution " + solutionId, e);
        }
    }

    @Override
    public List<SolutionMetadata> findByUser(User user) {
        String sql = "SELECT id, title, fitness, created_at FROM solutions WHERE user_id = ? ORDER BY created_at ASC";
        List<SolutionMetadata> list = new ArrayList<>();
        try (Connection conn = DBConnection.getInstance().getConnection();
            PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, user.getId());
            try (ResultSet rs = pstmt.executeQuery()) {
                while (rs.next()) {
                    list.add(new SolutionMetadata(
                            rs.getInt("id"),
                            rs.getString("title"),
                            rs.getTimestamp("created_at").toLocalDateTime(),
                            rs.getDouble("fitness")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Error listing solutions", e);
        }
        return list;
    }

    private PlantType safePlantType(String s) {
        try { return PlantType.valueOf(s); } catch (Exception _) { return PlantType.GENERIC; }
    }
}
