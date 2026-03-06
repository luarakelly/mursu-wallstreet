package eds.model;

import java.util.UUID;
import eds.framework.ISimulationEntity;

/**
 * Represents a trading order flowing through the simulation.
 *
 * <p>
 * An order contains both immutable attributes defined at submission
 * and mutable state that changes during execution.
 * </p>
 *
 * <ul>
 * <li><b>Immutable attributes:</b> side, type, price, original size, arrival
 * time</li>
 * <li><b>Mutable state:</b> remaining size, execution status, and conclusion
 * time</li>
 * </ul>
 *
 * <p>
 * <b>Lifecycle:</b>
 * </p>
 *
 * <pre>
 * NEW → PARTIAL → FILLED
 *           ↘
 *           CANCELLED
 * </pre>
 */
public class Order implements ISimulationEntity {

    /**
     * The side of the order.
     */
    public enum Side {
        /** Buy order */
        BUY,

        /** Sell order */
        SELL
    }

    /**
     * The type of order.
     */
    public enum Type {
        /** Market order executed immediately against available liquidity */
        MARKET,

        /** Limit order resting at a specific price level */
        LIMIT
    }

    /**
     * Current execution status of the order.
     */
    public enum Status {
        /** Order has just been submitted */
        NEW,

        /** Order has been partially executed */
        PARTIAL,

        /** Order has been fully executed */
        FILLED,

        /** Order has been cancelled before full execution */
        CANCELLED
    }

    private final String id;
    private final Side side;
    private final Type type;
    private final double price; // ignored for MARKET orders
    private final int originalShareSize; // quantity at arrival, never changes
    private int remainingShareSize; // decreases as fills occur
    private Status status;
    private final double arrivalTime; // simulation timestamp
    private double conclusionTime; // simulation timestamp at completion

    /**
     * Creates a new order instance.
     *
     * @param side        the order side (BUY or SELL)
     * @param type        the order type (MARKET or LIMIT)
     * @param price       the limit price; ignored for MARKET orders
     * @param size        the number of shares in the order
     * @param arrivalTime the simulation timestamp when the order arrived
     */
    public Order(Side side, Type type, double price, int size, double arrivalTime) {
        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.side = side;
        this.type = type;
        this.price = price;
        this.originalShareSize = size;
        this.remainingShareSize = size;
        this.status = Status.NEW;
        this.arrivalTime = arrivalTime;
        this.conclusionTime = Double.NaN; // indicates "not yet concluded"
    }

    // ── Getters ─────────────────────────────────────────────────────────────

    /**
     * Returns the unique identifier of this order.
     *
     * @return the order ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns the side of the order.
     *
     * @return the order side
     */
    public Side getSide() {
        return side;
    }

    /**
     * Returns the order type.
     *
     * @return the order type
     */
    public Type getType() {
        return type;
    }

    /**
     * Returns the limit price of the order.
     *
     * <p>
     * For {@link Type#MARKET} orders this value is ignored by the matching engine.
     * </p>
     *
     * @return the order price
     */
    public double getPrice() {
        return price;
    }

    /**
     * Returns the original share quantity submitted with the order.
     *
     * @return the original order size
     */
    public int getOriginalShareSize() {
        return originalShareSize;
    }

    /**
     * Returns the remaining shares yet to be executed.
     *
     * @return the remaining share size
     */
    public int getRemainingShareSize() {
        return remainingShareSize;
    }

    /**
     * Returns the current execution status of the order.
     *
     * @return the order status
     */
    public Status getStatus() {
        return status;
    }

    /**
     * Returns the simulation timestamp when the order arrived.
     *
     * @return the arrival time
     */
    public double getArrivalTime() {
        return arrivalTime;
    }

    /**
     * Returns the simulation timestamp when the order finished execution.
     *
     * <p>
     * If the order has not yet completed, this value will be {@link Double#NaN}.
     * </p>
     *
     * @return the conclusion time
     */
    public double getConclusionTime() {
        return conclusionTime;
    }

    /**
     * Determines whether the order is still active in the market.
     *
     * @return {@code true} if the order status is {@link Status#NEW}
     *         or {@link Status#PARTIAL}, otherwise {@code false}
     */
    public boolean isActive() {
        return status == Status.NEW || status == Status.PARTIAL;
    }

    /**
     * Determines whether the order has been fully executed.
     *
     * @return {@code true} if the status is {@link Status#FILLED}
     */
    public boolean isFilled() {
        return status == Status.FILLED;
    }

    // ── State Updates ─────────────────────────────────────────────────────────

    /**
     * Reduces the remaining share size after a trade execution.
     *
     * <p>
     * If shares remain after the reduction, the order transitions to
     * {@link Status#PARTIAL}. If no shares remain, the order transitions
     * to {@link Status#FILLED} and the conclusion time is recorded.
     * </p>
     *
     * @param qty         the number of shares executed
     * @param currentTime the simulation timestamp of the execution
     *
     * @throws IllegalArgumentException if the quantity is non-positive
     * @throws IllegalArgumentException if the quantity exceeds remaining shares
     * @throws IllegalStateException    if the order is not active
     */
    public void reduceShareSize(int qty, double currentTime) {

        if (qty <= 0) {
            throw new IllegalArgumentException("Reduction quantity must be positive");
        }

        if (!isActive()) {
            throw new IllegalStateException("Cannot reduce a non-active order");
        }

        if (qty > remainingShareSize) {
            throw new IllegalArgumentException(
                    "Requested reduction is greater than available shares: "
                            + qty + " > " + remainingShareSize);
        }

        remainingShareSize -= qty;

        if (remainingShareSize <= 0) {
            status = Status.FILLED;
            conclusionTime = currentTime;
        } else {
            status = Status.PARTIAL;
        }
    }

    /**
     * Cancels the order if it is still active.
     *
     * <p>
     * The order status becomes {@link Status#CANCELLED} and the
     * conclusion time is recorded.
     * </p>
     *
     * @param currentTime the simulation timestamp of the cancellation
     */
    public void cancel(double currentTime) {

        if (isActive()) {
            status = Status.CANCELLED;
            conclusionTime = currentTime;
        }
    }

    /**
     * Returns a formatted string representation of the order.
     *
     * @return a human-readable representation of the order state
     */
    @Override
    public String toString() {
        return String.format(
                "[%s] %s %s @ %.2f  rem=%d  status=%s  arrival=%.4f  conclusion=%.4f",
                id, side, type, price, remainingShareSize, status, arrivalTime, conclusionTime);
    }
}