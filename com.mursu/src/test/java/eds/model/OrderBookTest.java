package eds.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.OptionalDouble;

import static org.junit.jupiter.api.Assertions.*;

class OrderBookTest {

    // ── Setup ────────────────────────────────────────────────────────────────
    private OrderBook createBookWithOrders() {
        OrderBook book = new OrderBook();
        Order buy1 = new Order(Order.Side.BUY, Order.Type.LIMIT, 101.0, 100, 0.0);
        Order buy2 = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 50, 0.1);
        Order sell1 = new Order(Order.Side.SELL, Order.Type.LIMIT, 102.0, 200, 0.2);
        Order sell2 = new Order(Order.Side.SELL, Order.Type.LIMIT, 103.0, 75, 0.3);

        book.addOrder(buy1);
        book.addOrder(buy2);
        book.addOrder(sell1);
        book.addOrder(sell2);
        return book;
    }

    // ── State Queries ────────────────────────────────────────────────────────
    @Nested
    @DisplayName("State Queries")
    class StateQueries {

        @Test
        @DisplayName("hasBids and hasAsks reflect empty vs non-empty book")
        void hasBidsHasAsks() {
            OrderBook emptyBook = new OrderBook();
            assertFalse(emptyBook.hasBids());
            assertFalse(emptyBook.hasAsks());

            OrderBook book = createBookWithOrders();
            assertTrue(book.hasBids());
            assertTrue(book.hasAsks());
        }
    }

    // ── Getters ─────────────────────────────────────────────────────────────
    @Nested
    @DisplayName("Getters and Optional queries")
    class Getters {

        @Test
        @DisplayName("Best bid and ask prices and orders")
        void bestPricesAndOrders() {
            OrderBook book = createBookWithOrders();

            OptionalDouble bestBid = book.getBestBidPrice();
            OptionalDouble bestAsk = book.getBestAskPrice();

            assertTrue(bestBid.isPresent());
            assertEquals(101.0, bestBid.getAsDouble());

            assertTrue(bestAsk.isPresent());
            assertEquals(102.0, bestAsk.getAsDouble());

            Optional<Order> bestBidOrder = book.getBestBidOrder();
            Optional<Order> bestAskOrder = book.getBestAskOrder();

            assertTrue(bestBidOrder.isPresent());
            assertEquals(101.0, bestBidOrder.get().getPrice());

            assertTrue(bestAskOrder.isPresent());
            assertEquals(102.0, bestAskOrder.get().getPrice());
        }

        @Test
        @DisplayName("Mid price and spread calculations")
        void midPriceAndSpread() {
            OrderBook book = createBookWithOrders();

            OptionalDouble mid = book.getMidPrice();
            OptionalDouble spread = book.getSpread();

            assertTrue(mid.isPresent());
            assertEquals((101.0 + 102.0) / 2.0, mid.getAsDouble());

            assertTrue(spread.isPresent());
            assertEquals(102.0 - 101.0, spread.getAsDouble());
        }

        @Test
        @DisplayName("Optional returns empty on empty book")
        void optionalEmptyForEmptyBook() {
            OrderBook book = new OrderBook();

            assertTrue(book.getBestBidPrice().isEmpty());
            assertTrue(book.getBestAskPrice().isEmpty());
            assertTrue(book.getBestBidOrder().isEmpty());
            assertTrue(book.getBestAskOrder().isEmpty());
            assertTrue(book.getMidPrice().isEmpty());
            assertTrue(book.getSpread().isEmpty());
        }
    }

    // ── Mutation Methods ─────────────────────────────────────────────────────
    @Nested
    @DisplayName("Mutation methods")
    class Mutation {

        @Test
        @DisplayName("addOrder inserts orders at correct price level")
        void addOrderInsertsCorrectly() {
            OrderBook book = new OrderBook();
            Order buy = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 50, 0.0);
            Order sell = new Order(Order.Side.SELL, Order.Type.LIMIT, 105.0, 30, 0.0);

            book.addOrder(buy);
            book.addOrder(sell);

            assertEquals(100.0, book.getBestBidPrice().getAsDouble());
            assertEquals(105.0, book.getBestAskPrice().getAsDouble());
        }

        @Test
        @DisplayName("removeOrderByID removes specific order and cleans level")
        void removeOrderByID() {
            OrderBook book = createBookWithOrders();
            Optional<Order> bestBidOrder = book.getBestBidOrder();
            assertTrue(bestBidOrder.isPresent());

            Order orderToRemove = bestBidOrder.get();
            book.removeOrder(orderToRemove);
            assertFalse(book.getBestBidOrder().get().getId().equals(orderToRemove.getId()));
            // Best bid should now be next highest
            assertEquals(100.0, book.getBestBidPrice().getAsDouble());
        }

        @Test
        @DisplayName("removeBestBidOrder and removeBestAskOrder remove top order and clean empty levels")
        void removeBestOrders() {
            OrderBook book = createBookWithOrders();

            book.removeBestBidOrder(); // removes 101.0
            assertEquals(100.0, book.getBestBidPrice().getAsDouble());

            book.removeBestAskOrder(); // removes 102.0
            assertEquals(103.0, book.getBestAskPrice().getAsDouble());

            // Remove remaining orders
            book.removeBestBidOrder(); // 100.0
            book.removeBestAskOrder(); // 103.0

            assertTrue(book.getBestBidPrice().isEmpty());
            assertTrue(book.getBestAskPrice().isEmpty());
        }

        @Test
        @DisplayName("addOrder throws for non-LIMIT orders")
        void addOrderThrowsForNonLimit() {
            OrderBook book = new OrderBook();
            Order marketOrder = new Order(Order.Side.BUY, Order.Type.MARKET, 100.0, 10, 0.0);

            assertThrows(IllegalArgumentException.class, () -> book.addOrder(marketOrder));
        }

        @Test
        @DisplayName("Price level is cleaned up when last order is removed")
        void priceLevelCleanupOnLastRemoval() {
            OrderBook book = new OrderBook();
            Order only = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 50, 0.0);
            book.addOrder(only);

            book.removeOrder(only);

            assertTrue(book.getBestBidPrice().isEmpty());
            assertFalse(book.hasBids());
            assertEquals(0, book.remainingOrderCount());
        }

        @Test
        @DisplayName("remainingOrderCount reflects total resting orders")
        void remainingOrderCount() {
            OrderBook book = createBookWithOrders(); // 2 bids + 2 asks = 4
            assertEquals(4, book.remainingOrderCount());

            book.removeBestBidOrder();
            assertEquals(3, book.remainingOrderCount());

            OrderBook empty = new OrderBook();
            assertEquals(0, empty.remainingOrderCount());
        }

        @Test
        @DisplayName("FIFO ordering preserved within same price level")
        void fifoWithinPriceLevel() {
            OrderBook book = new OrderBook();
            Order first = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 50, 0.0);
            Order second = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 30, 0.1);

            book.addOrder(first);
            book.addOrder(second);

            assertEquals(first.getId(), book.getBestBidOrder().get().getId());
            book.removeBestBidOrder();
            assertEquals(second.getId(), book.getBestBidOrder().get().getId());
        }

        @Test
        @DisplayName("getSnapshot returns correct depth and market state")
        void snapshotContent() {
            OrderBook book = createBookWithOrders();
            OrderBook.OrderBookSnapshot snap = book.getSnapshot();

            assertEquals(2, snap.bids().size());
            assertEquals(2, snap.asks().size());

            assertEquals(101.0, snap.bestBid().getAsDouble());
            assertEquals(102.0, snap.bestAsk().getAsDouble());
            assertTrue(snap.midPrice().isPresent());
            assertTrue(snap.spread().isPresent());

            // snapshot lists must be unmodifiable
            assertThrows(UnsupportedOperationException.class, () -> snap.bids().add(null));
        }

        @Test
        @DisplayName("print snapshot to terminal for visual inspection")
        void printSnapshot() {
            OrderBook book = new OrderBook();

            // Add bids
            book.addOrder(new Order(Order.Side.BUY, Order.Type.LIMIT, 101.00, 100, 0.0));
            book.addOrder(new Order(Order.Side.BUY, Order.Type.LIMIT, 101.00, 50, 0.1)); // same level FIFO
            book.addOrder(new Order(Order.Side.BUY, Order.Type.LIMIT, 100.50, 75, 0.2));
            book.addOrder(new Order(Order.Side.BUY, Order.Type.LIMIT, 100.00, 200, 0.3));

            // Add asks
            book.addOrder(new Order(Order.Side.SELL, Order.Type.LIMIT, 101.50, 80, 0.4));
            book.addOrder(new Order(Order.Side.SELL, Order.Type.LIMIT, 102.00, 120, 0.5));
            book.addOrder(new Order(Order.Side.SELL, Order.Type.LIMIT, 102.00, 30, 0.6)); // same level FIFO
            book.addOrder(new Order(Order.Side.SELL, Order.Type.LIMIT, 103.00, 60, 0.7));

            OrderBook.OrderBookSnapshot snap = book.getSnapshot();
            System.out.println(formatSnapshot("INITIAL BOOK", snap));
            System.out.println("Remaining orders: " + book.remainingOrderCount());

            // Remove best bid — first order at 101.00 (qty=100), second (qty=50) remains
            book.removeBestBidOrder();
            snap = book.getSnapshot();
            System.out.println(formatSnapshot("AFTER removeBestBidOrder", snap));
            System.out.println("Remaining orders: " + book.remainingOrderCount());

            // Remove best ask — 101.50 level gone entirely
            book.removeBestAskOrder();
            snap = book.getSnapshot();
            System.out.println(formatSnapshot("AFTER removeBestAskOrder", snap));
            System.out.println("Remaining orders: " + book.remainingOrderCount());
        }

        // ── Helper — formats snapshot as a readable terminal table ───────────────────
        private String formatSnapshot(String label, OrderBook.OrderBookSnapshot snap) {
            StringBuilder sb = new StringBuilder();
            sb.append(String.format("%n============================================%n"));
            sb.append(String.format("  %s%n", label));
            sb.append(String.format("============================================%n"));

            sb.append("  ASK side (lowest first)\n");
            if (snap.asks().isEmpty()) {
                sb.append("    (empty)\n");
            } else {
                for (OrderBook.PriceLevel level : snap.asks())
                    sb.append(String.format("    price=%-8.2f totalQty=%-6d orders=%d%n",
                            level.price(), level.totalQty(), level.orderCount()));
            }

            sb.append(String.format("  ------------------------------------%n"));
            sb.append(String.format("  Best Ask : %s%n",
                    snap.bestAsk().isPresent() ? String.format("%.2f", snap.bestAsk().getAsDouble()) : "—"));
            sb.append(String.format("  Best Bid : %s%n",
                    snap.bestBid().isPresent() ? String.format("%.2f", snap.bestBid().getAsDouble()) : "—"));
            sb.append(String.format("  Mid Price: %s%n",
                    snap.midPrice().isPresent() ? String.format("%.2f", snap.midPrice().getAsDouble()) : "—"));
            sb.append(String.format("  Spread   : %s%n",
                    snap.spread().isPresent() ? String.format("%.2f", snap.spread().getAsDouble()) : "—"));
            sb.append(String.format("  ------------------------------------%n"));

            sb.append("  BID side (highest first)\n");
            if (snap.bids().isEmpty()) {
                sb.append("    (empty)\n");
            } else {
                for (OrderBook.PriceLevel level : snap.bids())
                    sb.append(String.format("    price=%-8.2f totalQty=%-6d orders=%d%n",
                            level.price(), level.totalQty(), level.orderCount()));
            }

            sb.append(String.format("============================================%n"));
            return sb.toString();
        }
    }
}
