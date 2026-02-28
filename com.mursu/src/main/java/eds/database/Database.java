package eds.database;

import eds.framework.Trace;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

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
        try (Statement stmt = connection.createStatement()) {

            // statistics: one row per simulation run
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS statistics (
                            id                      INTEGER PRIMARY KEY AUTOINCREMENT,
                            run_timestamp           TEXT    NOT NULL,
                            run_name                TEXT,

                            -- configuration parameters
                            seed                    INTEGER NOT NULL,
                            mean_validation         REAL    NOT NULL,
                            mean_market             REAL    NOT NULL,
                            mean_limit              REAL    NOT NULL,
                            mean_execution          REAL    NOT NULL,
                            mean_arrival            REAL    NOT NULL,
                            simulation_time         REAL    NOT NULL,

                            -- order statistics
                            total_orders            INTEGER,
                            total_trades            INTEGER,
                            filled_orders           INTEGER,
                            cancelled_orders        INTEGER,
                            remaining_orders        INTEGER,

                            -- price statistics
                            vwap                    REAL,
                            avg_mid_price           REAL,
                            min_price               REAL,
                            max_price               REAL,

                            -- market quality
                            avg_spread              REAL,
                            avg_latency             REAL,
                            throughput              REAL,
                            fill_rate               REAL,

                            -- service point metrics
                            utilization_validation  REAL,
                            utilization_market      REAL,
                            utilization_limit       REAL,
                            utilization_execution   REAL,
                            avg_queue_validation    REAL,
                            avg_queue_market        REAL,
                            avg_queue_limit         REAL,
                            avg_queue_execution     REAL
                        )
                    """);

            // trade: one row per executed trade, linked to a run
            stmt.execute("""
                        CREATE TABLE IF NOT EXISTS trade (
                            id              TEXT    PRIMARY KEY,
                            run_id          INTEGER NOT NULL,
                            buy_order_id    TEXT    NOT NULL,
                            sell_order_id   TEXT    NOT NULL,
                            price           REAL    NOT NULL,
                            share_size      INTEGER NOT NULL,
                            conclusion_time REAL    NOT NULL,
                            FOREIGN KEY (run_id) REFERENCES statistics(id)
                        )
                    """);

            Trace.out(Trace.Level.INFO, "Database: tables ready");
        }
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
