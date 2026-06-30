package ai.analizza.system.async.kafka.kafka;

import ai.analizza.system.async.kafka.model.Order;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
public class OrderKafkaProducer {
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper mapper;
    private static final String TOPIC = "order.created";

    public OrderKafkaProducer(KafkaTemplate<String, String> kafkaTemplate, ObjectMapper mapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.mapper = mapper;
    }

    public void publish(Order order) {
        try {
            String payload = mapper.writeValueAsString(order);
            kafkaTemplate.send(new ProducerRecord<>(TOPIC, order.getOrderId(), payload));
        } catch (Exception e) {
            throw new RuntimeException("Failed to publish order", e);
        }
    }
}
