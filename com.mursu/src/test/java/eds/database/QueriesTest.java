package eds.database;

import eds.database.Records.StatisticsAndMetricsRecord;
import eds.database.Records.TradeRecord;
import eds.model.StatisticsCollector;
import org.junit.jupiter.api.*;

import java.lang.reflect.Field;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Queries.
 *
 * Uses an in-memory SQLite database to avoid touching simulation.db on disk.
 * The Database singleton is reset via reflection before all tests so each
 * test suite starts with a clean, empty schema.
 */
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class QueriesTest {

    private static Queries queries;

    // ── Test setup ────────────────────────────────────────────────────────────

    /**
     * Resets the Database singleton to an in-memory SQLite instance.
     * Uses reflection because Database has no public reset method.
     * This ensures tests never touch simulation.db on disk.
     */
    @BeforeAll
    static void setUp() throws Exception {
        Connection inMemory = DriverManager.getConnection("jdbc:sqlite::memory:");
        inMemory.createStatement().execute("PRAGMA foreign_keys = ON");

        inMemory.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS statistics_and_metrics (
                    id                      INTEGER PRIMARY KEY AUTOINCREMENT,
                    run_timestamp           TEXT    NOT NULL,
                    run_name                TEXT,
                    seed                    INTEGER NOT NULL,
                    mean_validation         REAL    NOT NULL,
                    mean_market             REAL    NOT NULL,
                    mean_limit              REAL    NOT NULL,
                    mean_execution          REAL    NOT NULL,
                    mean_arrival            REAL    NOT NULL,
                    simulation_time         REAL    NOT NULL,
                    total_orders            INTEGER,
                    total_trades            INTEGER,
                    filled_orders           INTEGER,
                    cancelled_orders        INTEGER,
                    remaining_orders        INTEGER,
                    vwap                    REAL,
                    avg_mid_price           REAL,
                    min_price               REAL,
                    max_price               REAL,
                    avg_spread              REAL,
                    avg_latency             REAL,
                    throughput              REAL,
                    fill_rate               REAL,
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

        inMemory.createStatement().execute("""
                CREATE TABLE IF NOT EXISTS trades (
                    id              TEXT    PRIMARY KEY,
                    run_id          INTEGER NOT NULL,
                    buy_order_id    TEXT    NOT NULL,
                    sell_order_id   TEXT    NOT NULL,
                    price           REAL    NOT NULL,
                    share_size      INTEGER NOT NULL,
                    conclusion_time REAL    NOT NULL,
                    FOREIGN KEY (run_id) REFERENCES statistics_and_metrics(id) ON DELETE CASCADE
                )
                """);

        // Inject the in-memory connection into the Database singleton via reflection
        Database dbInstance = Database.getInstance();
        Field connectionField = Database.class.getDeclaredField("connection");
        connectionField.setAccessible(true);
        connectionField.set(dbInstance, inMemory);

        queries = new Queries();
    }

    // ── Shared test fixtures ──────────────────────────────────────────────────

    private static StatisticsAndMetricsRecord sampleRecord() {
        return new StatisticsAndMetricsRecord(
                0,
                "2024-01-01T10:00:00",
                "TestRun",
                42L,
                1.0, 2.0, 3.0, 4.0, 5.0,
                1000.0,
                500, 120, 300,
                100, 100,
                99.5,
                100.0, 95.0, 105.0,
                0.05,
                0.8,
                0.3,
                0.6,
                0.75, 0.80, 0.70, 0.65,
                1.2, 2.3, 1.8, 0.9);
    }

    private static TradeRecord sampleTrade(String id) {
        return new TradeRecord(id, 0, "BUY-001", "SELL-001", 100.0, 50, 123.45);
    }

    private static StatisticsCollector.Snapshot sampleSnapshot() {
        return new StatisticsCollector.Snapshot(
                500L, 300L, 100, 120L,
                99.5, 100.0, 95.0, 105.0,
                0.05, 0.3, 0.8, 0.6, 0.725,
                List.of(0.75, 0.80, 0.70, 0.65),
                List.of(1.2, 2.3, 1.8, 0.9),
                true, 1, 2.3,
                List.of());
    }

    // ── buildRecord() ─────────────────────────────────────────────────────────

    @Test
    @Order(1)
    @DisplayName("buildRecord: maps snapshot fields correctly to record")
    void buildRecord_mapsSnapshotFieldsCorrectly() {
        StatisticsAndMetricsRecord record = queries.buildRecord(
                sampleSnapshot(), "Run1", 42L,
                1.0, 2.0, 3.0, 4.0, 5.0, 1000.0);

        assertAll(
                () -> assertEquals("Run1", record.runName()),
                () -> assertEquals(42L, record.seed()),
                () -> assertEquals(1.0, record.meanValidation()),
                () -> assertEquals(2.0, record.meanMarket()),
                () -> assertEquals(3.0, record.meanLimit()),
                () -> assertEquals(4.0, record.meanExecution()),
                () -> assertEquals(5.0, record.meanArrival()),
                () -> assertEquals(1000.0, record.simulationTime()),
                () -> assertEquals(500, record.totalOrders()),
                () -> assertEquals(120, record.totalTrades()),
                () -> assertEquals(300, record.filledOrders()),
                () -> assertEquals(99.5, record.vwap()),
                () -> assertEquals(100.0, record.avgMidPrice()),
                () -> assertEquals(95.0, record.minPrice()),
                () -> assertEquals(105.0, record.maxPrice()),
                () -> assertEquals(0.05, record.avgSpread()),
                () -> assertEquals(0.8, record.avgLatency()),
                () -> assertEquals(0.3, record.throughput()),
                () -> assertEquals(0.6, record.fillRate()),
                () -> assertEquals(0.75, record.utilizationValidation()),
                () -> assertEquals(0.80, record.utilizationMarket()),
                () -> assertEquals(0.70, record.utilizationLimit()),
                () -> assertEquals(0.65, record.utilizationExecution()),
                () -> assertEquals(1.2, record.avgQueueValidation()),
                () -> assertEquals(2.3, record.avgQueueMarket()),
                () -> assertEquals(1.8, record.avgQueueLimit()),
                () -> assertEquals(0.9, record.avgQueueExecution()));
    }

    @Test
    @Order(2)
    @DisplayName("buildRecord: cancelledOrders derived correctly from snapshot")
    void buildRecord_cancelledOrdersDerivedCorrectly() {
        // cancelledOrders = totalArrivedOrders - totalExecutedOrders -
        // remainingOrdersInBook
        // = 500 - 300 - 100 = 100
        StatisticsAndMetricsRecord record = queries.buildRecord(
                sampleSnapshot(), "Run1", 42L,
                1.0, 2.0, 3.0, 4.0, 5.0, 1000.0);

        assertEquals(100, record.cancelledOrders());
    }

    @Test
    @Order(3)
    @DisplayName("buildRecord: id is always 0 — generated by DB")
    void buildRecord_idIsZero() {
        StatisticsAndMetricsRecord record = queries.buildRecord(
                sampleSnapshot(), "Run1", 1L,
                1.0, 2.0, 3.0, 4.0, 5.0, 1000.0);

        assertEquals(0, record.id());
    }

    @Test
    @Order(4)
    @DisplayName("buildRecord: handles empty service point lists with safeGet")
    void buildRecord_emptyServicePointListsDefaultToZero() {
        StatisticsCollector.Snapshot snap = new StatisticsCollector.Snapshot(
                0L, 0L, 0, 0L,
                0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                List.of(), List.of(),
                false, -1, 0.0,
                List.of());

        StatisticsAndMetricsRecord record = queries.buildRecord(
                snap, "EmptyRun", 1L,
                1.0, 2.0, 3.0, 4.0, 5.0, 100.0);

        assertAll(
                () -> assertEquals(0.0, record.utilizationValidation()),
                () -> assertEquals(0.0, record.utilizationMarket()),
                () -> assertEquals(0.0, record.utilizationLimit()),
                () -> assertEquals(0.0, record.utilizationExecution()),
                () -> assertEquals(0.0, record.avgQueueValidation()),
                () -> assertEquals(0.0, record.avgQueueMarket()),
                () -> assertEquals(0.0, record.avgQueueLimit()),
                () -> assertEquals(0.0, record.avgQueueExecution()));
    }

    // ── saveStatisticsAndMetrics() ────────────────────────────────────────────

    @Test
    @Order(5)
    @DisplayName("saveStatisticsAndMetrics: returns a positive generated id on success")
    void save_returnsPositiveRunId() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        assertTrue(runId > 0, "Expected a positive generated run id, got: " + runId);
    }

    @Test
    @Order(6)
    @DisplayName("saveStatisticsAndMetrics: each save returns a unique incrementing id")
    void save_eachSaveReturnsUniqueId() {
        int id1 = queries.saveStatisticsAndMetrics(sampleRecord());
        int id2 = queries.saveStatisticsAndMetrics(sampleRecord());
        assertTrue(id2 > id1, "Second save should return a higher id than the first");
    }

    // ── saveAllTrades() ───────────────────────────────────────────────────────

    @Test
    @Order(7)
    @DisplayName("saveAllTrades: returns count of saved trades")
    void saveAllTrades_returnsCorrectCount() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        int saved = queries.saveAllTrades(
                List.of(sampleTrade("T-001"), sampleTrade("T-002"), sampleTrade("T-003")),
                runId);
        assertEquals(3, saved);
    }

    @Test
    @Order(8)
    @DisplayName("saveAllTrades: returns 0 for empty trade list")
    void saveAllTrades_emptyListReturnsZero() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        assertEquals(0, queries.saveAllTrades(List.of(), runId));
    }

    @Test
    @Order(9)
    @DisplayName("saveAllTrades: returns 0 for null trade list")
    void saveAllTrades_nullListReturnsZero() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        assertEquals(0, queries.saveAllTrades(null, runId));
    }

    // ── findAll() ─────────────────────────────────────────────────────────────

    @Test
    @Order(10)
    @DisplayName("findAll: returns non-empty list after saves")
    void findAll_returnsNonEmptyList() {
        queries.saveStatisticsAndMetrics(sampleRecord());
        assertFalse(queries.findAll().isEmpty());
    }

    @Test
    @Order(11)
    @DisplayName("findAll: results are ordered most recent first (descending id)")
    void findAll_orderedByIdDescending() {
        int id1 = queries.saveStatisticsAndMetrics(sampleRecord());
        int id2 = queries.saveStatisticsAndMetrics(sampleRecord());

        List<StatisticsAndMetricsRecord> all = queries.findAll();

        int pos1 = -1, pos2 = -1;
        for (int i = 0; i < all.size(); i++) {
            if (all.get(i).id() == id2)
                pos2 = i;
            if (all.get(i).id() == id1)
                pos1 = i;
        }
        assertTrue(pos2 < pos1, "Most recent run should appear first");
    }

    // ── findById() ────────────────────────────────────────────────────────────

    @Test
    @Order(12)
    @DisplayName("findById: returns correct record for existing id")
    void findById_returnsCorrectRecord() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        Optional<StatisticsAndMetricsRecord> result = queries.findById(runId);

        assertTrue(result.isPresent());
        assertEquals(runId, result.get().id());
        assertEquals("TestRun", result.get().runName());
    }

    @Test
    @Order(13)
    @DisplayName("findById: returns Optional.empty for non-existent id")
    void findById_returnsEmptyForMissingId() {
        assertTrue(queries.findById(Integer.MAX_VALUE).isEmpty());
    }

    @Test
    @Order(14)
    @DisplayName("findById: persisted values match what was saved")
    void findById_persistedValuesMatchSaved() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        StatisticsAndMetricsRecord found = queries.findById(runId).orElseThrow();

        assertAll(
                () -> assertEquals(42L, found.seed()),
                () -> assertEquals("TestRun", found.runName()),
                () -> assertEquals(500, found.totalOrders()),
                () -> assertEquals(99.5, found.vwap()),
                () -> assertEquals(0.6, found.fillRate()),
                () -> assertEquals(0.75, found.utilizationValidation()));
    }

    // ── findTradesByRunId() ───────────────────────────────────────────────────

    @Test
    @Order(15)
    @DisplayName("findTradesByRunId: returns saved trades for a run")
    void findTradesByRunId_returnsSavedTrades() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        queries.saveAllTrades(List.of(sampleTrade("T-A01"), sampleTrade("T-A02")), runId);

        assertEquals(2, queries.findTradesByRunId(runId).size());
    }

    @Test
    @Order(16)
    @DisplayName("findTradesByRunId: trades are ordered by conclusion_time ascending")
    void findTradesByRunId_orderedByConclusionTimeAscending() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        queries.saveAllTrades(List.of(
                new TradeRecord("T-B03", 0, "BUY-1", "SELL-1", 100.0, 10, 300.0),
                new TradeRecord("T-B01", 0, "BUY-2", "SELL-2", 101.0, 20, 100.0),
                new TradeRecord("T-B02", 0, "BUY-3", "SELL-3", 102.0, 30, 200.0)),
                runId);

        List<TradeRecord> trades = queries.findTradesByRunId(runId);

        assertEquals(100.0, trades.get(0).conclusionTime());
        assertEquals(200.0, trades.get(1).conclusionTime());
        assertEquals(300.0, trades.get(2).conclusionTime());
    }

    @Test
    @Order(17)
    @DisplayName("findTradesByRunId: returns empty list for run with no trades")
    void findTradesByRunId_emptyListForRunWithNoTrades() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        assertTrue(queries.findTradesByRunId(runId).isEmpty());
    }

    @Test
    @Order(18)
    @DisplayName("findTradesByRunId: trade fields are persisted correctly")
    void findTradesByRunId_tradeFieldsPersistedCorrectly() {
        int runId = queries.saveStatisticsAndMetrics(sampleRecord());
        queries.saveAllTrades(
                List.of(new TradeRecord("T-C01", 0, "BUY-X", "SELL-X", 123.45, 75, 999.0)),
                runId);

        TradeRecord found = queries.findTradesByRunId(runId).get(0);

        assertAll(
                () -> assertEquals("T-C01", found.id()),
                () -> assertEquals(runId, found.runId()),
                () -> assertEquals("BUY-X", found.buyOrderId()),
                () -> assertEquals("SELL-X", found.sellOrderId()),
                () -> assertEquals(123.45, found.price()),
                () -> assertEquals(75, found.shareSize()),
                () -> assertEquals(999.0, found.conclusionTime()));
    }

    @Test
    @Order(19)
    @DisplayName("findTradesByRunId: returns empty list for non-existent run id")
    void findTradesByRunId_emptyForNonExistentRunId() {
        assertTrue(queries.findTradesByRunId(Integer.MAX_VALUE).isEmpty());
    }
}
