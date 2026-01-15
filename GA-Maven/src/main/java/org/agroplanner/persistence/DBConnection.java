package org.agroplanner.persistence;

import org.agroplanner.boot.model.PersistenceType;
import org.agroplanner.shared.exceptions.DataPersistenceException;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

/**
 * Singleton wrapper for the JDBC connection to the embedded H2 database.
 * <p>
 * Implements the Initialization-on-demand holder idiom (Bill Pugh Singleton)
 * to ensure thread safety and lazy initialization without synchronization overhead.
 * </p>
 */
public class DBConnection {

    private Connection activeConnection = null;
    private String connectionUrl = null;

    private static String dbUser;
    private static String dbPassword;
    private static String dbUrl;

    static {
        // search in classpath (src/main/res)
        try (InputStream input = DBConnection.class.getClassLoader().getResourceAsStream("db.properties")) {

            Properties prop = new Properties();

            if (input == null) {
                // default on error
                System.err.println("WARNING: db.properties non trovato in resources. Uso credenziali di default.");
                dbUser = "sa";
                dbPassword = "ISPW2025_2026";
                dbUrl = "jdbc:h2:./agri_db";
            } else {
                // normal behaviour
                prop.load(input);
                dbUser = prop.getProperty("db.user");
                dbPassword = prop.getProperty("db.password");
                dbUrl = prop.getProperty("db.url");
            }

        } catch (IOException e) {
            throw new DataPersistenceException("Errore critico leggendo db.properties", e);
        }
    }

    private DBConnection() {
        // Enforce non-instantiability
    }

    // --- BILL PUGH SINGLETON ---
    private static class LazyHolder {
        private static final DBConnection INSTANCE = new DBConnection();
    }

    public static DBConnection getInstance() {
        return LazyHolder.INSTANCE;
    }
    // ---------------------------

    /**
     * Retrieves the active JDBC connection.
     * <p>
     * If the connection was closed or dropped, this method attempts to re-establish it.
     * </p>
     *
     * @return A valid {@link Connection} object.
     * @throws DataPersistenceException If the SQL engine is disabled or unreachable.
     */
    public synchronized Connection getConnection() {
        if (connectionUrl == null) {
            throw new DataPersistenceException("CRITICAL: DB Access requested but SQL engine is DISABLED.");
        }

        try {
            if (activeConnection == null || activeConnection.isClosed()) {
                activeConnection = DriverManager.getConnection(connectionUrl, dbUser, dbPassword);
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Unable to establish connection to H2 Database.", e);
        }

        return activeConnection;
    }

    /**
     * Configures the internal SQL engine state based on the persistence requirement.
     * <p>
     * This method does not return status strings. If configuration fails, an exception is thrown.
     * </p>
     *
     * @param type The selected persistence mode.
     */
    public void configure(PersistenceType type) {
        // 1. Reset previous connection if exists
        this.connectionUrl = null;
        try {
            if (this.activeConnection != null && !this.activeConnection.isClosed()) {
                this.activeConnection.close();
            }
            this.activeConnection = null;
        } catch (SQLException _) {
            // Quietly suppress closure errors during reconfiguration
        }

        // 2. Set URL only if Database mode is requested
        if (type == PersistenceType.DATABASE) {
            this.connectionUrl = dbUrl;
        }
    }

    /**
     * Closes the active database connection gracefully.
     */
    public void shutdown() {
        try {
            if (activeConnection != null && !activeConnection.isClosed()) {
                activeConnection.close();
            }
        } catch (SQLException e) {
            throw new DataPersistenceException("Failed to close database connection gracefully.", e);
        }
    }
}
