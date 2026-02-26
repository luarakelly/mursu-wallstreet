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
}