package ai.analizza.system.async.kafka.service;

import ai.analizza.system.async.kafka.dto.OrderDto;
import ai.analizza.system.async.kafka.kafka.OrderKafkaProducer;
import ai.analizza.system.async.kafka.model.Order;
import ai.analizza.system.async.kafka.model.OrderStatus;
import ai.analizza.system.async.kafka.repository.OrderRepository;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import java.util.UUID;

@Service
public class OrderService {
    private final OrderRepository repository;
    private final OrderKafkaProducer producer;

    public OrderService(OrderRepository repository, OrderKafkaProducer producer) {
        this.repository = repository;
        this.producer = producer;
    }

    public Mono<Order> createOrder(OrderDto dto) {
        Order order = new Order(
                UUID.randomUUID().toString(),
                dto.getProductName(),
                dto.getProductPrice(),
                dto.getProductAmount()
        );
        order.setStatus(OrderStatus.PENDING);
        return repository.save(order)
                .doOnSuccess(producer::publish);
    }
}
