package eds.model;

import java.util.Locale;
import java.util.UUID;

/**
 * Represents a single executed trade in the simulation.
 *
 * Trade is an immutable execution record created by the matching engine
 * when a buy and a sell order successfully match.
 */

public class Trade {
    /** Unique identifier of the trade. */
    private final String id;

    /** Unique identifier of the buy order involved in the trade. */
    private final String buyOrderId;

    /** Unique identifier of the sell order involved in the trade. */
    private final String sellOrderId;

    /** Execution price of the trade. */
    private final double price;

    /** Quantity of shares executed in this trade. */
    private final int shareSize;

    /** Simulation time at which the trade occurred. */
    private final double conclusionTime;

    /**
     * Constructs a new Trade.
     *
     * @param buyOrderId     id of the buy order
     * @param sellOrderId    id of the sell order
     * @param price          execution price (must be positive)
     * @param shareSize      executed quantity (must be positive)
     * @param conclusionTime simulation time at which the trade occurred
     *
     * @throws IllegalArgumentException if order IDs are equal
     */
    public Trade(String buyOrderId, String sellOrderId, double price, int shareSize, double conclusionTime) {
        if (buyOrderId.equals(sellOrderId)) {
            throw new IllegalArgumentException("Buy order ID and Sell order ID must not be the same");
        }

        if (price <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }

        if (shareSize <= 0) {
            throw new IllegalArgumentException("Share size must be positive");
        }

        this.id = UUID.randomUUID().toString().substring(0, 8).toUpperCase();

        this.buyOrderId = buyOrderId;
        this.sellOrderId = sellOrderId;
        this.price = price;
        this.shareSize = shareSize;
        this.conclusionTime = conclusionTime;
    }

    /** @return unique trade identifier */
    public String getId() {
        return id;
    }

    /** @return buy order identifier */
    public String getBuyOrderId() {
        return buyOrderId;
    }

    /** @return sell order identifier */
    public String getSellOrderId() {
        return sellOrderId;
    }

    /** @return execution price */
    public double getPrice() {
        return price;
    }

    /** @return executed quantity of shares */
    public int getShareSize() {
        return shareSize;
    }

    /** @return simulation time at which trade occurred */
    public double getConclusionTime() {
        return conclusionTime;
    }

    /**
     * Returns a formatted string representation of the trade.
     */
    @Override
    public String toString() {
        return String.format(
                Locale.US,
                "[%s] buyID=%s sellID=%s @ %.2f size=%d conclusion=%.4f",
                id, buyOrderId, sellOrderId, price, shareSize, conclusionTime);
    }
}