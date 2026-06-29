package ai.analizza.system.async.kafka.kafka;

import ai.analizza.system.async.kafka.TestSystemAsyncKafkaApplication;
import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.model.OrderStatus;
import ai.analizza.system.async.kafka.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.tomakehurst.wiremock.client.WireMock;
import com.github.tomakehurst.wiremock.junit5.WireMockTest;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.context.annotation.Import;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import static com.github.tomakehurst.wiremock.client.WireMock.*;
import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@Import(ai.analizza.system.async.kafka.TestcontainersConfiguration.class)
@WireMockTest(httpPort = 8081)
public class OrderCreatedListenerIT {

    @Autowired
    private KafkaTemplate<String, String> kafkaTemplate;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("payment.service.url", () -> "http://localhost:8081");
    }

    @BeforeEach
    void setUp() {
        orderRepository.deleteAll().block();
        WireMock.reset();
    }

    @Test
    void shouldProcessPaymentAndCompleteOrder_whenSuccessfulResponse() throws Exception {
        // Arrange
        Order order = new Order("ord-123", "Product A", BigDecimal.TEN, 1);
        order = orderRepository.save(order).block();
        String payload = objectMapper.writeValueAsString(order);

        stubFor(post(urlEqualTo("/process-payment-order"))
                .withHeader("Idempotency-Key", equalTo("ord-123"))
                .willReturn(aResponse().withStatus(200)));

        // Act
        kafkaTemplate.send("order.created", order.getOrderId(), payload);

        // Assert
        final String savedId = order.getId();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(savedId).block();
            assertEquals(OrderStatus.COMPLETED, updatedOrder.getStatus());
        });

        verify(1, postRequestedFor(urlEqualTo("/process-payment-order")));
    }

    @Test
    void shouldRecoverAfterTemporaryFailure() throws Exception {
        // Arrange
        Order order = new Order("ord-124", "Product B", BigDecimal.TEN, 1);
        order = orderRepository.save(order).block();
        String payload = objectMapper.writeValueAsString(order);

        // Scenario: 1st time fails with 500, 2nd time succeeds with 200
        stubFor(post(urlEqualTo("/process-payment-order"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("Started")
                .willSetStateTo("SECOND_TRY")
                .willReturn(aResponse().withStatus(500)));

        stubFor(post(urlEqualTo("/process-payment-order"))
                .inScenario("Retry Scenario")
                .whenScenarioStateIs("SECOND_TRY")
                .willReturn(aResponse().withStatus(200)));

        // Act
        kafkaTemplate.send("order.created", order.getOrderId(), payload);

        // Assert
        final String savedId = order.getId();
        await().atMost(10, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(savedId).block();
            assertEquals(OrderStatus.COMPLETED, updatedOrder.getStatus());
        });

        verify(2, postRequestedFor(urlEqualTo("/process-payment-order")));
    }

    @Test
    void shouldRouteToDLQAndMarkAsFailed_whenPersistentFailure() throws Exception {
        // Arrange
        Order order = new Order("ord-125", "Product C", BigDecimal.TEN, 1);
        order = orderRepository.save(order).block();
        String payload = objectMapper.writeValueAsString(order);

        stubFor(post(urlEqualTo("/process-payment-order"))
                .willReturn(aResponse().withStatus(500)));

        // Act
        kafkaTemplate.send("order.created", order.getOrderId(), payload);

        // Assert
        final String savedId = order.getId();
        await().atMost(15, TimeUnit.SECONDS).untilAsserted(() -> {
            Order updatedOrder = orderRepository.findById(savedId).block();
            assertEquals(OrderStatus.FAILED, updatedOrder.getStatus());
        });

        // 1 initial try + 3 retries = 4 requests
        verify(4, postRequestedFor(urlEqualTo("/process-payment-order")));
    }
}
