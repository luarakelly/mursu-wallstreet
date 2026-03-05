package eds.model;

import java.util.*;
import eds.framework.Trace;

/**
 * Maintains the resting limit orders on both sides of the order book.
 *
 * <p>
 * The order book stores orders separately for bids and asks using
 * price-sorted maps.
 * </p>
 *
 * <ul>
 * <li><b>Bids</b> are sorted in <i>descending</i> price order (best bid =
 * highest).</li>
 * <li><b>Asks</b> are sorted in <i>ascending</i> price order (best ask =
 * lowest).</li>
 * <li>Orders at the same price level follow <b>FIFO ordering</b>.</li>
 * </ul>
 *
 * <p>
 * The internal data structures are never exposed directly. Consumers interact
 * with the order book through an immutable {@link OrderBookSnapshot}.
 * </p>
 *
 * <p>
 * Mutation operations are intended to be used only by the matching engine,
 * while read-only data is provided for the UI and statistics layer.
 * </p>
 */
public class OrderBook implements IOrderBook {

    // TreeMap sorts ascending by default — good for asks (lowest price first)
    private final TreeMap<Double, Deque<Order>> asks = new TreeMap<>();

    // Comparator.reverseOrder() makes bids sort descending (highest price first)
    private final TreeMap<Double, Deque<Order>> bids = new TreeMap<>(Comparator.reverseOrder());

    // ── State Queries ────────────────────────────────────────────────────────

    /**
     * Checks whether the order book contains any bid orders.
     *
     * @return {@code true} if at least one bid order exists, otherwise
     *         {@code false}
     */
    public boolean hasBids() {
        return !bids.isEmpty();
    }

    /**
     * Checks whether the order book contains any ask orders.
     *
     * @return {@code true} if at least one ask order exists, otherwise
     *         {@code false}
     */
    public boolean hasAsks() {
        return !asks.isEmpty();
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    /**
     * Returns the current best bid price.
     *
     * @return an {@link OptionalDouble} containing the highest bid price,
     *         or an empty optional if no bids exist
     */
    public OptionalDouble getBestBidPrice() {
        return bids.isEmpty()
                ? OptionalDouble.empty()
                : OptionalDouble.of(bids.firstKey());
    }

    /**
     * Returns the current best ask price.
     *
     * @return an {@link OptionalDouble} containing the lowest ask price,
     *         or an empty optional if no asks exist
     */
    public OptionalDouble getBestAskPrice() {
        return asks.isEmpty()
                ? OptionalDouble.empty()
                : OptionalDouble.of(asks.firstKey());
    }

    /**
     * Returns the best bid order currently resting in the book.
     *
     * @return an {@link Optional} containing the best bid order,
     *         or empty if no bids exist
     */
    public Optional<Order> getBestBidOrder() {
        return bids.isEmpty()
                ? Optional.empty()
                : Optional.of(bids.firstEntry().getValue().peekFirst());
    }

    /**
     * Returns the best ask order currently resting in the book.
     *
     * @return an {@link Optional} containing the best ask order,
     *         or empty if no asks exist
     */
    public Optional<Order> getBestAskOrder() {
        return asks.isEmpty()
                ? Optional.empty()
                : Optional.of(asks.firstEntry().getValue().peekFirst());
    }

    /**
     * Computes the mid price of the order book.
     *
     * <p>
     * The mid price is defined as:
     * </p>
     *
     * <pre>
     * (bestBid + bestAsk) / 2
     * </pre>
     *
     * @return an {@link OptionalDouble} containing the mid price,
     *         or empty if either side of the book is empty
     */
    public OptionalDouble getMidPrice() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();

        return OptionalDouble.of((bids.firstKey() + asks.firstKey()) / 2.0);
    }

    /**
     * Computes the bid-ask spread.
     *
     * <p>
     * The spread is defined as:
     * </p>
     *
     * <pre>
     * bestAsk − bestBid
     * </pre>
     *
     * @return an {@link OptionalDouble} containing the spread,
     *         or empty if either side of the book is empty
     */
    public OptionalDouble getSpread() {
        if (bids.isEmpty() || asks.isEmpty())
            return OptionalDouble.empty();

        return OptionalDouble.of(asks.firstKey() - bids.firstKey());
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    /**
     * Returns the internal map corresponding to the given order side.
     *
     * @param side the side of the order (BUY or SELL)
     * @return the {@link TreeMap} storing orders for that side
     */
    private TreeMap<Double, Deque<Order>> sideMap(Order.Side side) {
        return side == Order.Side.BUY ? bids : asks;
    }

    /**
     * Builds a list of {@link PriceLevel} objects from a side map.
     *
     * <p>
     * This method aggregates all orders at each price level into a
     * summary DTO containing:
     * </p>
     *
     * <ul>
     * <li>price level</li>
     * <li>total remaining quantity</li>
     * <li>number of orders</li>
     * </ul>
     *
     * <p>
     * Used internally by {@link #getSnapshot()} to construct the
     * immutable order book view.
     * </p>
     *
     * @param map the side map containing price levels
     * @return a list of aggregated {@link PriceLevel} objects
     */
    private List<PriceLevel> collectLevels(TreeMap<Double, Deque<Order>> map) {

        List<PriceLevel> levels = new ArrayList<>();

        for (Map.Entry<Double, Deque<Order>> entry : map.entrySet()) {

            int totalQty = entry.getValue()
                    .stream()
                    .mapToInt(Order::getRemainingShareSize)
                    .sum();

            levels.add(new PriceLevel(
                    entry.getKey(),
                    totalQty,
                    entry.getValue().size()));
        }

        return levels;
    }

    // ── Mutation methods (matching engine only) ───────────────────────────────

    /**
     * Adds a LIMIT order to the appropriate side of the order book.
     *
     * <p>
     * If the price level does not yet exist, a new FIFO queue
     * is created automatically.
     * </p>
     *
     * @param order the limit order to add
     * @throws IllegalArgumentException if the order type is not LIMIT
     */
    public void addOrder(Order order) {

        if (order.getType() != Order.Type.LIMIT)
            throw new IllegalArgumentException(
                    "Only LIMIT orders rest in the book");

        sideMap(order.getSide())
                .computeIfAbsent(order.getPrice(), p -> new ArrayDeque<>())
                .addLast(order);

        Trace.out(Trace.Level.INFO,
                "OrderBook: ADD " + order.getSide()
                        + " LIMIT " + order.getPrice()
                        + " qty=" + order.getRemainingShareSize());
    }

    /**
     * Removes a specific order from the book.
     *
     * <p>
     * If the removal leaves a price level empty,
     * that level is removed from the map.
     * </p>
     *
     * @param order the order to remove
     */
    public void removeOrder(Order order) {

        TreeMap<Double, Deque<Order>> side = sideMap(order.getSide());
        Deque<Order> level = side.get(order.getPrice());

        if (level != null) {
            level.remove(order);

            if (level.isEmpty())
                side.remove(order.getPrice());
        }
    }

    /**
     * Removes the best bid order from the order book.
     *
     * <p>
     * If the price level becomes empty after removal,
     * the entire level is deleted.
     * </p>
     */
    public void removeBestBidOrder() {

        if (bids.isEmpty())
            return;

        Deque<Order> level = bids.firstEntry().getValue();

        level.pollFirst();

        if (level.isEmpty())
            bids.pollFirstEntry();

        Trace.out(Trace.Level.INFO,
                "OrderBook: REMOVED best bid level");
    }

    /**
     * Removes the best ask order from the order book.
     *
     * <p>
     * If the price level becomes empty after removal,
     * the entire level is deleted.
     * </p>
     */
    public void removeBestAskOrder() {

        if (asks.isEmpty())
            return;

        Deque<Order> level = asks.firstEntry().getValue();

        level.pollFirst();

        if (level.isEmpty())
            asks.pollFirstEntry();

        Trace.out(Trace.Level.INFO,
                "OrderBook: REMOVED best ask level");
    }

    // ── DTOs ─────────────────────────────────────────────────────────────────

    /**
     * Aggregated representation of a single price level.
     *
     * @param price      the price of the level
     * @param totalQty   total remaining quantity at that price
     * @param orderCount number of orders at the level
     */
    public record PriceLevel(double price, int totalQty, int orderCount) {
    }

    /**
     * Immutable snapshot of the entire order book state.
     *
     * <p>
     * This object is safe to expose to external components such as
     * the UI or statistics collectors because it contains no mutable
     * references to internal data structures.
     * </p>
     *
     * @param bids     aggregated bid price levels
     * @param asks     aggregated ask price levels
     * @param bestBid  best bid price if present
     * @param bestAsk  best ask price if present
     * @param midPrice midpoint between best bid and best ask
     * @param spread   difference between best ask and best bid
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
     * Builds an immutable snapshot of the current order book state.
     *
     * <p>
     * The returned lists are unmodifiable using {@link List#copyOf(Collection)}.
     * </p>
     *
     * @return a snapshot containing aggregated bid/ask levels and key metrics
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
     * Returns the total number of resting orders in the book.
     *
     * <p>
     * This counts orders across both bid and ask sides.
     * </p>
     *
     * @return total number of remaining orders
     */
    @Override
    public int remainingOrderCount() {

        int bidsCount = collectLevels(bids)
                .stream()
                .mapToInt(PriceLevel::orderCount)
                .sum();

        int asksCount = collectLevels(asks)
                .stream()
                .mapToInt(PriceLevel::orderCount)
                .sum();

        return bidsCount + asksCount;
    }
}