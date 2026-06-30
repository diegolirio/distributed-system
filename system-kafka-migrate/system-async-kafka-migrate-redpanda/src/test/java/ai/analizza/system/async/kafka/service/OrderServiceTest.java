package ai.analizza.system.async.kafka.service;

import ai.analizza.system.async.kafka.dto.OrderDto;
import ai.analizza.system.async.kafka.kafka.OrderKafkaProducer;
import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.model.OrderStatus;
import ai.analizza.system.async.kafka.repository.OrderRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock
    private OrderRepository repository;

    @Mock
    private OrderKafkaProducer producer;

    @InjectMocks
    private OrderService service;

    @Test
    void createOrderPersistsAndPublishes() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.valueOf(10));
        dto.setProductAmount(3);

        Order savedOrder = new Order("test-uuid", "Widget", BigDecimal.valueOf(10), 3);
        savedOrder.setId("mongo-id");
        savedOrder.setStatus(OrderStatus.PENDING);

        when(repository.save(any(Order.class))).thenReturn(Mono.just(savedOrder));

        StepVerifier.create(service.createOrder(dto))
            .assertNext(order -> {
                assertNotNull(order.getId());
                assertEquals("mongo-id", order.getId());
                assertEquals(OrderStatus.PENDING, order.getStatus());
            })
            .verifyComplete();

        verify(producer).publish(savedOrder);
    }
}
