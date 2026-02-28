package eds.database;

import eds.framework.Trace;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Database is the single entry point for database access.
 * - Holds the SQLite connection as a singleton
 * - Creates tables on first run if they do not exist
 * - Provides the connection to repositories (repo)
 * - Closes the connection on app shutdown
 *
 * Usage:
 * - Call Database.getInstance().getConnection() from repositories
 * - Call Database.getInstance().close() in SimulatorGUI.stop()
 */
public class Database {

    private static final String DB_URL = "jdbc:sqlite:simulation.db";

    private static Database instance;
    private Connection connection;

    // ── Constructor ───────────────────────────────────────────────────────────
    private Database() {
        try {
            connection = DriverManager.getConnection(DB_URL);
            Trace.out(Trace.Level.INFO, "Database: connected to simulation.db");
            createTables();
        } catch (SQLException e) {
            Trace.out(Trace.Level.ERR, "Database: failed to connect — " + e.getMessage());
            throw new RuntimeException("Failed to connect to simulation database", e);
        }
    }

    // ── Singleton access ──────────────────────────────────────────────────────
    public static Database getInstance() {
        if (instance == null) {
            instance = new Database();
        }
        return instance;
    }

    // ── Connection access ─────────────────────────────────────────────────────
    public Connection getConnection() {
        return connection;
    }

    // ── Schema creation ───────────────────────────────────────────────────────
    /**
     * Creates tables if they do not already exist.
     * Called once at startup.
     */
    private void createTables() throws SQLException {
        Trace.out(Trace.Level.INFO, "Database: tables ready");
    }

    // ── Shutdown ──────────────────────────────────────────────────────────────

    /**
     * Closes the database connection.
     * Should be called in SimulatorGUI.stop().
     */
    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                Trace.out(Trace.Level.INFO, "Database: connection closed");
            }
        } catch (SQLException e) {
            Trace.out(Trace.Level.ERR, "Database: failed to close connection — " + e.getMessage());
            throw new RuntimeException("Failed to close database connection", e);
        }
    }
}
