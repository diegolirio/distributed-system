package ai.analizza.system.async.kafka.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.math.BigDecimal;

class OrderTest {

    @Test
    void canCreateOrderWithConstructor() {
        Order order = new Order("uuid-123", "Widget", BigDecimal.valueOf(9.99), 2);

        assertEquals("uuid-123", order.getOrderId());
        assertEquals("Widget", order.getProductName());
        assertEquals(BigDecimal.valueOf(9.99), order.getProductPrice());
        assertEquals(2, order.getProductAmount());
    }

    @Test
    void defaultStatusIsPending() {
        Order order = new Order("uuid-123", "Widget", BigDecimal.valueOf(9.99), 2);

        assertEquals(OrderStatus.PENDING, order.getStatus());
    }

    @Test
    void canSetStatus() {
        Order order = new Order("uuid-123", "Widget", BigDecimal.valueOf(9.99), 2);

        order.setStatus(OrderStatus.COMPLETED);
        assertEquals(OrderStatus.COMPLETED, order.getStatus());

        order.setStatus(OrderStatus.FAILED);
        assertEquals(OrderStatus.FAILED, order.getStatus());
    }

    @Test
    void canSetAllFields() {
        Order order = new Order();

        order.setId("mongo-id");
        order.setOrderId("uuid-456");
        order.setProductName("Gadget");
        order.setProductPrice(BigDecimal.valueOf(19.99));
        order.setProductAmount(5);
        order.setStatus(OrderStatus.COMPLETED);

        assertEquals("mongo-id", order.getId());
        assertEquals("uuid-456", order.getOrderId());
        assertEquals("Gadget", order.getProductName());
        assertEquals(BigDecimal.valueOf(19.99), order.getProductPrice());
        assertEquals(5, order.getProductAmount());
        assertEquals(OrderStatus.COMPLETED, order.getStatus());
    }

    @Test
    void defaultConstructorHasPendingStatus() {
        Order order = new Order();

        assertNull(order.getId());
        assertNull(order.getOrderId());
        assertNull(order.getProductName());
        assertNull(order.getProductPrice());
        assertEquals(0, order.getProductAmount());
        assertEquals(OrderStatus.PENDING, order.getStatus());
    }
}
