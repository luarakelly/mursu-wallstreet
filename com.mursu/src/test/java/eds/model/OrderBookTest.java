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
    }
}
