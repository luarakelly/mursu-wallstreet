package eds.model;

import java.util.*;
import eds.framework.Trace;

/**
 * Maintains the resting limit orders on both bid and ask sides.
 * ───── Bids -> sorted descending by price (best bid = highest)
 * ───── Asks -> sorted ascending by price (best ask = lowest)
 * ───── Within the same price level, FIFO ordering is preserved via insertion.
 * Does NOT expose internal data structures.
 * Provides controlled mutation methods for the matching engine.
 * Provides a read-only OrderBookSnapshot for the UI and statistics layer.
 */
public class OrderBook implements IOrderBook {
    // TreeMap sorts ascending by default — good for asks (lowest price first)
    private final TreeMap<Double, Deque<Order>> asks = new TreeMap<>();
    // Comparator.reverseOrder() makes bids sort descending (highest price first)
    private final TreeMap<Double, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    // ── State Queries ────────────────────────────────────────────────────────
    public boolean hasBids() {
        return !bids.isEmpty();
    }

    public boolean hasAsks() {
        return !asks.isEmpty();
    }

    // ── Getters ──────────────────────────────────────────────────────────────
    // OptionalDouble safely handles absence — forces callers to handle empty case
    // explicitly, avoiding NullPointerException.

    public OptionalDouble getBestBidPrice() {
        return bids.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(bids.firstKey());
    }

    public OptionalDouble getBestAskPrice() {
        return asks.isEmpty() ? OptionalDouble.empty() : OptionalDouble.of(asks.firstKey());
    }

    public Optional<Order> getBestBidOrder() {
        return bids.isEmpty() ? Optional.empty() : Optional.of(bids.firstEntry().getValue().peekFirst());
    }

    public Optional<Order> getBestAskOrder() {
        return asks.isEmpty() ? Optional.empty() : Optional.of(asks.firstEntry().getValue().peekFirst());
    }

    public OptionalDouble getMidPrice() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();
        return OptionalDouble.of((bids.firstKey() + asks.firstKey()) / 2.0);
    }

    public OptionalDouble getSpread() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();
        return OptionalDouble.of(asks.firstKey() - bids.firstKey());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────
    private TreeMap<Double, Deque<Order>> sideMap(Order.Side side) {
        return side == Order.Side.BUY ? bids : asks;
    }

    /**
     * Builds a list of PriceLevel DTOs from a side map.
     * Used by getSnapshot() for both bids and asks — single source of truth.
     */
    private List<PriceLevel> collectLevels(TreeMap<Double, Deque<Order>> map) {
        List<PriceLevel> levels = new ArrayList<>();
        for (Map.Entry<Double, Deque<Order>> entry : map.entrySet()) {
            int totalQty = entry.getValue().stream()
                    .mapToInt(Order::getRemainingShareSize)
                    .sum();
            levels.add(new PriceLevel(entry.getKey(), totalQty, entry.getValue().size()));
        }
        return levels;
    }

    // ── Mutation methods (matching engine only) ───────────────────────────────

    /**
     * Adds a LIMIT order to the correct side and price level.
     * Creates the price level deque if it does not yet exist.
     * computeIfAbsent() checks if a Deque exists for the price key —
     * if not, creates a new ArrayDeque and associates it, then adds the order FIFO.
     */
    public void addOrder(Order order) {
        if (order.getType() != Order.Type.LIMIT)
            throw new IllegalArgumentException("Only LIMIT orders rest in the book");
        sideMap(order.getSide())
                .computeIfAbsent(order.getPrice(), p -> new ArrayDeque<>())
                .addLast(order);
        Trace.out(Trace.Level.INFO, "OrderBook: ADD " + order.getSide()
                + " LIMIT " + order.getPrice() + " qty=" + order.getRemainingShareSize());
    }

    /** Removes a specific order by identity. Cleans up empty price levels. */
    public void removeOrder(Order order) {
        TreeMap<Double, Deque<Order>> side = sideMap(order.getSide());
        Deque<Order> level = side.get(order.getPrice());
        if (level != null) {
            level.remove(order);
            if (level.isEmpty())
                side.remove(order.getPrice());
        }
    }

    /** Removes the fully-filled best bid order. Cleans up empty price levels. */
    public void removeBestBidOrder() {
        if (bids.isEmpty())
            return;
        Deque<Order> level = bids.firstEntry().getValue();
        level.pollFirst();
        if (level.isEmpty())
            bids.pollFirstEntry();
        Trace.out(Trace.Level.INFO, "OrderBook: REMOVED best bid level");
    }

    /** Removes the fully-filled best ask order. Cleans up empty price levels. */
    public void removeBestAskOrder() {
        if (asks.isEmpty())
            return;
        Deque<Order> level = asks.firstEntry().getValue();
        level.pollFirst();
        if (level.isEmpty())
            asks.pollFirstEntry();
        Trace.out(Trace.Level.INFO, "OrderBook: REMOVED best ask level");
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    /** Aggregated view of one price level — used in OrderBookSnapshot. */
    public record PriceLevel(double price, int totalQty, int orderCount) {
    }

    /**
     * Immutable snapshot of the full book state.
     * The only object the UI and StatisticsCollector need —
     * no internal structures are ever exposed.
     */
    public record OrderBookSnapshot(
            List<PriceLevel> bids,
            List<PriceLevel> asks,
            OptionalDouble bestBid,
            OptionalDouble bestAsk,
            OptionalDouble midPrice,
            OptionalDouble spread) {
    }

    // ── Snapshot (UI + statistics) ────────────────────────────────────────────
    /**
     * Builds and returns an immutable snapshot of the current book state.
     * List.copyOf() ensures the snapshot lists are unmodifiable.
     */
    @Override
    public OrderBookSnapshot getSnapshot() {
        return new OrderBookSnapshot(
                List.copyOf(collectLevels(bids)),
                List.copyOf(collectLevels(asks)),
                getBestBidPrice(),
                getBestAskPrice(),
                getMidPrice(),
                getSpread());
    }

    /**
     * Total resting orders across both sides. Sums orderCount from each PriceLevel
     * DTO (Data Transfer Object).
     */
    @Override
    public int remainingOrderCount() {
        int bidsCount = collectLevels(bids).stream().mapToInt(PriceLevel::orderCount).sum();
        int asksCount = collectLevels(asks).stream().mapToInt(PriceLevel::orderCount).sum();
        return bidsCount + asksCount;
    }
}