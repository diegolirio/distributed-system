package ai.analizza.system.async.kafka.kafka;

import ai.analizza.system.async.kafka.TestcontainersConfiguration;
import ai.analizza.system.async.kafka.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import java.math.BigDecimal;
import java.util.UUID;

@Import(TestcontainersConfiguration.class)
@SpringBootTest
class OrderKafkaProducerTest {

    @Autowired
    OrderKafkaProducer producer;

    @Test
    void sendOrder() {
        Order order = new Order(UUID.randomUUID().toString(), "Widget", BigDecimal.valueOf(5), 1);
        assertDoesNotThrow(() -> producer.publish(order));
    }
}
