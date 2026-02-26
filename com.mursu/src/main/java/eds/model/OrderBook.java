package eds.model;

import java.util.*;

/**
 * Maintains the resting limit orders on both bid and ask sides.
 * ───── Bids -> sorted descending by price (best bid = highest)
 * ───── Asks -> sorted ascending by price (best ask = lowest)
 * ───── Within the same price level, FIFO ordering is preserved via insertion.
 * Does NOT expose internal data structures.
 * Provides controlled mutation methods for the matching engine.
 */
public class OrderBook {
    // Using TreeMap, because with it insertions automatically go to correct sorted
    // position and we can easily get best price levels via firstKey().

    // Price -> list of resting orders at that level (FIFO within level)
    private final TreeMap<Double, Deque<Order>> asks = new TreeMap<>();
    // By default, TreeMap sorts ascending that is why we need
    // Comparator.reverseOrder(). for bids.
    private final TreeMap<Double, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    // ── State Queries ───────────────────────────────────────────────────────
    public boolean hasBids() {
        return !bids.isEmpty();
    }

    public boolean hasAsks() {
        return !asks.isEmpty();
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    /**
     * Uses Optional/OptionalDouble to safely handle the case when there are no
     * resting bids/asks. This avoids returning null and forces the caller to handle
     * absence explicitly avoinding NullPointerException
     */

    public OptionalDouble getBestBidPrice() {
        return bids.isEmpty()
                ? OptionalDouble.empty() // indicates "there is no best bid right now".
                : OptionalDouble.of(bids.firstKey());
    }

    public OptionalDouble getBestAskPrice() {
        return asks.isEmpty()
                ? OptionalDouble.empty()
                : OptionalDouble.of(asks.firstKey());
    }

    public Optional<Order> getBestBidOrder() {
        if (bids.isEmpty())
            return Optional.empty();
        return Optional.of(bids.firstEntry().getValue().peekFirst());
    }

    public Optional<Order> getBestAskOrder() {
        if (asks.isEmpty())
            return Optional.empty();
        return Optional.of(asks.firstEntry().getValue().peekFirst());
    }

    public OptionalDouble getMidPrice() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();

        return OptionalDouble.of(
                (bids.firstKey() + asks.firstKey()) / 2.0);
    }

    public OptionalDouble getSpread() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();

        return OptionalDouble.of(
                asks.firstKey() - bids.firstKey());
    }

    // ── Helpers ─────────────────────────────────────────────────────────────

    private TreeMap<Double, Deque<Order>> sideMap(Order.Side side) {
        return side == Order.Side.BUY ? bids : asks;
    }

    // ── Mutation methods for matching engine ─────────────────────────────────
    /**
     * Adds a LIMIT order to the correct side and price level.
     * Creates the level if it does not yet exist.
     */

    public void addOrder(Order order) {
        if (order.getType() != Order.Type.LIMIT) {
            throw new IllegalArgumentException("Only LIMIT orders rest in the book");
        }
        TreeMap<Double, Deque<Order>> side = sideMap(order.getSide());
        /*
         * computeIfAbsent() is defined in java.util.Map Interface and is inherited
         * through the TreeMap. Here it is used to compute a value
         * for a given key if it is not already present in the map. In this case, it
         * checks if there is already a Deque<Order> for the given price level
         * (order.getPrice()). If there isn't, it creates a new ArrayDeque<Order>() and
         * associates it with that price level. Then, it adds the order to the end of
         * the deque for that price level using addLast(order).
         * method signature: V computeIfAbsent(K key, Function<? super K, ? extends V>
         * mappingFunction)
         * 
         * Syntax to define an anonimous function:
         * (parameters) -> (what it does/returns).
         */
        side.computeIfAbsent(order.getPrice(), (priceLevel) -> (new ArrayDeque<>())).addLast(order); // FIFO
    }

    /**
     * Removes a specific order by identity.
     * Cleans up the price level if it becomes empty.
     */
    public void removeOrderByID(Order order) {
        TreeMap<Double, Deque<Order>> side = sideMap(order.getSide());
        Deque<Order> priceLevel = side.get(order.getPrice());
        if (priceLevel != null) {
            priceLevel.remove(order);
            if (priceLevel.isEmpty())
                side.remove(order.getPrice());
        }
    }

    /**
     * Removes the fully-filled best bid order.
     * Automatically removes empty price levels.
     */
    public void removeBestBidOrder() {
        if (bids.isEmpty())
            return;

        Deque<Order> level = bids.firstEntry().getValue();
        level.pollFirst();

        if (level.isEmpty()) {
            bids.pollFirstEntry();
        }
    }

    /**
     * Removes the fully-filled best ask order.
     * Automatically removes empty price levels.
     */
    public void removeBestAskOrder() {
        if (asks.isEmpty())
            return;

        Deque<Order> level = asks.firstEntry().getValue();
        level.pollFirst();

        if (level.isEmpty()) {
            asks.pollFirstEntry();
        }
    }
}