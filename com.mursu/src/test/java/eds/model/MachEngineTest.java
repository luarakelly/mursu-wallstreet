
package eds.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class MatchEngineTest {

        private static final double TIME = 100.0;

        @Test
        @DisplayName("BUY limit order fully matches a single resting SELL order")
        void buyOrderFullyMatchesSingleAsk() {
                OrderBook book = new OrderBook();

                Order restingSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                50,
                                1.0);
                book.addOrder(restingSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.LIMIT,
                                105.0,
                                50,
                                2.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(1, trades.size());
                assertTrue(incomingBuy.isFilled());
                assertTrue(restingSell.isFilled());
                assertFalse(book.hasAsks());
        }

        @Test
        @DisplayName("BUY limit order partially matches when larger than resting SELL")
        void buyOrderPartiallyMatchesAsk() {
                OrderBook book = new OrderBook();

                Order restingSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                40,
                                1.0);
                book.addOrder(restingSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.LIMIT,
                                105.0,
                                100,
                                2.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(1, trades.size());
                assertEquals(60, incomingBuy.getRemainingShareSize());
                assertTrue(restingSell.isFilled());
                assertFalse(incomingBuy.isFilled());
        }

        @Test
        @DisplayName("Limit price prevents execution when BUY price is below best ASK")
        void limitPricePreventsTrade() {
                OrderBook book = new OrderBook();

                Order restingSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                110.0,
                                50,
                                1.0);
                book.addOrder(restingSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.LIMIT,
                                100.0,
                                50,
                                2.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(0, trades.size());
                assertTrue(book.hasAsks());
                assertTrue(book.hasBids()); // buy should rest in book
        }

        @Test
        @DisplayName("MARKET order cancels remaining quantity if liquidity is insufficient")
        void marketOrderCancelsRemainder() {
                OrderBook book = new OrderBook();

                Order restingSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                30,
                                1.0);
                book.addOrder(restingSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.MARKET,
                                0.0,
                                100,
                                2.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(1, trades.size());
                assertTrue(restingSell.isFilled());
                assertEquals(Order.Status.CANCELLED, incomingBuy.getStatus());
        }

        @Test
        @DisplayName("FIFO priority is respected within the same price level")
        void fifoPriorityIsRespected() {
                OrderBook book = new OrderBook();

                Order firstSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                30,
                                1.0);

                Order secondSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                30,
                                2.0);

                book.addOrder(firstSell);
                book.addOrder(secondSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.LIMIT,
                                105.0,
                                30,
                                3.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(1, trades.size());
                assertTrue(firstSell.isFilled());
                assertFalse(secondSell.isFilled());
        }

        @Test
        @DisplayName("Best price level is matched before worse price levels")
        void matchesBestPriceFirst() {
                OrderBook book = new OrderBook();

                Order higherPriceSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                105.0,
                                50,
                                1.0);

                Order lowerPriceSell = new Order(
                                Order.Side.SELL,
                                Order.Type.LIMIT,
                                100.0,
                                50,
                                2.0);

                book.addOrder(higherPriceSell);
                book.addOrder(lowerPriceSell);

                Order incomingBuy = new Order(
                                Order.Side.BUY,
                                Order.Type.LIMIT,
                                110.0,
                                50,
                                3.0);

                MatchEngine engine = new MatchEngine();
                List<Trade> trades = engine.match(incomingBuy, book, TIME);

                assertEquals(1, trades.size());
                assertTrue(lowerPriceSell.isFilled());
                assertFalse(higherPriceSell.isFilled());
        }
}