package ai.analizza.system.async.kafka.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class OrderStatusTest {

    @Test
    void enumHasPending() {
        assertNotNull(OrderStatus.PENDING);
    }

    @Test
    void enumHasCompleted() {
        assertNotNull(OrderStatus.COMPLETED);
    }

    @Test
    void enumHasFailed() {
        assertNotNull(OrderStatus.FAILED);
    }

    @Test
    void enumHasExactlyThreeValues() {
        assertEquals(3, OrderStatus.values().length);
    }

    @Test
    void valueOfRoundTrips() {
        for (OrderStatus status : OrderStatus.values()) {
            assertEquals(status, OrderStatus.valueOf(status.name()));
        }
    }
}
