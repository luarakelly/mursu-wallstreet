package eds.model;

import java.util.UUID;

/**
 * Represents a trading order - the main entity flowing through the simulation.
 * - It encapsulates immutable attributes defined at submission (side, type,
 * price, original size, arrival time).
 * - It maintains mutable state reflecting its execution progress (remaining
 * size, status, and conclusion time).
 * 
 * Lifecycle: NEW -> PARTIAL -> FILLED | CANCELLED.
 */

public class Order {

    public enum Side {
        BUY, SELL
    }

    public enum Type {
        MARKET, LIMIT
    }

    public enum Status {
        NEW, PARTIAL, FILLED, CANCELLED
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

    public String getId() {
        return id;
    }

    public Side getSide() {
        return side;
    }

    public Type getType() {
        return type;
    }

    public double getPrice() {
        return price;
    }

    public int getOriginalShareSize() {
        return originalShareSize;
    }

    public int getRemainingShareSize() {
        return remainingShareSize;
    }

    public Status getStatus() {
        return status;
    }

    public double getArrivalTime() {
        return arrivalTime;
    }

    public double getConclusionTime() {
        return conclusionTime;
    }

    /**
     * Checks if the order is still active not filled or cancelled.
     */
    public boolean isActive() {
        return status == Status.NEW || status == Status.PARTIAL;
    }

    public boolean isFilled() {
        return status == Status.FILLED;
    }

    // ── Setters ─────────────────────────────────────────────────────────────

    /**
     * Subtracts from remainingShareSize.
     * Transitions to PARTIAL if shares remain or to FILLED if no shares remain.
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
                    "Requested reduction is greater than available shares: " + qty + " > " + remainingShareSize);
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
     * Cancels the order.
     */
    public void cancel(double currentTime) {
        if (isActive()) {
            status = Status.CANCELLED;
            conclusionTime = currentTime;
        }
    }

    @Override
    public String toString() {
        return String.format(
                "[%s] %s %s @ %.2f  rem=%d  status=%s  arrival=%.4f  conclusion=%.4f",
                id, side, type, price, remainingShareSize, status, arrivalTime, conclusionTime);
    }
}