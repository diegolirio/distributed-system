package ai.analizza.system.async.kafka.repository;

import ai.analizza.system.async.kafka.model.Order;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface OrderRepository extends ReactiveCrudRepository<Order, String> {}
