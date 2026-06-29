package ai.analizza.system.async.kafka.kafka;

import ai.analizza.system.async.kafka.client.PaymentServiceClient;
import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.model.OrderStatus;
import ai.analizza.system.async.kafka.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderCreatedListener {

    private static final Logger log = LoggerFactory.getLogger(OrderCreatedListener.class);

    private final OrderRepository orderRepository;
    private final PaymentServiceClient paymentServiceClient;
    private final ObjectMapper objectMapper;

    public OrderCreatedListener(OrderRepository orderRepository,
                                PaymentServiceClient paymentServiceClient,
                                ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.paymentServiceClient = paymentServiceClient;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.created", groupId = "payment-processor-group")
    public void onOrderCreated(String payload) throws Exception {
        Order orderEvent = objectMapper.readValue(payload, Order.class);
        String orderId = orderEvent.getOrderId();

        // 1. Fetch order from DB
        Order existingOrder = orderRepository.findById(orderEvent.getId()).block();
        if (existingOrder == null) {
            log.warn("Order not found in DB for id: {}", orderEvent.getId());
            return;
        }

        // 2. If already PROCESSED, ignore
        if (OrderStatus.COMPLETED.equals(existingOrder.getStatus())) {
            log.info("Order {} is already COMPLETED. Skipping.", orderId);
            return;
        }

        // 3. Invoke Payment Service
        log.info("Calling payment service for order {}", orderId);
        paymentServiceClient.processPayment(orderId);

        // 4. Update order status to COMPLETED
        existingOrder.setStatus(OrderStatus.COMPLETED);
        orderRepository.save(existingOrder).block();
        log.info("Order {} successfully processed and updated.", orderId);
    }
}
