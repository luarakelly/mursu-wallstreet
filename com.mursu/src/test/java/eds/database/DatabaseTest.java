package eds.database;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.Order;

import static org.junit.jupiter.api.Assertions.*;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.SQLException;

/**
 * Tests for Database.java
 *
 * Covers:
 * - Singleton behaviour
 * - Connection validity
 * - Schema creation (tables exist with correct columns)
 * - Graceful close and reconnect behaviour
 */
@DisplayName("Database tests")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class DatabaseTest {

    // ── Singleton ─────────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("Should return the same instance on multiple calls")
    void shouldReturnSameInstance() {
        Database first = Database.getInstance();
        Database second = Database.getInstance();

        assertSame(first, second,
                "getInstance() should always return the same singleton instance");
    }

    // ── Connection ────────────────────────────────────────────────────────────

    @Test
    @Order(2)
    @DisplayName("Should provide a non-null connection")
    void shouldProvideNonNullConnection() {
        Connection connection = Database.getInstance().getConnection();

        assertNotNull(connection, "getConnection() should not return null");
    }

    @Test
    @Order(3)
    @DisplayName("Should provide an open connection")
    void shouldProvideOpenConnection() throws SQLException {
        Connection connection = Database.getInstance().getConnection();

        assertFalse(connection.isClosed(), "Connection should be open");
    }

    @Test
    @Order(4)
    @DisplayName("Connection should be valid and responsive")
    void connectionShouldBeValid() throws SQLException {
        Connection connection = Database.getInstance().getConnection();

        assertTrue(connection.isValid(2),
                "Connection should be valid with 2 second timeout");
    }

    // ── Schema: statistics table ──────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("Should create statistics table on startup")
    void shouldCreateSimulationRunTable() throws SQLException {
        assertTrue(tableExists("statistics"),
                "statistics table should exist after initialization");
    }

    @Test
    @Order(6)
    @DisplayName("statistics table should have id column as primary key")
    void simulationRunShouldHaveIdColumn() throws SQLException {
        assertTrue(columnExists("statistics", "id"),
                "statistics should have id column");
    }

    @Test
    @Order(7)
    @DisplayName("statistics table should have all configuration columns")
    void simulationRunShouldHaveConfigColumns() throws SQLException {
        assertTrue(columnExists("statistics", "seed"), "missing seed");
        assertTrue(columnExists("statistics", "mean_validation"), "missing mean_validation");
        assertTrue(columnExists("statistics", "mean_market"), "missing mean_market");
        assertTrue(columnExists("statistics", "mean_limit"), "missing mean_limit");
        assertTrue(columnExists("statistics", "mean_execution"), "missing mean_execution");
        assertTrue(columnExists("statistics", "mean_arrival"), "missing mean_arrival");
        assertTrue(columnExists("statistics", "simulation_time"), "missing simulation_time");
    }

    @Test
    @Order(8)
    @DisplayName("statistics table should have all order statistics columns")
    void simulationRunShouldHaveOrderStatColumns() throws SQLException {
        assertTrue(columnExists("statistics", "total_orders"), "missing total_orders");
        assertTrue(columnExists("statistics", "total_trades"), "missing total_trades");
        assertTrue(columnExists("statistics", "filled_orders"), "missing filled_orders");
        assertTrue(columnExists("statistics", "cancelled_orders"), "missing cancelled_orders");
        assertTrue(columnExists("statistics", "remaining_orders"), "missing remaining_orders");
    }

    @Test
    @Order(9)
    @DisplayName("statistics table should have all price statistics columns")
    void simulationRunShouldHavePriceColumns() throws SQLException {
        assertTrue(columnExists("statistics", "vwap"), "missing vwap");
        assertTrue(columnExists("statistics", "avg_mid_price"), "missing avg_mid_price");
        assertTrue(columnExists("statistics", "min_price"), "missing min_price");
        assertTrue(columnExists("statistics", "max_price"), "missing max_price");
    }

    @Test
    @Order(10)
    @DisplayName("statistics table should have all market quality columns")
    void simulationRunShouldHaveMarketQualityColumns() throws SQLException {
        assertTrue(columnExists("statistics", "avg_spread"), "missing avg_spread");
        assertTrue(columnExists("statistics", "avg_latency"), "missing avg_latency");
        assertTrue(columnExists("statistics", "throughput"), "missing throughput");
        assertTrue(columnExists("statistics", "fill_rate"), "missing fill_rate");
    }

    @Test
    @Order(11)
    @DisplayName("statistics table should have all service point metric columns")
    void simulationRunShouldHaveServicePointColumns() throws SQLException {
        assertTrue(columnExists("statistics", "utilization_validation"), "missing utilization_validation");
        assertTrue(columnExists("statistics", "utilization_market"), "missing utilization_market");
        assertTrue(columnExists("statistics", "utilization_limit"), "missing utilization_limit");
        assertTrue(columnExists("statistics", "utilization_execution"), "missing utilization_execution");
        assertTrue(columnExists("statistics", "avg_queue_validation"), "missing avg_queue_validation");
        assertTrue(columnExists("statistics", "avg_queue_market"), "missing avg_queue_market");
        assertTrue(columnExists("statistics", "avg_queue_limit"), "missing avg_queue_limit");
        assertTrue(columnExists("statistics", "avg_queue_execution"), "missing avg_queue_execution");
    }

    @Test
    @Order(12)
    @DisplayName("statistics table should have metadata columns")
    void simulationRunShouldHaveMetadataColumns() throws SQLException {
        assertTrue(columnExists("statistics", "run_timestamp"), "missing run_timestamp");
        assertTrue(columnExists("statistics", "run_name"), "missing run_name");
    }

    // ── Schema: trade table ───────────────────────────────────────────────────

    @Test
    @Order(13)
    @DisplayName("Should create trade table on startup")
    void shouldCreateTradeTable() throws SQLException {
        assertTrue(tableExists("trade"),
                "trade table should exist after initialization");
    }

    @Test
    @Order(14)
    @DisplayName("trade table should have all required columns")
    void tradeShouldHaveAllColumns() throws SQLException {
        assertTrue(columnExists("trade", "id"), "missing id");
        assertTrue(columnExists("trade", "run_id"), "missing run_id");
        assertTrue(columnExists("trade", "buy_order_id"), "missing buy_order_id");
        assertTrue(columnExists("trade", "sell_order_id"), "missing sell_order_id");
        assertTrue(columnExists("trade", "price"), "missing price");
        assertTrue(columnExists("trade", "share_size"), "missing share_size");
        assertTrue(columnExists("trade", "conclusion_time"), "missing conclusion_time");
    }

    // ── Idempotency ───────────────────────────────────────────────────────────

    @Test
    @Order(15)
    @DisplayName("Should not throw if tables already exist on repeated init")
    void shouldNotThrowIfTablesAlreadyExist() {
        assertDoesNotThrow(() -> Database.getInstance(),
                "Calling getInstance() when tables already exist should not throw");
    }

    // ── Close ─────────────────────────────────────────────────────────────────

    @Test
    @Order(16)
    @DisplayName("Should close connection without throwing")
    void shouldCloseWithoutThrowing() {
        assertDoesNotThrow(() -> Database.getInstance().close(),
                "close() should not throw");
    }

    @Test
    @Order(17)
    @DisplayName("Connection should be closed after close() is called")
    void connectionShouldBeClosedAfterClose() throws SQLException {
        Database.getInstance().close();
        Connection connection = Database.getInstance().getConnection();

        assertTrue(connection.isClosed(),
                "Connection should be closed after close() is called");
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private boolean tableExists(String tableName) throws SQLException {
        Connection connection = Database.getInstance().getConnection();
        String sql = "SELECT name FROM sqlite_master WHERE type='table' AND name=?";

        try (var stmt = connection.prepareStatement(sql)) {
            stmt.setString(1, tableName);
            ResultSet rs = stmt.executeQuery();
            return rs.next();
        }
    }

    private boolean columnExists(String tableName, String columnName) throws SQLException {
        Connection connection = Database.getInstance().getConnection();

        try (Statement stmt = connection.createStatement();
                ResultSet rs = stmt.executeQuery("PRAGMA table_info(" + tableName + ")")) {

            while (rs.next()) {
                if (columnName.equals(rs.getString("name"))) {
                    return true;
                }
            }
        }
        return false;
    }
}
