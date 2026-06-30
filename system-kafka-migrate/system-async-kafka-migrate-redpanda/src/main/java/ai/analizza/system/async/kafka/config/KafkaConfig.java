package ai.analizza.system.async.kafka.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaOperations;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;
import org.springframework.web.reactive.function.client.WebClient;
import org.apache.kafka.clients.admin.NewTopic;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order.created").build();
    }

    @Bean
    public NewTopic orderCreatedDltTopic() {
        return TopicBuilder.name("order.created.DLT").build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<String, String> kafkaTemplate) {
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        return new DefaultErrorHandler((record, exception) -> {
            try {
                System.out.println("MANUAL RECOVERER SENDING TO DLT: " + record.key());
                kafkaTemplate.send("order.created.DLT", (String) record.key(), (String) record.value()).get();
                System.out.println("MANUAL RECOVERER SEND SUCCESSFUL");
            } catch (Exception e) {
                System.out.println("MANUAL RECOVERER SEND FAILED: " + e.getMessage());
                e.printStackTrace();
            }
        }, backOff);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
