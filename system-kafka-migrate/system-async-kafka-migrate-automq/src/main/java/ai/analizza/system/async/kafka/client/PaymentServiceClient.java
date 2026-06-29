package ai.analizza.system.async.kafka.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class PaymentServiceClient {

    private final WebClient webClient;

    public PaymentServiceClient(WebClient.Builder webClientBuilder, 
                                @Value("${payment.service.url}") String paymentServiceUrl) {
        this.webClient = webClientBuilder.baseUrl(paymentServiceUrl).build();
    }

    public void processPayment(String orderId) {
        webClient.post()
                .uri("/process-payment-order")
                .header("Idempotency-Key", orderId)
                .retrieve()
                .toBodilessEntity()
                .block(); // Blocking here because Kafka listener is synchronous by default
    }
}
