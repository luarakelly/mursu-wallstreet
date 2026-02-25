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
        this.conclusionTime = 0; // zero indicates "not yet concluded"
    }
}