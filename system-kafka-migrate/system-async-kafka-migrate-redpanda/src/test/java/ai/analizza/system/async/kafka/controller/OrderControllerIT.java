package ai.analizza.system.async.kafka.controller;

import ai.analizza.system.async.kafka.dto.OrderDto;
import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.service.OrderService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.http.MediaType;
import reactor.core.publisher.Mono;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import java.math.BigDecimal;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class OrderControllerIT {

    @org.springframework.boot.test.web.server.LocalServerPort
    private int port;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        this.webTestClient = WebTestClient.bindToServer().baseUrl("http://localhost:" + port).build();
    }

    @MockitoBean
    private OrderService orderService;

    @Test
    void createOrderSuccess() {
        OrderDto dto = new OrderDto();
        dto.setProductName("Widget");
        dto.setProductPrice(BigDecimal.valueOf(12.5));
        dto.setProductAmount(4);

        Order order = new Order();
        order.setOrderId("123");
        order.setProductName("Widget");
        order.setProductPrice(BigDecimal.valueOf(12.5));
        order.setProductAmount(4);

        when(orderService.createOrder(any(OrderDto.class))).thenReturn(Mono.just(order));

        webTestClient.post()
                .uri("/orders")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(dto)
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.orderId").isNotEmpty()
                .jsonPath("$.orderId").isEqualTo("123")
                .jsonPath("$.productName").isEqualTo("Widget");
    }
}
