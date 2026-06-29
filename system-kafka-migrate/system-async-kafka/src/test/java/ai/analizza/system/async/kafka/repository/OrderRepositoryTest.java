package ai.analizza.system.async.kafka.repository;

import ai.analizza.system.async.kafka.TestcontainersConfiguration;
import ai.analizza.system.async.kafka.model.Order;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.UUID;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class OrderRepositoryTest {

    @Autowired
    OrderRepository repo;

    @Test
    void saveAndFind() {
        Order order = new Order(UUID.randomUUID().toString(), "Widget", BigDecimal.valueOf(5), 1);
        StepVerifier.create(repo.save(order).flatMap(saved -> repo.findById(saved.getId())))
            .expectNextMatches(found -> found.getId() != null && "Widget".equals(found.getProductName()))
            .verifyComplete();
    }
}
