package ai.analizza.system.async.kafka.kafka;

import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.model.OrderStatus;
import ai.analizza.system.async.kafka.repository.OrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class OrderDeadLetterListener {

    private static final Logger log = LoggerFactory.getLogger(OrderDeadLetterListener.class);

    private final OrderRepository orderRepository;
    private final ObjectMapper objectMapper;

    public OrderDeadLetterListener(OrderRepository orderRepository, ObjectMapper objectMapper) {
        this.orderRepository = orderRepository;
        this.objectMapper = objectMapper;
    }

    @KafkaListener(topics = "order.created.DLT", groupId = "payment-dlq-group")
    public void onOrderCreatedDeadLetter(String payload) {
        try {
            Order orderEvent = objectMapper.readValue(payload, Order.class);
            Order existingOrder = orderRepository.findById(orderEvent.getId()).block();

            if (existingOrder != null && existingOrder.getStatus() != OrderStatus.COMPLETED) {
                existingOrder.setStatus(OrderStatus.FAILED);
                orderRepository.save(existingOrder).block();
                log.error("Order {} payment failed persistently. Status updated to FAILED.", existingOrder.getOrderId());
            }
        } catch (Exception e) {
            log.error("Failed to process DLQ message: {}", payload, e);
        }
    }
}
