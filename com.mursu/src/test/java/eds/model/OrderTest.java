package eds.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    // ── Construction ──────────────────────────────────────────────────────────

    @Nested
    @DisplayName("Construction")
    class Construction {

        @Test
        @DisplayName("Constructor stores all fields correctly")
        void constructor_storesAllFields() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.50, 500, 1.0);

            assertEquals(Order.Side.BUY, order.getSide());
            assertEquals(Order.Type.LIMIT, order.getType());
            assertEquals(100.50, order.getPrice());
            assertEquals(500, order.getOriginalShareSize());
            assertEquals(500, order.getRemainingShareSize());
            assertEquals(Order.Status.NEW, order.getStatus());
            assertEquals(1.0, order.getArrivalTime());
            assertTrue(Double.isNaN(order.getConclusionTime()));
        }

        @Test
        @DisplayName("id is generated and not null or empty")
        void constructor_idGeneratedAndNotEmpty() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            assertNotNull(order.getId());
            assertFalse(order.getId().isBlank());
        }

        @Test
        @DisplayName("Two orders have different ids")
        void constructor_uniqueIds() {
            Order a = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            Order b = new Order(Order.Side.SELL, Order.Type.MARKET, 100.0, 100, 0.0);
            assertNotEquals(a.getId(), b.getId());
        }

        @Test
        @DisplayName("Status is NEW at construction")
        void constructor_statusIsNew() {
            Order order = new Order(Order.Side.SELL, Order.Type.MARKET, 0.0, 100, 0.0);
            assertEquals(Order.Status.NEW, order.getStatus());
        }
    }

    // ── isActive / isFilled ───────────────────────────────────────────────────

    @Nested
    @DisplayName("isActive and isFilled")
    class ActiveAndFilled {

        @Test
        @DisplayName("NEW and PARTIAL order is active and not filled")
        void newOrder_isActiveNotFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            assertTrue(order.isActive());
            assertFalse(order.isFilled());
        }

        void partialOrder_isActiveNotFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.reduceShareSize(40, 1.0); // 60 remaining → PARTIAL
            assertTrue(order.isActive());
            assertFalse(order.isFilled());
        }

        @Test
        @DisplayName("FILLED order is not active and is filled")
        void filledOrder_notActiveIsFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.reduceShareSize(100, 2.0); // 0 remaining → FILLED
            assertFalse(order.isActive());
            assertTrue(order.isFilled());
        }

        @Test
        @DisplayName("CANCELLED order is not active and not filled")
        void cancelledOrder_notActiveNotFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.cancel(1.5);
            assertFalse(order.isActive());
            assertFalse(order.isFilled());
        }
    }

    // ── reduceShareSize ───────────────────────────────────────────────────────

    @Nested
    @DisplayName("reduceShareSize")
    class ReduceShareSize {

        @Test
        @DisplayName("Partial fill reduces remainingShareSize and sets PARTIAL")
        void partialFill_reducesRemainingAndSetsPartial() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 200, 0.0);
            order.reduceShareSize(80, 1.0);

            assertEquals(120, order.getRemainingShareSize());
            assertEquals(Order.Status.PARTIAL, order.getStatus());
        }

        @Test
        @DisplayName("Full fill sets conclusionTime to currentTime")
        void fullFill_setsConclusionTime() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.reduceShareSize(100, 7.5);
            assertEquals(7.5, order.getConclusionTime());
        }

        @Test
        @DisplayName("Multiple partial fills accumulate correctly")
        void multiplePartialFills_accumulateCorrectly() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 300, 0.0);
            order.reduceShareSize(100, 1.0);
            order.reduceShareSize(100, 2.0);

            assertEquals(100, order.getRemainingShareSize());
            assertEquals(Order.Status.PARTIAL, order.getStatus());
        }

        @Test
        @DisplayName("Final fill after partial fills sets FILLED")
        void finalFillAfterPartials_setsFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 300, 0.0);
            order.reduceShareSize(100, 1.0);
            order.reduceShareSize(100, 2.0);
            order.reduceShareSize(100, 3.0);

            assertEquals(0, order.getRemainingShareSize());
            assertEquals(Order.Status.FILLED, order.getStatus());
            assertEquals(3.0, order.getConclusionTime());
        }
    }

    // ── cancel ────────────────────────────────────────────────────────────────

    @Nested
    @DisplayName("cancel")
    class Cancel {

        @Test
        @DisplayName("cancel sets conclusionTime")
        void cancel_setsConclusionTime() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.cancel(4.5);
            assertEquals(4.5, order.getConclusionTime());
        }
    }

    // ── Status lifecycle ──────────────────────────────────────────────────────

    @Nested
    @DisplayName("Status lifecycle")
    class StatusLifecycle {

        @Test
        @DisplayName("Full lifecycle: NEW -> PARTIAL -> FILLED")
        void lifecycle_newToPartialToFilled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            assertEquals(Order.Status.NEW, order.getStatus());

            order.reduceShareSize(60, 1.0);
            assertEquals(Order.Status.PARTIAL, order.getStatus());

            order.reduceShareSize(40, 2.0);
            assertEquals(Order.Status.FILLED, order.getStatus());
        }

        @Test
        @DisplayName("Full lifecycle: NEW -> PARTIAL -> CANCELLED")
        void lifecycle_newToPartialToCancelled() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.reduceShareSize(50, 1.0);
            assertEquals(Order.Status.PARTIAL, order.getStatus());

            order.cancel(2.0);
            assertEquals(Order.Status.CANCELLED, order.getStatus());
        }

        @Test
        @DisplayName("Full lifecycle: NEW -> CANCELLED directly")
        void lifecycle_newToCancelledDirectly() {
            Order order = new Order(Order.Side.BUY, Order.Type.LIMIT, 100.0, 100, 0.0);
            order.cancel(1.0);
            assertEquals(Order.Status.CANCELLED, order.getStatus());
        }
    }
}