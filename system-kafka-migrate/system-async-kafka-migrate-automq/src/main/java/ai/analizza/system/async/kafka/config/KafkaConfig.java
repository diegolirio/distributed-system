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
        return TopicBuilder.name("order.created-dlt").build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaOperations<?, ?> kafkaTemplate) {
        // Send to topic.DLT upon failure
        DeadLetterPublishingRecoverer recoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);
        
        // Retry 3 times, with 1 second delay between retries
        FixedBackOff backOff = new FixedBackOff(1000L, 3L);
        
        return new DefaultErrorHandler(recoverer, backOff);
    }

    @Bean
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
